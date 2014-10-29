/*
 * Copyright (c) 2011 Danish Maritime Authority.
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

package dk.dma.embryo.tiles.service;

import dk.dma.embryo.tiles.model.TileSet;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.inject.Named;
import java.io.File;

/**
 * Created by Jesper Tejlgaard on 10/08/14.
 */
@Named
public class SourceFileNameParserImpl implements SourceFileNameParser {

    private static DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("yyyyMMdd").withZoneUTC();

    private static DateTimeFormatter tsFormatter = DateTimeFormat.forPattern("yyyyMMddHHmm").withZoneUTC();

    public TileSet parse(File file) {
        String tileSetName = ImageType.getName(file);
        String[] parts = tileSetName.split("_");

        DateTime ts;
        if (parts[2].length() < 12) {
            // The file may only contain date information, e.g. yyyyMMdd
            ts = dateFormatter.parseLocalDate(parts[2].substring(0, 8)).toDateTimeAtStartOfDay(DateTimeZone.UTC);
        } else {
            try {
                // First try if the file contains date and time information, e.g. yyyyMMddHHmm
                ts = tsFormatter.parseDateTime(parts[2].substring(0, 12));
            } catch (IllegalArgumentException e) {
                // The file may only contain date information, e.g. yyyyMMdd
                ts = dateFormatter.parseLocalDate(parts[2].substring(0, 8)).toDateTimeAtStartOfDay(DateTimeZone.UTC);
            }
        }

        return new TileSet(tileSetName, parts[0], parts[1], ts);
    }
}
