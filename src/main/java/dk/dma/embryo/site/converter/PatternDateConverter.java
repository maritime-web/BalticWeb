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
package dk.dma.embryo.site.converter;

import java.text.SimpleDateFormat;
import java.util.Locale;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class PatternDateConverter extends BaseJodaConverter<LocalDateTime> {

    private static final long serialVersionUID = 1L;

    /** pattern to use. */
    private final String datePattern;

    /**
     * Construct.
     * 
     * @param datePattern
     *            The pattern to use. Must be not null. See {@link SimpleDateFormat} for available patterns.
     * @throws IllegalArgumentException
     *             in case the date pattern is null
     */
    public PatternDateConverter(String datePattern) {
        super();
        if (datePattern == null) {
            throw new IllegalArgumentException("datePattern must be not null");
        }
        this.datePattern = datePattern;
    }

    /**
     * Gets the optional date pattern.
     * 
     * @return datePattern
     */
    @Override
    public final String getDatePattern(Locale locale) {
        return datePattern;
    }

    /**
     * @return formatter The formatter for the current conversion
     */
    @Override
    protected DateTimeFormatter getFormat(Locale locale) {
        return DateTimeFormat.forPattern(datePattern).withLocale(locale).withPivotYear(2000);
    }

    @Override
    protected LocalDateTime parse(DateTimeFormatter format, String value) {
        return format.parseLocalDateTime(value);
    }
}
