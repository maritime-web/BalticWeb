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
        
        String fileName = input.getName();
        
        FileInfo info = null;
        if(filenameIsLegal(fileName)) {
            
            info = new FileInfo();
            info.date = formatter.parseDateTime(fileName.substring(0, 10)).toDateTime(DateTimeZone.UTC).toDateMidnight();
            info.file = input;
            int versionIndex = fileName.indexOf("_");
            if (versionIndex > 0) {
                int extensionIndex = fileName.lastIndexOf(".");
                info.version = fileName.substring(versionIndex + 1, extensionIndex);
            }
        }
        
        return info;
    }

    private boolean filenameIsLegal(String fileName) {
        
        final boolean LEGAL = true;
        final boolean ILLEGAL = false;
        
        // Some files starts with a . for example the .DS_Store file on a mac which should be ignored.
        if(fileName != null && !fileName.isEmpty() && !fileName.startsWith(".")) {
            return LEGAL;
        } else {
            
            return ILLEGAL;
        }
    }
}
