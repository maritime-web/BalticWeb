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
import java.util.Date;
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
import dk.dma.embryo.common.util.CollectionUtils;

public class NetCDFParser {
    public static final double MIN_VAL = -9.0E32;
    public static final String TIME = "time";
    public static final String LON = "lon";
    public static final String LAT = "lat";

    private List<Double> latList;
    private List<Double> lonList;
    private List<Date> timeList;

    private Map<String, SmallEntry> entries = new HashMap<>();
    private Map<String, NetCDFVar> basicVars = new HashMap<>();

    public NetCDFParser() {
        NetCDFVar.addToMap(basicVars, LAT, "Latitude");
        NetCDFVar.addToMap(basicVars, LON, "Longitude");
        NetCDFVar.addToMap(basicVars, TIME, "Time");
    }

    /**
     * Convenience method for parsing with a default restriction set.
     * 
     * @param filename
     * @return
     * @throws InvalidRangeException
     * @throws IOException
     */
    public Map<NetCDFType, NetCDFResult> parse(String filename, List<? extends NetCDFType> types) throws IOException {
        return parse(filename, types, createDefaultRestriction());
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
    public Map<NetCDFType, NetCDFResult> parse(String filename, List<? extends NetCDFType> types, NetCDFRestriction restriction) throws IOException {
        NetcdfFile netcdfFile = NetcdfFile.open(filename);
        List<Variable> variables = netcdfFile.getVariables();

        Map<NetCDFType, NetCDFResult> results = new HashMap<>();

        // Read vars from NetCDF file.
        for (NetCDFType type : types) {
            Map<String, NetCDFVar> vars = type.getVars();
            for (Variable variable : variables) {
                String varName = variable.getName();
                if (basicVars.containsKey(varName)) {
                    basicVars.get(varName).setVariable(variable);
                } else if (vars.containsKey(varName)) {
                    vars.get(varName).setVariable(variable);
                }
            }

            if (latList == null && lonList == null && timeList == null) {
                // Extract contents from lat and lon simple vars.
                Array lats = basicVars.get(LAT).getVariable().read();
                latList = new ArrayList<>();
                for (int i = 0; i < lats.getSize(); i++) {
                    latList.add(lats.getDouble(i));
                }

                Array lons = basicVars.get(LON).getVariable().read();
                lonList = new ArrayList<>();
                for (int i = 0; i < lons.getSize(); i++) {
                    lonList.add(lons.getDouble(i));
                }

                // Extract contents from the time simple var.
                Array times = basicVars.get(TIME).getVariable().read();
                timeList = new ArrayList<>();
                for (int i = 0; i < times.getSize(); i++) {
                    Date date = getDateTime(times.getDouble(i));
                    timeList.add(date);
                }
            }

            // Retrieve data from the complex vars.

            Map<String, Integer> outputVars = new HashMap<>();

            try {
                int count = 0;
                for (NetCDFVar cdfVar : vars.values()) {
                    if (cdfVar.getVariable() != null) {
                        outputVars.put(cdfVar.getVarname(), count);
                        parseData(cdfVar.getVariable(), count++, restriction);
                    }
                }
            } catch (InvalidRangeException e) {
                throw new IOException("Illegal range when processing NetCDF file " + filename, e);
            }

            // Change variables to their descriptions
            HashMap<Integer, String> reversedVars = CollectionUtils.reverse(outputVars);
            outputVars = new HashMap<>();
            for (int i : reversedVars.keySet()) {
                String value = reversedVars.get(i);
                NetCDFVar netCDFVar = type.getVars().get(value);
                outputVars.put(netCDFVar.getDescription(), i);
            }

            results.put(type, new NetCDFResult(outputVars, getSimpleVars(), entries));
        }
        return results;
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
    private Date getDateTime(double input) {
        BigDecimal[] values = BigDecimal.valueOf(input).divideAndRemainder(BigDecimal.ONE);
        String date = String.valueOf(values[0].intValue());
        if (date.startsWith("2014")) {
            int year = Integer.valueOf(date.substring(0, 4));
            int month = Integer.valueOf(date.substring(4, 6));
            int day = Integer.valueOf(date.substring(6, 8));

            long ms = (long) (values[1].doubleValue() * 3600 * 24 * 1000);
            Period period = new Period(ms);
            int hours = period.getHours();
            if (period.getMinutes() == 59) {
                hours++;
            }
            return new DateTime(year, month, day, hours, 0, DateTimeZone.UTC).toDate();
        } else {
            // DateTime dateTime = new DateTime(2014, 7, 31, 0, 0,
            // DateTimeZone.UTC).plusHours((int) input);
            // return dateTime.toDate();
            return new Date((long) input * 1000);
        }
    }

    /**
     * If a restriction has not been set, this method will create a default one.
     * 
     * @return
     */
    private NetCDFRestriction createDefaultRestriction() {
        NetCDFRestriction restriction = new NetCDFRestriction();
        // restriction.setTimeStart(12);
        // restriction.setTimeInterval(24);
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
    private void parseData(Variable v, int order, NetCDFRestriction restriction) throws InvalidRangeException, IOException {

        // TODO: This method should be updated to optionally shrink the lat and
        // lon ranges in order to support fetching subsets (i.e. restricting the
        // geographic areas used).

        List<Range> ranges = new ArrayList<>();

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
                    String key = j + "_" + k + "_" + i;
                    if (val > MIN_VAL && val != 0 && isWholeCoordinate(latList.get(j)) && isWholeCoordinate(lonList.get(k))) {
                        // The time dimension from the range needs to correspond
                        // to the range we're using, so we're converting the i
                        // variable back to the "original" index.
                        // SmallEntry entry = new SmallEntry(j, k, i * 24 + 12,
                        // val);
                        if (entries.containsKey(key)) {
                            entries.get(key).getObs().put(order, val);
                        } else {
                            SmallEntry entry = new SmallEntry(j, k, i, order, val);
                            entries.put(key, entry);
                        }
                    }
                }

            }
        }

    }

    private boolean isWholeCoordinate(double coord) {
        return coord == Math.floor(coord);
    }

    @SuppressWarnings("unused")
    private boolean isHalfCoordinate(double coord) {
        return ((int) (coord * 10.0)) % 5 == 0;
    }
}
