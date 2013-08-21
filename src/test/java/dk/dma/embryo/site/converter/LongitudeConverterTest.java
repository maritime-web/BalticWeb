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

import org.junit.Assert;

import org.junit.Test;

public class LongitudeConverterTest {

    @Test
    public void convertToObject_null() {
        Assert.assertNull(LongitudeConverter.get().convertToObject(null, null));
    }

    @Test
    public void convertToObject_value() {
        Double result = LongitudeConverter.get().convertToObject("018 45.000W", null); 
        Assert.assertNotNull(result);
        Assert.assertEquals(-18.75, result.doubleValue(), 0.0);
    }

    @Test
    public void convertToString_null() {
        Assert.assertNull(LongitudeConverter.get().convertToString(null, null));
    }

    @Test
    public void convertToString_value() {
        String result = LongitudeConverter.get().convertToString(-18.75, null); 
        Assert.assertNotNull(result);
        Assert.assertEquals("018 45.000W", result);
    }

}
