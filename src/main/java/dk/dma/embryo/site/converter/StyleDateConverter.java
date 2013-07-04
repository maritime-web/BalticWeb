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

import java.util.Locale;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class StyleDateConverter extends DateConverter {

    private static final long serialVersionUID = 1L;
    
    public static final String DEFAULT_DATE_TIME = "SS";

    /**
     * Date style to use. See {@link DateTimeFormat#forStyle(String)}.
     */
    private final String dateStyle;

    /**
     * Construct. The dateStyle 'SS' will be used for constructing the date format for the current locale.
     */
    public StyleDateConverter() {
        this("SS");
    }

    /**
     * Construct. The provided pattern will be used as the base format (but they will be localized for the current
     * locale) . 
     * 
     * @param dateStyle
     *            Date style to use. The first character is the date style, and the second character is the time style.
     *            Specify a character of 'S' for short style, 'M' for medium, 'L' for long, and 'F' for full. A date or
     *            time may be ommitted by specifying a style character '-'. See {@link DateTimeFormat#forStyle(String)}.
     * @throws IllegalArgumentException
     *             in case dateStyle is null
     */
    public StyleDateConverter(String dateStyle) {
        super();
        if (dateStyle == null) {
            throw new IllegalArgumentException("dateStyle must be not null");
        }
        this.dateStyle = dateStyle;
    }

    /**
     * Gets the optional date pattern.
     * 
     * @return datePattern
     */
    @Override
    public final String getDatePattern(Locale locale) {
        return DateTimeFormat.patternForStyle(dateStyle, locale);
    }

    /**
     * @return formatter The formatter for the current conversion
     */
    @Override
    protected DateTimeFormatter getFormat(Locale locale) {
        return DateTimeFormat.forPattern(getDatePattern(locale)).withLocale(locale).withPivotYear(2000);
    }

    @Override
    protected LocalDateTime parse(DateTimeFormatter format, String value) {
        return format.parseLocalDateTime(value);
    }
}
