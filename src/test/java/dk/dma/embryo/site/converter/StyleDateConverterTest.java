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

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.joda.time.LocalDateTime;
import org.junit.Assert;
import org.junit.Test;

public class StyleDateConverterTest {

    @Test
    public void getDatePattern_US() {
        StyleDateConverter converter = new StyleDateConverter("SS");
        
        assertEquals("M/d/yy h:mm a", converter.getDatePattern(new Locale("en", "US")));
    }


    @Test
    public void getDatePattern_DK() {
        StyleDateConverter converter = new StyleDateConverter("SS");
        
        assertEquals("dd-MM-yy HH:mm", converter.getDatePattern(new Locale("da")));
    }
    
    @Test
    public void test(){
        StyleDateConverter converter = new StyleDateConverter("SS");
        
        LocalDateTime dateTime = converter.convertToObject("9/2/04 4:23 am", new Locale("en", "US"));
        
        Assert.assertNotNull(dateTime);
        Assert.assertEquals("2004-09-02T04:23:00.000", dateTime.toString());
    }
}
