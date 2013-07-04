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
import org.joda.time.ReadablePartial;
import org.joda.time.format.DateTimeFormatter;

public abstract class BaseJodaConverter<T extends ReadablePartial> implements IConverter<T> {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a ConversionException and sets additional context information to it.
     * 
     * @param cause
     *            - {@link RuntimeException} cause
     * @param locale
     *            - {@link Locale} used to set 'format' variable with localized pattern
     * @return {@link ConversionException}
     */
    protected ConversionException newConversionException(RuntimeException cause, Locale locale) {
        return new ConversionException(cause).setVariable("format", getDatePattern(locale));
    }
    
    @Override
    public T convertToObject(String value, Locale locale) {
        if (Strings.isEmpty(value)) {
            return null;
        }

        DateTimeFormatter format = getFormat(locale);
        if (format == null) {
            throw new IllegalStateException("format must be not null");
        }

        try {
            T object  = parse(format, value);
            return object;
        } catch (RuntimeException e) {
            throw newConversionException(e, locale);
        }
    }

    /**
     * @see org.apache.wicket.util.convert.IConverter#convertToString(java.lang.Object, java.util.Locale)
     */
    @Override
    public String convertToString(T value, Locale locale) {
        if (value == null) {
            return null;
        }
        
        DateTimeFormatter format = getFormat(locale);
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


    protected abstract T parse(DateTimeFormatter formatter, String value);
}
