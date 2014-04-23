/* Copyright (c) 2011 Danish Maritime Authority
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
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
