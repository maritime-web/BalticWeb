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

import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.util.convert.IConverter;

import dk.dma.embryo.site.converter.LatitudeConverter;

/**
 * 
 * @author Jesper Tejlgaard
 */
public class LatitudeTextField extends TextField<Double> {

    private static final long serialVersionUID = 1938956927163519213L;

    private final LatitudeConverter converter;

    public LatitudeTextField(String id) {
        super(id);

        this.converter = LatitudeConverter.get();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <C> IConverter<C> getConverter(Class<C> clazz) {
        if (Double.class.isAssignableFrom(clazz)) {
            return (IConverter<C>) converter;
        } else {
            return super.getConverter(clazz);
        }
    }

}
