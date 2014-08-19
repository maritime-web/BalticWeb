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

import java.util.Locale;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class DateTimeConverter {

    public static final Locale DEFAULT_LOCALE = new Locale("da", "DK");

    private final DateTimeFormatter formatter;

    private final DateTimeFormatter defaultFormatter;

    public static DateTimeFormatter getSSDateTimeFormatter() {
        return DateTimeFormat.forStyle("SS").withZone(DateTimeZone.UTC);
    }
    
    public static DateTimeFormatter getDateTimeStyleFormatter(String style) {
        return DateTimeFormat.forStyle(style).withZone(DateTimeZone.UTC).withLocale(DEFAULT_LOCALE);
    }

    public static DateTimeConverter getDateTimeConverter() {
        return new DateTimeConverter();
    }

    public static DateTimeConverter getDateTimeConverter(String style) {
        return new DateTimeConverter(getDateTimeStyleFormatter(style));
    }

    public DateTimeConverter() {
        this(getSSDateTimeFormatter());
    }

    public DateTimeConverter(DateTimeFormatter formatter) {
        this.formatter = formatter;
        this.defaultFormatter = formatter.withLocale(DEFAULT_LOCALE);
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

    public String toString(DateTime value) {
        if (value == null) {
            return null;
        }
        return formatter.print(value);
    }
}
