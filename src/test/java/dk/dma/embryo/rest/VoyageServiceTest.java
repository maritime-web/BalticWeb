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
package dk.dma.embryo.rest;

import static org.junit.Assert.assertEquals;

import org.joda.time.LocalDateTime;
import org.junit.Test;

import dk.dma.embryo.domain.Voyage;
import dk.dma.embryo.rest.VoyageRestService.VoyageDatum;
import dk.dma.embryo.rest.VoyageRestService.VoyageTransformerFunction;
import dk.dma.embryo.rest.util.DateTimeConverter;

public class VoyageServiceTest {

    @Test
    public void VoyageTransformerFunction_test() {

        LocalDateTime departure = DateTimeConverter.getDateTimeConverter().toObject("10-12-14 12:00", null);
        
        Voyage v = new Voyage("MyBerth", "1 1.100N", "1 2.000W", LocalDateTime.now(), departure);
        v.setEnavId("MyKey");
        VoyageDatum d = new VoyageTransformerFunction(DateTimeConverter.getDateTimeConverter()).apply(v);
        
        assertEquals("MyBerth (10-12-14 12:00)", d.getValue());
        assertEquals("MyBerth", d.getTokens()[0]);
        assertEquals("MyKey", d.getTokens()[1]);
    }

    @Test
    public void VoyageTransformerFunction_testWithNullDate() {

        Voyage v = new Voyage("MyBerth", "1 1.100N", "1 2.000W", LocalDateTime.now(), null);
        v.setEnavId("MyKey");
        VoyageDatum d = new VoyageTransformerFunction(DateTimeConverter.getDateTimeConverter()).apply(v);
        
        assertEquals("MyBerth", d.getValue());
        assertEquals("MyBerth", d.getTokens()[0]);
        assertEquals("MyKey", d.getTokens()[1]);
    }
}
