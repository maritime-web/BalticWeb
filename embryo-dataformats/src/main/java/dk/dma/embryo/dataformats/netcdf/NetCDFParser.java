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
package dk.dma.embryo.dataformats.netcdf;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;

import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

public class NetCDFParser {
    public static final double MIN_VAL = -9.0E32;
    public static final String TIME = "time";
    public static final String LON = "lon";
    public static final String LAT = "lat";

    private List<Double> latList;
    private List<Double> lonList;
    private List<DateTime> timeList;

    public static final String FILE_NAME = "wam.grib.2014042900.NATLANT.nc";
    private Map<String, NetCDFVar> cdfVars;

    public NetCDFParser() {
        // Setup
        cdfVars = new HashMap<>();

        NetCDFVar.addToMap(cdfVars, LAT, "Latitude", false);
        NetCDFVar.addToMap(cdfVars, LON, "Longitude", false);
        NetCDFVar.addToMap(cdfVars, TIME, "Time", false);
        NetCDFVar.addToMap(cdfVars, "var229", "Wave height", true);
        NetCDFVar.addToMap(cdfVars, "var230", "Wave direction", true);
        NetCDFVar.addToMap(cdfVars, "var245", "Wind speed", true);
        NetCDFVar.addToMap(cdfVars, "var249", "Wind direction", true);

    }

    /**
     * Convenience method for parsing with a default restriction set.
     * 
     * @param filename
     * @return
     * @throws InvalidRangeException
     * @throws IOException
     */
    public NetCDFResult parse(String filename) throws InvalidRangeException, IOException {
        return parse(filename, createDefaultRestriction());
    }

    /**
     * Parses a NetCDF file and returns a result containing the data as well as
     * metadata to map this data to.
     * 
     * @param filename
     * @param restriction
     * @return
     * @throws InvalidRangeException
     * @throws IOException
     */
    public NetCDFResult parse(String filename, NetCDFRestriction restriction) throws InvalidRangeException, IOException {
        NetcdfFile netcdfFile = NetcdfFile.open(filename);
        List<Variable> variables = netcdfFile.getVariables();

        // Read vars from NetCDF file.
        for (Variable v : variables) {
            String varName = v.getName();
            NetCDFVar cdfVar = cdfVars.get(varName);
            if (cdfVar != null) {
                cdfVar.setVariable(v);
            }
        }

        // Extract contents from lat and lon simple vars. As these data are
        // unlikely to ever change, we only do this once.
        if (latList == null || lonList == null) {
            Array lats = cdfVars.get(LAT).getVariable().read();
            latList = new ArrayList<>();
            for (int i = 0; i < lats.getSize(); i++) {
                latList.add(lats.getDouble(i));
            }

            Array lons = cdfVars.get(LON).getVariable().read();
            lonList = new ArrayList<>();
            for (int i = 0; i < lons.getSize(); i++) {
                lonList.add(lons.getDouble(i));
            }
        }

        // Extract contents from the time simple var. This data will change over
        // time, so it's re-parsed from every file.
        Array times = cdfVars.get(TIME).getVariable().read();
        timeList = new ArrayList<>();
        for (int i = 0; i < times.getSize(); i++) {
            DateTime dateTime = getDateTime(times.getDouble(i));
            timeList.add(dateTime);
        }

        // Retrieve data from the complex vars.
        Map<String, List<SmallEntry>> entries = new HashMap<>();
        for (NetCDFVar cdfVar : cdfVars.values()) {
            if (cdfVar.isComplex()) {
                entries.put(cdfVar.getDescription(), getData(cdfVar.getVariable(), restriction));
            }
        }

        return new NetCDFResult(getSimpleVars(), entries);
    }

    /**
     * Retrieve the metadata for the dataset.
     * 
     * @return
     */
    public Map<String, List<? extends Serializable>> getSimpleVars() {
        Map<String, List<? extends Serializable>> result = new HashMap<>();
        result.put(LAT, latList);
        result.put(LON, lonList);
        result.put(TIME, timeList);
        return result;
    }

    /**
     * The time variable received from a NetCDF file is stored as a double. This
     * method converts it to a DateTime object.
     * 
     * @param input
     * @return
     */
    private DateTime getDateTime(double input) {
        BigDecimal[] values = BigDecimal.valueOf(input).divideAndRemainder(BigDecimal.ONE);
        String date = String.valueOf(values[0].intValue());
        int year = Integer.valueOf(date.substring(0, 4));
        int month = Integer.valueOf(date.substring(4, 6));
        int day = Integer.valueOf(date.substring(6, 8));

        long ms = (long) (values[1].doubleValue() * 3600 * 24 * 1000);
        Period period = new Period(ms);
        int hours = period.getHours();
        if (period.getMinutes() == 59) {
            hours++;
        }
        return new DateTime(year, month, day, hours, 0, DateTimeZone.UTC);
    }

    /**
     * If a restriction has not been set, this method will create a default one.
     * 
     * @return
     */
    private NetCDFRestriction createDefaultRestriction() {
        NetCDFRestriction restriction = new NetCDFRestriction();
        restriction.setTimeStart(12);
        restriction.setTimeInterval(24);
        return restriction;
    }

    /**
     * Retrieve data from a NetCDF complex variable.
     * 
     * @param v
     * @return
     * @throws InvalidRangeException
     * @throws IOException
     */
    private List<SmallEntry> getData(Variable v, NetCDFRestriction restriction) throws InvalidRangeException, IOException {

        // TODO: This method should be updated to optionally shrink the lat and
        // lon ranges in order to support fetching subsets (i.e. restricting the
        // geographic areas used).

        List<SmallEntry> entries = new ArrayList<>();
        List<Range> ranges = new ArrayList<>();

        // The time range start on the first day at 12PM and is incremented with
        // 24 hours for each iteration. The lat and lon ranges are used in full.
        ranges.add(new Range(restriction.getTimeStart(), timeList.size() - 1, restriction.getTimeInterval()));
        ranges.add(new Range(restriction.getMinLat(), restriction.getMaxLat() != 0 ? restriction.getMaxLat() : latList.size() - 1));
        ranges.add(new Range(restriction.getMinLat(), restriction.getMaxLon() != 0 ? restriction.getMaxLon() : lonList.size() - 1));
        Array data = v.read(ranges);
        int[] shape = data.getShape();
        Index index = data.getIndex();
        for (int i = 0; i < shape[0]; i++) {
            for (int j = 0; j < shape[1]; j++) {
                for (int k = 0; k < shape[2]; k++) {
                    float val = data.getFloat(index.set(i, j, k));
                    // We are not interested in default/empty values, so these
                    // are excluded.
                    if (val > MIN_VAL) {
                        // The time dimension from the range needs to correspond
                        // to the range we're using, so we're converting the i
                        // variable back to the "original" index.
                        SmallEntry entry = new SmallEntry(j, k, i * 24 + 12, val);
                        entries.add(entry);
                    }
                }

            }
        }
        return entries;

    }

    public Entry toEntry(SmallEntry smallEntry) {
        return new Entry(latList.get(smallEntry.getLat()), lonList.get(smallEntry.getLon()), timeList.get(smallEntry.getTime()), smallEntry.getObservation());
    }
}
