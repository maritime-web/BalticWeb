/* Copyright (c) 2011 Danish Maritime Authority.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dk.dma.embryo.dataformats.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.dma.embryo.common.configuration.Property;
import dk.dma.embryo.common.configuration.PropertyFileService;
import dk.dma.embryo.common.log.EmbryoLogService;
import dk.dma.embryo.dataformats.model.ForecastData;
import dk.dma.embryo.dataformats.model.ForecastDataId;
import dk.dma.embryo.dataformats.model.ForecastHeader;
import dk.dma.embryo.dataformats.model.ForecastMetaData;
import dk.dma.embryo.dataformats.model.ForecastProvider;
import dk.dma.embryo.dataformats.model.ForecastType;
import dk.dma.embryo.dataformats.model.Type;
import dk.dma.embryo.dataformats.netcdf.NetCDFRestriction;
import dk.dma.embryo.dataformats.netcdf.NetCDFType;
import dk.dma.embryo.dataformats.netcdf.NetCDFVar;
import dk.dma.embryo.dataformats.persistence.ForecastDataRepository;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

@Stateless
public class ForecastServiceImpl implements ForecastService {

    private static boolean parsing;

    private final Logger logger = LoggerFactory.getLogger(ForecastServiceImpl.class);

    private List<ForecastType> forecastTypes = new ArrayList<>();

    private Map<ForecastProvider, Map<String, NetCDFRestriction>> restrictions = new HashMap<>();

    public static final double MIN_LAT = 55;
    public static final double MID_LAT = 70;
    public static final double MAX_LAT = 80;
    public static final double MIN_LON = -75;
    public static final double MID_LON = -40;
    public static final double MAX_LON = -5;

    public static final String NULL_VALUE = "Greenland";

    @Inject
    @Property(value = "embryo.netcdf.types")
    private Map<String, String> netcdfTypes;

    @Inject
    @Property(value = "embryo.netcdf.providers")
    private String netcdfProviders;

    @Inject
    private PropertyFileService propertyFileService;

    @Inject
    private NetCDFService netCDFService;

    @Inject
    private ForecastDataRepository forecastDataRepository;

    @Inject
    private EmbryoLogService embryoLogService;

    @PostConstruct
    public void init() {
        forecastTypes = createData();
        restrictions = initRestrictions();
        logger.info("INIT");
        reParse();
        logger.info("AFTER reparse INIT");
    }

    @Override
    public List<ForecastType> getForecastTypes() {
        return forecastTypes;
    }

    @Override
    public ForecastType getForecastType(Type type) {
        for (ForecastType t : forecastTypes) {
            if (t.getType() == type) {
                return t;
            }
        }
        return null;
    }

    public List<ForecastHeader> getForecastList(Type type) {
        return forecastDataRepository.list(type);
    }

    @Override
    public List<ForecastHeader> listAvailableIceForecasts() {
        //The quality of FCOO Ice forecasts are not good enough yet
        List<ForecastHeader> forecastList = getForecastList(Type.ICE_FORECAST);
        return forecastList.stream()
                .filter(forecast -> !forecast.getProvider().equals(ForecastProvider.FCOO))
                .collect(Collectors.toList());
    }

    @Override
    public List<ForecastHeader> listAvailableWaveForecasts() {
        return getForecastList(Type.WAVE_FORECAST);
    }

    @Override
    public List<ForecastHeader> listAvailableCurrentForecasts() {
        return getForecastList(Type.CURRENT_FORECAST);
    }

    /**
     * Initiates parsing of the files currently ready for processing.
     */
    @Override
    public void reParse() {
        Set<String> failedFiles = new HashSet<>();
        Set<String> addedFiles = new HashSet<>();

        if (parsing) {
            logger.info("Already parsing, will not re-parse at the moment.");
            return;
        }

        parsing = true;
        logger.info("Re-parsing NetCDF files.");
        try {
            // Loop through providers (DMI and FCOO so far)
            for (String netcdfProvider : netcdfProviders.split(";")) {
                // If we - in theory - have more types than just forecasts
                // (previously called prognoses)
                for (String netcdfType : netcdfTypes.values()) {
                    String folderName = propertyFileService.getProperty("embryo." + netcdfType + "." + netcdfProvider + ".localDirectory", true);
                    logger.info("NetCDF folder: " + folderName);
                    File folder = new File(folderName);
                    if (folder.exists()) {
                        File[] files = folder.listFiles(new FileFilter() {
                            @Override
                            public boolean accept(File pathname) {
                                return pathname.getName().endsWith(".nc");
                            }
                        });
                        if (files != null) {
                            for (File file : files) {
                                String name = file.getName();
                                ForecastProvider provider = name.contains("fcoo") ? ForecastProvider.FCOO : ForecastProvider.DMI;
                                String timestampStr = name.substring(name.length() - 13, name.length() - 3);
                                long timestamp = getTimestamp(timestampStr);
                                if (file.length() != 0) {
                                    logger.info("Importing NetCDF data from file {}.", name);
                                    int errorSize = failedFiles.size();
                                    for (Map.Entry<String, NetCDFRestriction> entry : restrictions.get(provider).entrySet()) {
                                        String area;
                                        if (entry.getKey().equals(NULL_VALUE)) {
                                            area = getArea(name);
                                        } else {
                                            area = entry.getKey();
                                        }
                                        logger.info("Parsing NetCDF area {} for file {}.", area, name);

                                        for (NetCDFType type : getForecastTypes()) {
                                            logger.info("Parsing NetCDF type {} for file {}.", type.getName(), name);
                                            try {
                                                Map<NetCDFType, String> parseResult = netCDFService.parseFile(file, type, entry.getValue());
                                                String json = parseResult.get(type);
                                                if (json != null) {
                                                    logger.info("Got result of size {}.", json.length());
                                                    Type forecastType = ((ForecastType) type).getType();
                                                    ForecastDataId id = new ForecastDataId(area, provider, forecastType);
                                                    ForecastData forecastData = new ForecastData(id, json);
                                                    ForecastMetaData additionalMetaData = new ForecastMetaData()
                                                            .withJsonSize(getJsonSize(json))
                                                            .withTimestamp(timestamp)
                                                            .withOriginalFileName(name);
                                                    forecastData.add(additionalMetaData);
                                                    persistForecastData(forecastData);
                                                } else {
                                                    logger.info("Got empty result for {}.", name);
                                                }
                                            } catch (IOException e) {
                                                failedFiles.add(name);
                                                logger.error("Got error parsing \"" + name + "\"", e);
                                                if (!failedFiles.contains(name)) {
                                                    embryoLogService.error("Error parsing file " + name, e);
                                                }
                                            }
                                        }
                                    }
                                    if (errorSize == failedFiles.size()) {
                                        addedFiles.add(name);
                                    }
                                    file.delete();
                                    // Create a new, empty file so we
                                    // don't download it again
                                    file.createNewFile();
                                }
                            }
                        } else {
                            logger.info("No files found in folder " + folder.getPath());
                        }
                    } else {
                        throw new IOException("Folder " + folderName + " does not exist.");
                    }
                }
            }
            if (failedFiles.size() > 0) {
                embryoLogService.error("Error parsing files " + failedFiles.toString());
            } else if (addedFiles.size() > 0) {
                embryoLogService.info("Sucesssfully parsed files " + addedFiles);
            }
        } catch (IOException e) {
            logger.error("Unhandled error parsing file", e);
            embryoLogService.error("Unhandled error parsing file", e);
        } finally {
            parsing = false;
        }
    }

    /**
     * Finds the estimated zipped size of the provided JSON data. As the content
     * is zipped when sent to the client, this gives a better idea of the actual
     * bandwidth cost than just using the length of the uncompressed string.
     * 
     * @param json JSON string to be zipped.
     * @return Size of the resulting zipped product.
     * @throws IOException
     */
    private int getJsonSize(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gos = new GZIPOutputStream(out);
        String result = mapper.writeValueAsString(json);
        gos.write(result.getBytes());
        gos.close();

        return out.toByteArray().length;
    }

    private void persistForecastData(ForecastData forecastData) {
        ForecastHeader existingHeader = forecastDataRepository.getForecastHeader(forecastData.getId());
        if (forecastData.getHeader().isBetterThan(existingHeader)) {
            logger.info("Persisting \"{}\".", forecastData.getId());
            forecastDataRepository.addOrUpdateForecastData(forecastData);
        }
    }

    /**
     * Converts a time stamp string into a Unix Time long (UTC).
     * 
     * @param timestampStr
     *            String to be converted, in the pattern yyyyMMddHH.
     * @return Number representing number of milliseconds since the Unix Epoch,
     *         UTC.
     */
    private long getTimestamp(String timestampStr) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMddHH");
        DateTime dateTime = formatter.withZoneUTC().parseDateTime(timestampStr);
        return dateTime.getMillis();
    }

    /**
     * Retrieves the area code from a file name.
     * 
     * @param filename
     *            File name to parse for the area code.
     * @return Area code.
     */
    private String getArea(String filename) {
        if (filename.startsWith("hycom-cice")) {
            return filename.substring(11, filename.length() - 14);
        } else if (filename.startsWith("arcticweb_fcoo")) {
            return filename.substring(19, filename.length() - 30);
        }
        return filename;
    }

    /**
     * Sets up the NetCDF variables and links them to the relevant forecast
     * types.
     * 
     * @return A list of forecast types, each with a set of relevant variables
     *         to use.
     */
    public List<ForecastType> createData() {

        List<ForecastType> types = new ArrayList<>();

        // Set up forecast types
        ForecastType iceForecastType = new ForecastType("Ice forecast", "ice", Type.ICE_FORECAST);
        Map<String, NetCDFVar> iceVars = iceForecastType.getVars();

        // DMI

        // Old vars
        NetCDFVar.addToMap(iceVars, "ice-concentration", "Ice concentration");
        NetCDFVar.addToMap(iceVars, "ice-thickness", "Ice thickness");
        NetCDFVar.addToMap(iceVars, "u-ice", "Ice speed east");
        NetCDFVar.addToMap(iceVars, "v-ice", "Ice speed north");
        NetCDFVar.addToMap(iceVars, "Icing", "Ice accretion risk");

        // New vars
        NetCDFVar.addToMap(iceVars, "Ice_con", "Ice concentration");
        NetCDFVar.addToMap(iceVars, "Ice_thk", "Ice thickness");
        NetCDFVar.addToMap(iceVars, "Uice", "Ice speed east");
        NetCDFVar.addToMap(iceVars, "Vice", "Ice speed north");

        // FCOO
        NetCDFVar.addToMap(iceVars, "ICE", "Ice concentration");

        types.add(iceForecastType);

        ForecastType currentForecastType = new ForecastType("Current forecast", "current", Type.CURRENT_FORECAST);
        Map<String, NetCDFVar> currentVars = currentForecastType.getVars();

        // DMI
        NetCDFVar.addToMap(currentVars, "u-current", "Current east");
        NetCDFVar.addToMap(currentVars, "v-current", "Current north");

        // New vars
        NetCDFVar.addToMap(currentVars, "Uocean", "Current east");
        NetCDFVar.addToMap(currentVars, "Vocean", "Current north");

        types.add(currentForecastType);

        ForecastType waveForecastType = new ForecastType("Wave forecast", "wave", Type.WAVE_FORECAST);
        Map<String, NetCDFVar> waveVars = waveForecastType.getVars();

        // DMI
        NetCDFVar.addToMap(waveVars, "var229", "Significant wave height");
        NetCDFVar.addToMap(waveVars, "var230", "Wave direction");
        NetCDFVar.addToMap(waveVars, "var232", "Wave mean period");

        // New var

        NetCDFVar.addToMap(waveVars, "SWH", "Significant wave height");
        NetCDFVar.addToMap(waveVars, "MWD", "Mean wave direction");
        NetCDFVar.addToMap(waveVars, "MWP", "Mean wave period");

        // FCOO
        NetCDFVar.addToMap(waveVars, "DIRMN", "Mean wave direction");
        NetCDFVar.addToMap(waveVars, "Hs", "Significant wave height");
        NetCDFVar.addToMap(waveVars, "TMN", "Mean wave period");
        NetCDFVar.addToMap(waveVars, "Tz", "Zero upcrossing period");
        // Water depth does not account for time. We do not parse this at the
        // moment.
        // NetCDFVar.addToMap(waveVars, "DEPTH", "Water depth");

        types.add(waveForecastType);

        ForecastType windForecastType = new ForecastType("Wind forecast", "wind", Type.WIND_FORECAST);
        Map<String, NetCDFVar> windVars = windForecastType.getVars();

        // DMI
        NetCDFVar.addToMap(windVars, "var245", "Wind speed");
        NetCDFVar.addToMap(windVars, "var249", "Wind direction");

        // New vars

        NetCDFVar.addToMap(windVars, "Uatm", "Wind speed east");
        NetCDFVar.addToMap(windVars, "Vatm", "Wind speed north");

        // FCOO
        NetCDFVar.addToMap(windVars, "WU", "Wind speed east");
        NetCDFVar.addToMap(windVars, "WV", "Wind speed north");

        types.add(windForecastType);

        return types;
    }

    /**
     * Create restriction objects to be used in the parsing procedure.
     * 
     * @return Areas and their restrictions sorted by providers.  
     */
    public Map<ForecastProvider, Map<String, NetCDFRestriction>> initRestrictions() {
        Map<ForecastProvider, Map<String, NetCDFRestriction>> restrictions = new HashMap<>();

        // NetCDFRestriction emptyRestriction = new NetCDFRestriction();
        NetCDFRestriction bottomLeftRestriction = new NetCDFRestriction(MIN_LAT, MID_LAT, MIN_LON, MID_LON);
        NetCDFRestriction bottomRightRestriction = new NetCDFRestriction(MIN_LAT, MID_LAT, MID_LON, MAX_LON);
        NetCDFRestriction topLeftRestriction = new NetCDFRestriction(MID_LAT, MAX_LAT, MIN_LON, MID_LON);
        NetCDFRestriction topRightRestriction = new NetCDFRestriction(MID_LAT, MAX_LAT, MID_LON, MAX_LON);
        NetCDFRestriction svalbardRestriction = new NetCDFRestriction(Svalbard.MIN_LAT, Svalbard.MAX_LAT, Svalbard.MIN_LON, Svalbard.MAX_LON);
        NetCDFRestriction norwegianSeaRestriction = new NetCDFRestriction(NorwegianSea.MIN_LAT, NorwegianSea.MAX_LAT, NorwegianSea.MIN_LON,
                NorwegianSea.MAX_LON);

        Map<String, NetCDFRestriction> dmiRestrictions = new HashMap<>();
        dmiRestrictions.put("Greenland SW", bottomLeftRestriction);
        dmiRestrictions.put("Greenland SE", bottomRightRestriction);
        dmiRestrictions.put("Greenland NW", topLeftRestriction);
        dmiRestrictions.put("Greenland NE", topRightRestriction);
        dmiRestrictions.put("Svalbard", svalbardRestriction);
        dmiRestrictions.put("Norwegian Sea", norwegianSeaRestriction);
        restrictions.put(ForecastProvider.DMI, dmiRestrictions);

        Map<String, NetCDFRestriction> fcooRestrictions = new HashMap<>();
        fcooRestrictions.put("Greenland SW", bottomLeftRestriction);
        fcooRestrictions.put("Greenland SE", bottomRightRestriction);
        fcooRestrictions.put("Greenland NW", topLeftRestriction);
        fcooRestrictions.put("Greenland NE", topRightRestriction);
        restrictions.put(ForecastProvider.FCOO, fcooRestrictions);

        return restrictions;
    }

    public static class Svalbard {
        public static final double MIN_LAT = 75;
        public static final double MAX_LAT = 82;
        public static final double MIN_LON = 0;
        public static final double MAX_LON = 35;
    }

    public static class NorwegianSea {
        public static final double MIN_LAT = 58;
        public static final double MAX_LAT = 73;
        public static final double MIN_LON = -5;
        public static final double MAX_LON = 25;
    }

}
