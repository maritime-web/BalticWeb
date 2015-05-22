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
import dk.dma.embryo.dataformats.model.Forecast;
import dk.dma.embryo.dataformats.model.Forecast.Provider;
import dk.dma.embryo.dataformats.model.ForecastType;
import dk.dma.embryo.dataformats.model.ForecastType.Type;
import dk.dma.embryo.dataformats.netcdf.NetCDFRestriction;
import dk.dma.embryo.dataformats.netcdf.NetCDFType;
import dk.dma.embryo.dataformats.netcdf.NetCDFVar;
import dk.dma.embryo.dataformats.persistence.ForecastDao;
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
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

@Stateless
public class ForecastServiceImpl implements ForecastService {

    private static boolean parsing;

    private final Logger logger = LoggerFactory.getLogger(ForecastServiceImpl.class);

    private List<ForecastType> forecastTypes = new ArrayList<>();

    private Map<Provider, Map<String, NetCDFRestriction>> restrictions = new HashMap<>();

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
    private ForecastDao forecastDao;

    @Inject
    private ForecastPersistService forecastPersistService;

    @Inject
    private EmbryoLogService embryoLogService;

    @PostConstruct
    public void init() {
        forecastTypes = createData();
        restrictions = initRestrictions();
        reParse();
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

    public List<Forecast> getForecastList(Type type) {
        List<Forecast> list = forecastDao.list(type);
        return list;
    }

    @Override
    public Forecast getForecast(long id) {
        Forecast forecast = forecastDao.findById(id);
        return forecast;
    }

    @Override
    public List<Forecast> listAvailableIceForecasts() {
        return getForecastList(Type.ICE_FORECAST);
    }

    @Override
    public List<Forecast> listAvailableWaveForecasts() {
        return getForecastList(Type.WAVE_FORECAST);
    }

    @Override
    public List<Forecast> listAvailableCurrentForecasts() {
        return getForecastList(Type.CURRENT_FORECAST);
    }

    /**
     * Initiates parsing of the files currently ready for processing.
     */
    @Override
    public void reParse() {
        if (!parsing) {
            logger.info("Re-parsing NetCDF files.");
            parsing = true;
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
                                    Provider provider = name.contains("fcoo") ? Provider.FCOO : Provider.DMI;
                                    String timestampStr = name.substring(name.length() - 13, name.length() - 3);
                                    long timestamp = getTimestamp(timestampStr);
                                    if (file.length() == 0) {
                                        if (!forecastDao.exists(name, timestamp)) {
                                            // File has been downloaded, but
                                            // there's no entry in the database,
                                            // probably because of a database
                                            // wipe. We remove the empty file
                                            // and it will be re-downloaded next
                                            // time.
                                            logger.info("Found empty file {} with no corresponding database entry - deleting.", name);
                                            if (!file.delete()) {
                                                logger.error("Could not delete file {}.", name);
                                            }
                                        }
                                    } else {
                                        logger.info("Importing NetCDF data from file {}.", name);
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
                                                Map<NetCDFType, String> parseResult = netCDFService.parseFile(file, type, entry.getValue());
                                                String json = parseResult.get(type);
                                                if (json != null) {
                                                    logger.info("Got result of size {}, persisting.", json.length());
                                                    persistForecast(name, json, ((ForecastType) type).getType(), getJsonSize(json), provider, timestamp, area);
                                                } else {
                                                    logger.info("Got empty result, persisting.");
                                                    persistForecast(name, "", ((ForecastType) type).getType(), -1, provider, timestamp, area);
                                                }
                                            }
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
                embryoLogService.info("Finished parsing forecast files");
            } catch (IOException e) {
                logger.error("Unhandled error parsing file", e);
                embryoLogService.error("Unhandled error parsing file", e);
            } finally {
                parsing = false;
            }
        } else {
            logger.info("Already parsing, will not re-parse at the moment.");
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

    /**
     * Sends forecast data to the ForecastPersistService to be persisted to
     * database. This is necessarily in its own class as it requires to be in a
     * separate transaction (if everything was in the same transaction, it would
     * likely timeout).
     * 
     * @param name
     *            Forecast file name.
     * @param json
     *            Forecast JSON data.
     * @param type
     *            Forecast type.
     * @param size
     *            Zipped size of forecast.
     * @param provider
     *            Forecast provider.
     * @param timestamp
     *            Timestamp for forecast start.
     * @param area
     *            Area name for forecast.
     */
    private void persistForecast(String name, String json, Type type, int size, Provider provider, long timestamp, String area) {
        Forecast forecast = new Forecast(name, json, type, size, provider, timestamp, area);
        forecastPersistService.persist(forecast);
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
    public Map<Provider, Map<String, NetCDFRestriction>> initRestrictions() {
        Map<Provider, Map<String, NetCDFRestriction>> restrictions = new HashMap<>();

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
        restrictions.put(Provider.DMI, dmiRestrictions);

        Map<String, NetCDFRestriction> fcooRestrictions = new HashMap<>();
        fcooRestrictions.put("Greenland SW", bottomLeftRestriction);
        fcooRestrictions.put("Greenland SE", bottomRightRestriction);
        fcooRestrictions.put("Greenland NW", topLeftRestriction);
        fcooRestrictions.put("Greenland NE", topRightRestriction);
        restrictions.put(Provider.FCOO, fcooRestrictions);

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
