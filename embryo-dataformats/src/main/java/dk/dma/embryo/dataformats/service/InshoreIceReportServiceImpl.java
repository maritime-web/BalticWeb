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
package dk.dma.embryo.dataformats.service;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.slf4j.Logger;

import dk.dma.embryo.common.configuration.Property;
import dk.dma.embryo.dataformats.model.InshoreIceReport;
import dk.dma.embryo.dataformats.notification.InshoreIceReportParser;

@Stateless
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
public class InshoreIceReportServiceImpl implements InshoreIceReportService {

    @Inject
    private Logger logger;

    @Property(value = "embryo.inshoreIceReport.dmi.localDirectory", substituteSystemProperties = true)
    @Inject
    private String localDirectory;

    private Map<String, InshoreIceReport> inshoreIceReports = new HashMap<>();

    @PostConstruct
    public void init() {
        try {
            update();
        } catch (IOException e) {
            logger.error("Error initializing ice information", e);
        }
    }

    public void update() throws IOException {

        File newest = findInshoreIceReport();
        if (newest != null) {
            InshoreIceReportParser parser = new InshoreIceReportParser(newest);
            InshoreIceReport iceInformations = parser.parse();
            setInshoreIceReport("dmi", iceInformations);
        }
    }
    
    private class FileComparator implements Comparator<File>{
        @Override
        public int compare(File o1, File o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }

    private File findInshoreIceReport() throws IOException {
        File dir = new File(localDirectory);
        File[] files = dir.listFiles();
        if(files.length == 0){
            return null;
        }
        
        Arrays.sort(files, new FileComparator());
        return files[files.length - 1];
    }

    @Override
    @Lock(LockType.READ)
    public InshoreIceReport getInshoreIceReport(String provider) {
        return inshoreIceReports.get(provider);
    }

    @Lock(LockType.WRITE)
    private void setInshoreIceReport(String provider, InshoreIceReport notifications) {
        inshoreIceReports.put(provider, notifications);
    }
}
