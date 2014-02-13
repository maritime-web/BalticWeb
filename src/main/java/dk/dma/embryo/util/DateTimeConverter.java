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
package dk.dma.embryo.util;

import java.util.Locale;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class DateTimeConverter {

    public static final Locale DEFAULT_LOCALE = new Locale("da", "DK");

    private final DateTimeFormatter formatter;

    private final DateTimeFormatter defaultFormatter;
    private final DateTimeFormatter defaultMMFormatter;

    public static DateTimeFormatter getSSDateTimeFormatter() {
        return DateTimeFormat.forStyle("SS").withZone(DateTimeZone.UTC);
    }
    
    public static DateTimeFormatter getMMDateTimeFormatter() {
        return DateTimeFormat.forStyle("MM").withZone(DateTimeZone.UTC);
    }
    
    public static DateTimeConverter getDateTimeConverter() {
        return new DateTimeConverter();
    }

    public DateTimeConverter() {
        this.formatter = getSSDateTimeFormatter();
        this.defaultFormatter = formatter.withLocale(DEFAULT_LOCALE);
        this.defaultMMFormatter = getMMDateTimeFormatter().withLocale(DEFAULT_LOCALE);
    }

    public DateTime toObject(String value) {
        return toObject(value, null);
    }

    public DateTime toObject(String value, Locale locale) {
        if (value == null) {
            return null;
        }
        if (value.trim().length() == 0) {
            return null;
        }
        if (locale != null) {
            return formatter.withLocale(locale).parseDateTime(value);
        }

        return defaultFormatter.parseDateTime(value);
    }

    public String toStringMedium(DateTime value) {
        if (value == null) {
            return null;
        }
        return defaultMMFormatter.print(value);
    }
}
