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

package dk.dma.embryo.tiles.image;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.dma.embryo.tiles.model.TileSet;

/**
 * Created by Jesper Tejlgaard on 11/10/14.
 */
public class GeoImage {

    private static DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("yyyyMMdd").withZoneUTC();
    private static DateTimeFormatter tsFormatter = DateTimeFormat.forPattern("yyyyMMddHHmm").withZoneUTC();

    private java.io.File file;
    private ImageType type;
    private Logger logger = LoggerFactory.getLogger(GeoImage.class);

    public GeoImage(File file) {
        this.file = file;
        this.type = ImageType.getType(file);
    }


    public boolean delete() {
        switch (type) {
            case JPG: {
                return deleteFile(metaFile(file, ".jpg", ".jgw")) && deleteFile(metaFile(file, ".jpg", ".jpg.aux.xml")) &&
                        deleteFile(metaFile(file, ".jpg", ".prj")) &&
                        deleteFile(file);
            }
        }
        return deleteFile(file);
    }

    private File metaFile(File file, String currentExtension, String metaExtension) {
        return new File(file.getParentFile(), file.getName().replaceAll(currentExtension, metaExtension));
    }

    private boolean deleteFile(File file) {
        if (file.exists()) {
            if (!FileUtils.deleteQuietly(file)) {
                logger.info("Failed deleting file {}", file);
                return false;
            }
        }
        return true;
    }

    public static TileSet parse(File file) {
        String tileSetName = ImageType.getName(file);
        String[] parts = tileSetName.split("_");
        DateTime ts = parseDate(parts[2]);
        return new TileSet(tileSetName, parts[0], parts[1], ts);
    }

    private static DateTime parseDate(String name) {
        DateTime ts;
        if (name.length() < 12) {
            // The file may only contain date information, e.g. yyyyMMdd
            ts = dateFormatter.parseLocalDate(name.substring(0, 8)).toDateTimeAtStartOfDay(DateTimeZone.UTC);
        } else {
            try {
                // First try if the file contains date and time information, e.g. yyyyMMddHHmm
                ts = tsFormatter.parseDateTime(name.substring(0, 12));
            } catch (IllegalArgumentException e) {
                // The file may only contain date information, e.g. yyyyMMdd
                ts = dateFormatter.parseLocalDate(name.substring(0, 8)).toDateTimeAtStartOfDay(DateTimeZone.UTC);
            }
        }
        return ts;
    }

    public static DateTime extractTs(File file) {
        String[] parts = file.getName().split("_");
        return parseDate(parts[2]);
    }
}
