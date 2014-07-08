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
