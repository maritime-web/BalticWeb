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
package dk.dma.embryo.common.util;

/**
 * Utility class different parsing tasks
 */
public class ParseUtils {

    public static Double parseDouble(String str) {
        if (str == null || str.length() == 0) {
            return null;
        }
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            throw new FormatException("Could not parse " + str + " as a decimal number");
        }
    }

    public static Integer parseInt(String str) {
        if (str == null || str.length() == 0) {
            return null;
        }
        if (!isNumeric(str)) {
            throw new FormatException("Could not parse " + str + " as an integer");
        }
        return Integer.parseInt(str);
    }

    public static Long parseLong(String str) {
        if (str == null || str.length() == 0) {
            return null;
        }
        if (!isNumeric(str)) {
            throw new FormatException("Could not parse " + str + " as a long");
        }
        return Long.parseLong(str);
    }

    public static String parseString(String str) {
        str = str.trim();
        if (str == null || str.length() == 0) {
            return null;
        }
        return str;
    }

    public static double parseLatitude(String formattedString) {
        String[] parts = splitFormattedPos(formattedString);
        return parseLatitude(parts[0], parts[1], parts[2]);
    }

    public static double parseLongitude(String formattedString) {
        String[] parts = splitFormattedPos(formattedString);
        return parseLongitude(parts[0], parts[1], parts[2]);
    }

    private static String[] splitFormattedPos(String posStr) {
        if (posStr.length() < 4) {
            throw new FormatException("Invalid value '" + posStr + "'. Does not follow correct format");
        }
        String[] parts = new String[3];
        parts[2] = posStr.substring(posStr.length() - 1);
        posStr = posStr.substring(0, posStr.length() - 1);
        String[] posParts = posStr.split(" ");
        if (posParts.length != 2) {
            throw new FormatException("Invalid value '" + posStr + "'. Does not follow correct format");
        }
        parts[0] = posParts[0];
        parts[1] = posParts[1];

        return parts;
    }

    public static double parseLatitude(String hours, String minutes, String northSouth) {
        Integer h = parseInt(hours);
        Double m = parseDouble(minutes);
        String ns = parseString(northSouth);
        if (h == null || m == null || ns == null) {
            throw new FormatException();
        }
        if (!ns.equals("N") && !ns.equals("S")) {
            throw new FormatException("Invalid value '" + ns + "'. Must be 'N' or 'S'");
        }
        double lat = h + m / 60.0;
        if (ns.equals("S")) {
            lat *= -1;
        }
        return lat;
    }

    public static double parseLongitude(String hours, String minutes, String eastWest) {
        Integer h = parseInt(hours);
        Double m = parseDouble(minutes);
        String ew = parseString(eastWest);
        if (h == null || m == null || ew == null) {
            throw new FormatException();
        }
        if (!ew.equals("E") && !ew.equals("W")) {
            throw new FormatException("Invalid value '" + ew + "'. Must be 'E' or 'W'");
        }
        double lon = h + m / 60.0;
        if (ew.equals("W")) {
            lon *= -1;
        }
        return lon;
    }

    public static boolean isNumeric(String str) {
        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

}
