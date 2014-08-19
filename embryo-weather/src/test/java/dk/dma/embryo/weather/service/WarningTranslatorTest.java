/* Copyright (c) 2011 Danish Maritime Authority.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
