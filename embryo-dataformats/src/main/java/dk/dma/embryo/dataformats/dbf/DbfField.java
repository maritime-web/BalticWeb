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
package dk.dma.embryo.dataformats.dbf;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Adapted from:
 *
 * http://code.google.com/p/jdbf/
 */

public class DbfField {
    public DbfField(String s, char c, int i, int j) {
        if (s.length() > 10) {
            throw new RuntimeException(
                    "The field name is more than 10 characters long: " + s);
        }
        if (c != 'C' && c != 'N' && c != 'L' && c != 'D' && c != 'F') {
            throw new RuntimeException("The field type is not a valid. Got: " + c);
        }
        if (i < 1) {
            throw new RuntimeException(
                    "The field length should be a positive integer. Got: " + i);
        }
        if (c == 'C' && i >= 254) {
            throw new RuntimeException("The field length should be less than 254 characters for character fields. Got: " +
                    i);
        }
        if (c == 'N' && i >= 30) {
            throw new RuntimeException(
                    "The field length should be less than 21 digits for numeric fields. Got: " +
                            i);
        }
        if (c == 'L' && i != 1) {
            throw new RuntimeException(
                    "The field length should be 1 characater for logical fields. Got: " +
                            i);
        }
        if (c == 'D' && i != 8) {
            throw new RuntimeException(
                    "The field length should be 8 characaters for date fields. Got: " + i);
        }
        if (c == 'F' && i >= 21) {
            throw new RuntimeException("The field length should be less than 21 digits for floating point fields. Got: " +
                    i);
        }
        if (j < 0) {
            throw new RuntimeException(
                    "The field decimal count should not be a negative integer. Got: " + j);
        }
        if ((c == 'C' || c == 'L' || c == 'D') && j != 0) {
            throw new RuntimeException("The field decimal count should be 0 for character, logical, and date fields. Got: " +
                    j);
        }
        if (j > i - 1) {
            throw new RuntimeException(
                    "The field decimal count should be less than the length - 1. Got: " +
                            j);
        } else {
            name = s;
            type = c;
            length = i;
            decimalCount = j;
            return;
        }
    }

    public String getName() {
        return name;
    }

    public char getType() {
        return type;
    }

    public int getLength() {
        return length;
    }

    public int getDecimalCount() {
        return decimalCount;
    }

    public String format(Object obj) {
        if (type == 'N' || type == 'F') {
            if (obj == null) {
                obj = 0.0D;
            }
            if (obj instanceof Number) {
                Number number = (Number) obj;
                StringBuffer stringbuffer = new StringBuffer(getLength());
                for (int i = 0; i < getLength(); i++) {
                    stringbuffer.append("#");

                }
                if (getDecimalCount() > 0) {
                    stringbuffer.setCharAt(getLength() - getDecimalCount() - 1, '.');
                }
                DecimalFormat decimalformat = new DecimalFormat(stringbuffer.toString());
                String s1 = decimalformat.format(number);
                int k = getLength() - s1.length();
                if (k < 0) {
                    throw new RuntimeException("Value " + number +
                            " cannot fit in pattern: '" + stringbuffer +
                            "'.");
                }
                StringBuffer stringbuffer2 = new StringBuffer(k);
                for (int l = 0; l < k; l++) {
                    stringbuffer2.append(" ");

                }
                return stringbuffer2 + s1;
            } else {
                throw new RuntimeException("Expected a Number, got " + obj.getClass() +
                        ".");
            }
        }
        if (type == 'C') {
            if (obj == null) {
                obj = "";
            }
            if (obj instanceof String) {
                String s = (String) obj;
                if (s.length() > getLength()) {
                    throw new RuntimeException("'" + obj + "' is longer than " + getLength() +
                            " characters.");
                }
                StringBuffer stringbuffer1 = new StringBuffer(getLength() - s.length());
                for (int j = 0; j < getLength() - s.length(); j++) {
                    stringbuffer1.append(' ');

                }
                return s + stringbuffer1;
            } else {
                throw new RuntimeException("Expected a String, got " + obj.getClass() +
                        ".");
            }
        }
        if (type == 'L') {
            if (obj == null) {
                obj = false;
            }
            if (obj instanceof Boolean) {
                Boolean boolean1 = (Boolean) obj;
                return boolean1 ? "Y" : "N";
            } else {
                throw new RuntimeException("Expected a Boolean, got " + obj.getClass() +
                        ".");
            }
        }
        if (type == 'D') {
            if (obj == null) {
                obj = new Date();
            }
            if (obj instanceof Date) {
                Date date = (Date) obj;
                SimpleDateFormat simpledateformat = new SimpleDateFormat("yyyyMMdd");
                return simpledateformat.format(date);
            } else {
                throw new RuntimeException("Expected a Date, got " + obj.getClass() + ".");
            }
        } else {
            throw new RuntimeException("Unrecognized type: " + type);
        }
    }

    public Object parse(String s) {
        s = s.trim();
        if (type == 'N' || type == 'F') {
            if (s.equals("")) {
                s = "0";
            }
            try {
                if (getDecimalCount() == 0) {
                    return new Long(s);
                } else {
                    return new Double(s);
                }
            } catch (NumberFormatException numberformatexception) {
                throw new RuntimeException(numberformatexception);
            }
        }
        if (type == 'C') {
            return s;
        }
        if (type == 'L') {
            if (s.equals("Y") || s.equals("y") || s.equals("T") || s.equals("t")) {
                return true;
            }
            if (s.equals("N") || s.equals("n") || s.equals("F") || s.equals("f")) {
                return false;
            } else {
                throw new RuntimeException("Unrecognized value for logical field: " + s);
            }
        }
        if (type == 'D') {
            SimpleDateFormat simpledateformat = new SimpleDateFormat("yyyyMMdd");
            try {
                if ("".equals(s)) {
                    return null;
                } else {
                    return simpledateformat.parse(s);
                }
            } catch (ParseException parseexception) {
                throw new RuntimeException(parseexception);
            }
        } else {
            throw new RuntimeException("Unrecognized type: " + type);
        }
    }

    public String toString() {
        return name;
    }

    private String name;
    private char type;
    private int length;
    private int decimalCount;
}
