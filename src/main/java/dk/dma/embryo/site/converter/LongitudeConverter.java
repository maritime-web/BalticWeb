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

import org.apache.wicket.util.convert.IConverter;

import dk.dma.embryo.domain.ParseUtils;
import dk.dma.enav.model.geometry.Position;

public class LongitudeConverter implements IConverter<Double> {

    private static final long serialVersionUID = -2563300688711954763L;
    
    LongitudeConverter() {
    };

    @Override
    public Double convertToObject(String value, Locale locale) {
        if (value == null) {
            return null;
        }
        return ParseUtils.parseLongitude(value);
    }

    @Override
    public String convertToString(Double value, Locale locale) {
        if (value == null) {
            return null;
        }
        return Position.create(0, value).getLongitudeAsString();
    }

    public static LongitudeConverter get(){
        return new LongitudeConverter();
    }
}
