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

import java.util.Arrays;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;

/**
 * @author Jesper Tejlgaard
 */
public class DmiFTPFileFilters {
    
    private static final String[] acceptedFileNames = new String[]{"gronvar.xml", "gruds.xml"};
    
    /**
     * Accepts all (non-null) FTPFile directory entries
     */
    public static final FTPFileFilter FILES = new FTPFileFilter() {
        public boolean accept(FTPFile file) {
            return file != null && !file.isDirectory() && Arrays.binarySearch(acceptedFileNames, file.getName()) >= 0;
        }
    };

}
