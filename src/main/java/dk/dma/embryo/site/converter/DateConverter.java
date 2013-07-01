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

import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.string.Strings;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormatter;

public abstract class DateConverter implements IConverter<LocalDateTime> {
    
    private static final long serialVersionUID = 1L;
    
    @Override
    public LocalDateTime convertToObject(String value, Locale locale) {
        if (Strings.isEmpty(value)) {
            return null;
        }

        DateTimeFormatter format = getFormat(locale);
        if (format == null) {
            throw new IllegalStateException("format must be not null");
        }

//        if (applyTimeZoneDifference) {
//            TimeZone zone = getClientTimeZone();
//            DateTime dateTime;
//
//            // set time zone for client
//            format = format.withZone(getTimeZone());
//
//            try {
//                // parse date retaining the time of the submission
//                dateTime = format.parseDateTime(value);
//            } catch (RuntimeException e) {
//                throw newConversionException(e, locale);
//            }
//            // apply the server time zone to the parsed value
//            if (zone != null) {
//                dateTime = dateTime.withZoneRetainFields(DateTimeZone.forTimeZone(zone));
//            }
//
//            return dateTime.toDate();
//        } else {
            try {
                LocalDateTime dateTime = format.parseLocalDateTime(value);
                return dateTime;
            } catch (RuntimeException e) {
                
                e.printStackTrace();
                
                throw newConversionException(e, locale);
            }
//        }
    }

    /**
     * Creates a ConversionException and sets additional context information to it.
     * 
     * @param cause
     *            - {@link RuntimeException} cause
     * @param locale
     *            - {@link Locale} used to set 'format' variable with localized pattern
     * @return {@link ConversionException}
     */
    private ConversionException newConversionException(RuntimeException cause, Locale locale) {
        return new ConversionException(cause).setVariable("format", getDatePattern(locale));
    }

    /**
     * @see org.apache.wicket.util.convert.IConverter#convertToString(java.lang.Object, java.util.Locale)
     */
    @Override
    public String convertToString(LocalDateTime value, Locale locale) {
        if (value == null) {
            return null;
        }
        
        DateTimeFormatter format = getFormat(locale);

        // if (applyTimeZoneDifference)
        // {
        // TimeZone zone = getClientTimeZone();
        // if (zone != null)
        // {
        // // apply time zone to formatter
        // format = format.withZone(DateTimeZone.forTimeZone(zone));
        // }
        // }
        return format.print(value);
    }

    /**
     * @param locale
     *            The locale used to convert the value
     * @return Gets the pattern that is used for printing and parsing
     */
    public abstract String getDatePattern(Locale locale);

    /**
     * @param locale
     *            The locale used to convert the value
     * 
     * @return formatter The formatter for the current conversion
     */
    protected abstract DateTimeFormatter getFormat(Locale locale);


}
