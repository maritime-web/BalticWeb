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
import java.text.DecimalFormat;
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
    public static final double MIN_VAL = -9998;
    public static final String TIME = "time";
    public static final String LON = "lon";
    public static final String LAT = "lat";

    private List<Double> latList;
    private List<Double> lonList;
    private List<Date> timeList;

    private Map<String, Variable> varMap = new HashMap<>();

    /**
     * Convenience method for parsing with a default restriction set.
     * 
     * @param filename
     * @return
     * @throws InvalidRangeException
     * @throws IOException
     */
    public Map<NetCDFType, NetCDFResult> parse(String filename, List<? extends NetCDFType> types) throws IOException {
        return parse(filename, types, new NetCDFRestriction());
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
                String varName = variable.getShortName();
                varMap.put(varName, variable);
            }

            if (latList == null && lonList == null && timeList == null) {
                // Extract contents from lat and lon simple vars.
                Array lats = varMap.get(LAT).read();
                latList = new ArrayList<>();
                for (int i = 0; i < lats.getSize(); i++) {
                    latList.add(lats.getDouble(i));
                }

                Array lons = varMap.get(LON).read();
                lonList = new ArrayList<>();
                for (int i = 0; i < lons.getSize(); i++) {
                    lonList.add(lons.getDouble(i));
                }

                // Extract contents from the time simple var.
                Array times = varMap.get(TIME).read();
                timeList = new ArrayList<>();
                for (int i = 0; i < times.getSize(); i++) {
                    Date date = getDateTime(times.getDouble(i));
                    timeList.add(date);
                }
            }

            // Retrieve data from the complex vars.

            Map<String, Integer> outputVars = new HashMap<>();

            Map<Integer, NetCDFMoment> moments = new HashMap<>();
            boolean empty = true;
            try {
                int count = 0;
                for (NetCDFVar cdfVar : vars.values()) {
                    if (varMap.containsKey(cdfVar.getVarname())) {
                        outputVars.put(cdfVar.getVarname(), count);
                        boolean hasContent = parseData(varMap.get(cdfVar.getVarname()), count++, restriction, moments, cdfVar.getDigits());
                        if (empty && hasContent) {
                            empty = false;
                        }
                    }
                }
            } catch (InvalidRangeException e) {
                throw new IOException("Illegal range when processing NetCDF file " + filename, e);
            }

            if (empty) {
                results.put(type, null);
            } else {
                // Change variables to their descriptions
                HashMap<Integer, String> reversedVars = CollectionUtils.reverse(outputVars);
                outputVars = new HashMap<>();
                for (int i : reversedVars.keySet()) {
                    String value = reversedVars.get(i);
                    NetCDFVar netCDFVar = type.getVars().get(value);
                    outputVars.put(netCDFVar.getDescription(), i);
                }
                results.put(type, new NetCDFResult(outputVars, getSimpleVars(), moments));
            }
        }
        netcdfFile.close();
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
            return new DateTime((long) input * 1000, DateTimeZone.UTC).toDate();
        }
    }

    /**
     * Retrieve data from a NetCDF complex variable.
     * 
     * @param v
     * @return
     * @throws InvalidRangeException
     * @throws IOException
     */
    private boolean parseData(Variable v, int order, NetCDFRestriction restriction, Map<Integer, NetCDFMoment> moments, int digits) throws InvalidRangeException,
            IOException {
        List<Range> ranges = new ArrayList<>();
        int minLat, minLon, maxLat, maxLon;
        boolean hasContent = false;

        if (restriction.isSubarea()) {
            minLat = findClosestCoordIndex(restriction.getMinLat(), latList, true);
            maxLat = findClosestCoordIndex(restriction.getMaxLat(), latList, false);
            minLon = findClosestCoordIndex(restriction.getMinLon(), lonList, true);
            maxLon = findClosestCoordIndex(restriction.getMaxLon(), lonList, false);
        } else {
            minLat = 0;
            maxLat = latList.size() - 1;
            minLon = 0;
            maxLon = lonList.size() - 1;
        }
        ranges.add(new Range(restriction.getTimeStart(), timeList.size() - 1, restriction.getTimeInterval()));
        ranges.add(new Range(minLat, maxLat));
        ranges.add(new Range(minLon, maxLon));

        Array data = v.read(ranges);
        int[] shape = data.getShape();
        Index index = data.getIndex();
        
        for (int i = 0; i < shape[0]; i++) {

            NetCDFMoment moment = moments.get(i);
            if (moment == null) {
                moment = new NetCDFMoment(i);
                moments.put(i, moment);
            }
            for (int j = 0; j < shape[1]; j++) {
                for (int k = 0; k < shape[2]; k++) {
                    final float val = data.getFloat(index.set(i, j, k));
                    // We are not interested in default/empty values, so these
                    // are excluded.
                    if (val > MIN_VAL && val != 0 && isZeroPointFourCoordinate(latList.get(j + minLat)) && isZeroPointFourCoordinate(lonList.get(k + minLon))) {
                        moment.addEntry(new NetCDFPoint(j + minLat, k + minLon), order, cutDigits(val, digits));
                    }
                }

            }
            if (!hasContent && !moment.getEntries().isEmpty()) {
                hasContent = true;
            }
        }
        return hasContent;
    }
    
    private float cutDigits(float value, int digits) {
        int factor = (int) Math.pow(10, digits);
        int result = (int)(value * factor);
        return (float)result / factor;
    }

    private int findClosestCoordIndex(double coord, List<Double> list, boolean isMinValue) {
        for (int i = 0; i < list.size(); i++) {
            double d = list.get(i);
            if (d > coord) {
                if (isMinValue) {
                    return i;
                } else {
                    return i - 1;
                }
            }
        }
        if (isMinValue) {
            return 0;
        } else {
            return list.size() - 1;
        }
    }

    @SuppressWarnings("unused")
    private boolean isWholeCoordinate(double coord) {
        double adjustedCoord = Math.abs(coord) + 0.0001;
        int timesTen = (int) (adjustedCoord * 10);
        int remainder = timesTen % 10;
        return remainder == 0;
    }

    @SuppressWarnings("unused")
    private boolean isHalfCoordinate(double coord) {
        return ((int) (coord * 10.0)) % 5 == 0;
    }
    
    private boolean isZeroPointFourCoordinate(double coord) {
        double adjustedCoord = Math.abs(coord) + 0.0001;
        int timesTen = (int) (adjustedCoord * 10);
        int remainder = timesTen % 4;
        return remainder == 0;
    }
}
