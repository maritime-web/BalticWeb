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

import java.util.Arrays;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;

/**
 * @author Jesper Tejlgaard
 */
public class DmiFTPFileFilters {
    
    private static final String[] ACCEPTED_FILE_NAMES = new String[]{"gronvar.xml", "gruds.xml", "grudseng.xml"};
    
    /**
     * Accepts all (non-null) FTPFile directory entries
     */
    public static final FTPFileFilter FILES = new FTPFileFilter() {
        public boolean accept(FTPFile file) {
            return file != null && !file.isDirectory() && Arrays.binarySearch(ACCEPTED_FILE_NAMES, file.getName()) >= 0;
        }
    };

}
