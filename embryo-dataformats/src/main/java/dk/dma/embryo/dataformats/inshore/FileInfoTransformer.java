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
package dk.dma.embryo.dataformats.inshore;

import java.io.File;

import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.common.base.Function;

import dk.dma.embryo.dataformats.inshore.InshoreIceReportServiceImpl.FileInfo;

/**
 * @author Jesper Tejlgaard
 */
public class FileInfoTransformer implements Function<File, FileInfo> {
    private DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd").withZoneUTC();

    @Override
    public FileInfo apply(File input) {
        String value = input.getName();
        FileInfo info = new FileInfo();
        info.date = formatter.parseDateTime(value.substring(0, 10)).toDateTime(DateTimeZone.UTC).toDateMidnight();
        info.file = input;
        int versionIndex = value.indexOf("_");
        if (versionIndex > 0) {
            int extensionIndex = value.lastIndexOf(".");
            info.version = value.substring(versionIndex + 1, extensionIndex);
        }
        return info;
    }
}
