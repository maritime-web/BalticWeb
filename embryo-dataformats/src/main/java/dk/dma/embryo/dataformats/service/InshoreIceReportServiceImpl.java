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

    private class FileComparator implements Comparator<File>{
        @Override
        public int compare(File o1, File o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }

}
