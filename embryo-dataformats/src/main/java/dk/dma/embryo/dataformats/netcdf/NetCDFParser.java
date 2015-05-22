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

import dk.dma.embryo.common.util.CollectionUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class responsible for converting data in a NetCDF file to objects.
 * 
 * @author avlund
 *
 */
public class NetCDFParser {
    public static final double MIN_VAL = -9998;
    public static final String TIME = "time";
    public static final String LON = "lon";
    public static final String LAT = "lat";

    private Range latRange;
    private Range lonRange;

    private List<Double> latList;
    private List<Double> lonList;
    private List<Date> timeList;

    private Map<String, Variable> varMap = new HashMap<>();

    /**
     * Convenience method for parsing with a default restriction set.
     * 
     * @param filename Name of file to parse.
     * @param types Types of forecasts (ice, current etc.) to parse.
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
     * @param filename Name of file to parse.
     * @param types Types of forecasts (ice, current etc.) to parse.
     * @param restriction Coordinate restrictions for the parsed area.
     * @return
     * @throws InvalidRangeException
     * @throws IOException
     */
    public Map<NetCDFType, NetCDFResult> parse(String filename, List<? extends NetCDFType> types, NetCDFRestriction restriction) throws IOException {
        NetcdfFile netcdfFile = NetcdfFile.open(filename);
        List<Variable> variables = netcdfFile.getVariables();

        Map<NetCDFType, NetCDFResult> results = new HashMap<>();

        try {
        // Read vars from NetCDF file.
        for (NetCDFType type : types) {
            Map<String, NetCDFVar> vars = type.getVars();
            for (Variable variable : variables) {
                String varName = variable.getShortName();
                varMap.put(varName, variable);
            }
            try {
                // Extract contents from lat and lon simple vars.
                createLatAndLonLists(restriction);
            } catch (InvalidRangeException e) {
                throw new IOException("Illegal range when processing NetCDF file " + filename, e);
            }

            if (timeList == null) {
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
        } finally {
            netcdfFile.close();
        }

        return results;
    }

    /**
     * Retrieve the metadata for the dataset.
     * 
     * @return A map with the list of latitudes, longitudes and time units for the current dataset.
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
     * @param input The double value to be converted.
     * @return A DateTime object representing the date and time.
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
     * @param order
     * @param restriction
     * @param moments
     * @param digits
     * 
     * @return
     * @throws InvalidRangeException
     * @throws IOException
     */
    private boolean parseData(Variable v, int order, NetCDFRestriction restriction, Map<Integer, NetCDFMoment> moments, int digits)
            throws InvalidRangeException, IOException {
        List<Range> ranges = new ArrayList<>();
        boolean hasContent = false;

        ranges.add(new Range(restriction.getTimeStart(), timeList.size() - 1, restriction.getTimeInterval()));
        ranges.add(latRange);
        ranges.add(lonRange);

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
                    if (val > MIN_VAL && val != 0 && isZeroPointFourCoordinate(latList.get(j)) && isZeroPointFourCoordinate(lonList.get(k))) {
                        moment.addEntry(new NetCDFPoint(j, k), order, cutDigits(val, digits));
                    }
                }

            }
            if (!hasContent && !moment.getEntries().isEmpty()) {
                hasContent = true;
            }
        }
        return hasContent;
    }

    /**
     * Populate the ranges and lat/lon lists based on the current restriction values.
     * 
     * @param restriction Restriction for the current processing.
     * @throws IOException
     * @throws InvalidRangeException
     */
    private void createLatAndLonLists(NetCDFRestriction restriction) throws IOException, InvalidRangeException {
        latList = new ArrayList<>();
        lonList = new ArrayList<>();
        Array lats = varMap.get(LAT).read();
        Array lons = varMap.get(LON).read();
        if (restriction.isSubarea()) {

//            System.out.println("File edges: min lat: " + lats.getDouble(0) + ", max lat: " + lats.getDouble((int)lats.getSize() - 1));
//            System.out.println("File edges: min lon: " + lons.getDouble(0) + ", max lon: " + lons.getDouble((int)lons.getSize() - 1));
            
            int cnt = -1;
            double val = -999;
            while (val < Math.min(restriction.getMinLat(), getRoundedCoordinate(lats.getDouble((int)lats.getSize() -1)))) {
                val = getRoundedCoordinate(lats.getDouble(++cnt));
            }
            int minLat = cnt;
            while (val < Math.min(restriction.getMaxLat(), getRoundedCoordinate(lats.getDouble((int)lats.getSize() -1)))) {
                val = getRoundedCoordinate(lats.getDouble(cnt));
                latList.add(val);
                cnt++;
            }
            int maxLat = cnt - 1;

            cnt = -1;
            val = -999;
            while (val < Math.min(restriction.getMinLon(), getRoundedCoordinate(lons.getDouble((int)lons.getSize() -1)))) {
                val = getRoundedCoordinate(lons.getDouble(++cnt));
            }
            int minLon = cnt;
            while (val < Math.min(restriction.getMaxLon(), getRoundedCoordinate(lons.getDouble((int)lons.getSize() -1)))) {
                val = getRoundedCoordinate(lons.getDouble(cnt));
                lonList.add(val);
                cnt++;
            }
            int maxLon = cnt - 1;

//            System.out.println("Min lat: " + lats.getDouble(minLat));
//            System.out.println("Max lat: " + lats.getDouble(maxLat));
//            System.out.println("Min lon: " + lons.getDouble(minLon));
//            System.out.println("Max lon: " + lons.getDouble(maxLon));
//            System.out.println("List edges: min lat: " + latList.get(0) + ", max lat: " + latList.get(latList.size() - 1));
//            System.out.println("List edges: min lon: " + lonList.get(0) + ", max lon: " + lonList.get(lonList.size() - 1));
//            System.out.println();

            latRange = new Range(minLat, maxLat);
            lonRange = new Range(minLon, maxLon);
        } else {
            for (int i = 0; i < lats.getSize(); i++) {
                latList.add(lats.getDouble(i));
            }
            for (int i = 0; i < lons.getSize(); i++) {
                lonList.add(lons.getDouble(i));
            }
            latRange = new Range(0, latList.size() - 1);
            lonRange = new Range(0, lonList.size() - 1);
        }
    }

    /**
     * Retrieves a value rounded to the provided number of digits.
     * 
     * @param value
     * @param digits
     * @return
     */
    private float cutDigits(float value, int digits) {
        int factor = (int) Math.pow(10, digits);
        int result = (int) (value * factor);
        return (float) result / factor;
    }

    /**
     * Retrieves a rounded version of a coordinate.
     * 
     * @param coord
     * @return
     */
    private double getRoundedCoordinate(double coord) {
        double adjustedCoord = Math.abs(coord) + 0.0001;
        int timesTen = (int) (adjustedCoord * 10);
        adjustedCoord = timesTen / 10.0;
        return adjustedCoord * (coord < 0 ? -1 : 1);
    }

    /**
     * Checks if a coordinate is divisible by 0.4.
     * 
     * @param coord
     * @return
     */
    private boolean isZeroPointFourCoordinate(double coord) {
        double adjustedCoord = Math.abs(coord) + 0.0001;
        int timesTen = (int) (adjustedCoord * 10);
        int remainder = timesTen % 4;
        return remainder == 0;
    }
}
