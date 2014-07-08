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
package dk.dma.embryo.weather.service;

import org.junit.Test;

import dk.dma.embryo.weather.model.Warnings;

/**
 * @author Jesper Tejlgaard
 */
public class WarningTranslatorTest {

    @Test
    public void testFromDanishToEnglish() {

        Warnings inDanish = new Warnings();
        inDanish.getGale().put("Attu", "Sydsydøst 12 m/s.");
        inDanish.getGale().put("Daneborg", "nordvest 18 m/s.");
        inDanish.getGale().put("Kitaa", "nordnordøst 24 m/s.");

        inDanish.getStorm().put("Narsalik", "Øst 12 m/s.");
        inDanish.getStorm().put("Daneborg", "Nordøst 18 m/s.");
        inDanish.getStorm().put("Kitaa", "South 24 m/s.");

        inDanish.getIcing().put("Meqquitsoq", "Noget helt andet muligvis med nord, syd øst og vest.");
        inDanish.getIcing().put("Daneborg", "Igen noget andet");

        
        Warnings expected = new Warnings();
        expected.getGale().put("Attu", "South south east 12 m/s.");
        expected.getGale().put("Daneborg", "North west 18 m/s.");
        expected.getGale().put("Kitaa", "North north east 24 m/s.");

        expected.getStorm().put("Narsalik", "East 12 m/s.");
        expected.getStorm().put("Daneborg", "North east 18 m/s.");
        expected.getStorm().put("Kitaa", "South 24 m/s.");

        expected.getIcing().put("Meqquitsoq", "Noget helt andet muligvis med nord, syd øst og vest.");
        expected.getIcing().put("Daneborg", "Igen noget andet");

        
    }

}
