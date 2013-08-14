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
import dk.dma.embryo.rest.VoyageService.VoyageDatum;

import dk.dma.embryo.rest.VoyageService.VoyageTransformerFunction;

public class VoyageServiceTest {

    @Test
    public void VoyageTransformerFunction_test() {
        
        Voyage v = new Voyage("MyBerth", "1 1.100N", "1 2.000W", LocalDateTime.now(), LocalDateTime.now());
        v.setEnavId("MyKey");
        VoyageDatum d = new VoyageTransformerFunction().apply(v);
        
        assertEquals("MyBerth - (MyKey)", d.getValue());
        assertEquals("MyBerth", d.getTokens()[0]);
        assertEquals("MyKey", d.getTokens()[1]);
    }
}
