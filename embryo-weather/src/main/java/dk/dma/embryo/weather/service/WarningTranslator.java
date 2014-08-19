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

import java.util.Map.Entry;

import dk.dma.embryo.weather.model.Warnings;

/**
 * Parser for reading routes in RT3 format. RT3 format is among others used by Transas ECDIS.
 * 
 * @author Jesper Tejlgaard
 */
public class WarningTranslator {

    public Warnings fromDanishToEnglish(Warnings warnings) {
        Warnings result = new Warnings();

        for (Entry<String, String> entry : warnings.getGale().entrySet()) {
            result.getGale().put(entry.getKey(), translateDirections(entry.getValue()));
        }

        // We have no data specifications nor examples from DMI at the time of writing. Assuming data looks like gale
        // warning.
        for (Entry<String, String> entry : warnings.getStorm().entrySet()) {
            result.getStorm().put(entry.getKey(), translateDirections(entry.getValue()));
        }

        // no translation yet, as data is not known at the time of writing
        result.getIcing().putAll(warnings.getIcing());
        
        return result;
    }

    private String translateDirections(String value) {
        value = value.substring(0, 1).toLowerCase() + value.substring(1);
        value = value.replaceAll("nord", " north");
        value = value.replaceAll("syd", " south");
        value = value.replaceAll("øst", " east");
        value = value.replaceAll("vest", " west");
        value = value.trim();
        value = value.substring(0, 1).toUpperCase() + value.substring(1);
        return value;
    }

}
