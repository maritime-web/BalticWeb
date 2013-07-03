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
package dk.dma.embryo.site.markup.html.form;

import java.text.SimpleDateFormat;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.form.AbstractTextComponent.ITextFormatProvider;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.lang.Args;
import org.joda.time.LocalDateTime;

import dk.dma.embryo.site.converter.DateConverter;
import dk.dma.embryo.site.converter.StyleDateConverter;

/**
 * 
 * @author Jesper Tejlgaard
 */
public class DateTimeTextField extends TextField<LocalDateTime> implements ITextFormatProvider {

    private static final long serialVersionUID = 1L;

    /**
     * The converter for the TextField
     */
    private final DateConverter converter;

    /**
     * Construct with a converter.
     * 
     * @param id
     *            The component id
     * @param model
     *            The model
     * @param converter
     *            The converter to use
     */
    public DateTimeTextField(String id, boolean includePlaceholder, DateConverter converter) {
        super(id, LocalDateTime.class);

        Args.notNull(converter, "converter");
        this.converter = converter;

        if (includePlaceholder) {
            add(new AttributeModifier("placeholder", this.converter.getDatePattern(Session.get().getLocale())));
        }
    }

    // /**
    // * Creates a new DateTextField defaulting to using the date pattern ""
    // *
    // * @param id
    // * The id of the text field
    // * @param datePattern
    // * The pattern to use. Must be not null. See {@link SimpleDateFormat} for available patterns.
    // * @return DateTextField
    // */
    // public static DateTimeTextField forDatePattern(String id, String datePattern) {
    // return new DateTimeTextField(id, new PatternDateConverter(datePattern));
    // }

    /**
     * Creates a new DateTextField defaulting to using the date pattern ""
     * 
     * @param id
     *            The id of the text field
     * @param datePattern
     *            The pattern to use. Must be not null. See {@link SimpleDateFormat} for available patterns.
     * @return DateTextField
     */
    public static DateTimeTextField forDateStyle(String id, boolean includePlaceholder, String style) {
        return new DateTimeTextField(id, includePlaceholder, new StyleDateConverter(style));
    }

    /**
     * Creates a new DateTextField defaulting to using the date pattern ""
     * 
     * @param id
     *            The id of the text field
     * @param datePattern
     *            The pattern to use. Must be not null. See {@link SimpleDateFormat} for available patterns.
     * @return DateTextField
     */
    public static DateTimeTextField forDateStyle(String id, String style) {
        return forDateStyle(id, false, style);
    }

    /**
     * @return The specialized converter.
     * @see org.apache.wicket.Component#getConverter(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <C> IConverter<C> getConverter(Class<C> clazz) {
        if (LocalDateTime.class.isAssignableFrom(clazz)) {
            return (IConverter<C>) converter;
        } else {
            return super.getConverter(clazz);
        }
    }

    @Override
    public final String getTextFormat() {
        return converter.getDatePattern(getLocale());
    }
}
