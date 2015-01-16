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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;

import com.google.common.collect.Collections2;

import dk.dma.embryo.common.configuration.Property;

@Stateless
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
public class InshoreIceReportServiceImpl implements InshoreIceReportService {

    @Inject
    private Logger logger;

    @Property(value = "embryo.inshoreIceReport.dmi.localDirectory", substituteSystemProperties = true)
    @Inject
    private String localDirectory;

    @Property(value = "embryo.inshoreIceReport.dmi.maxAgeInDays")
    @Inject
    private Integer maxAgeInDays;

    private Map<String, InshoreIceReportMerged> inshoreIceReportsMerged = new HashMap<>();

    public InshoreIceReportServiceImpl(){}

    public InshoreIceReportServiceImpl(String localDirectory, Integer maxAgeInDays){
        this.localDirectory = localDirectory;
        this.maxAgeInDays = maxAgeInDays;
        logger = org.slf4j.LoggerFactory.getLogger(getClass());
    }

    @PostConstruct
    public void init() {
        try {
            update();
        } catch (Exception e) {
            logger.error("Error initializing inshore ice report information", e);
        }
    }


    public void update() throws IOException, InshoreIceReportException {
        File[] readFiles = findInshoreIceReports();

        DateMidnight limit = DateMidnight.now(DateTimeZone.UTC).minusDays(maxAgeInDays);
        logger.debug("Date must be > {}", limit);

        InshoreIceReportMerged result = new InshoreIceReportMerged();
        List<File> rFiles = Arrays.asList(readFiles);
        Collection<FileInfo> fileInfos = Collections2.transform(rFiles, new FileInfoTransformer());
        Collection<FileInfo> filtered = Collections2.filter(fileInfos, DmiInshoreIceReportPredicates.allPredicates(fileInfos, limit));

        List<FileInfo> sorted = new ArrayList<>();
        sorted.addAll(filtered);
        Collections.sort(sorted, new FileInfoComparator());

        Map<String, Exception> problems = new HashMap<>();

        for (FileInfo fileInfo : sorted) {
            try{
                InshoreIceReportParser parser = new InshoreIceReportParser(fileInfo.file);
                InshoreIceReport report = parser.parse();
                if(report.getNotifications().size() == 0){
                    throw new IOException("No observations detected in inshore ice report " + fileInfo.file.getName());
                }
                result.mergeInReport(fileInfo.date.toDate(), fileInfo.file.getName(), report);
            }catch(Exception e){
                problems.put(fileInfo.file.getName(), e);
            }
        }
        setInshoreIceReport("dmi", result);

        if(problems.size() > 0){
            throw new InshoreIceReportException(problems);
        }
    }

    private File[] findInshoreIceReports() throws IOException {
        File dir = new File(localDirectory);
        File[] files = dir.listFiles();
        return files;
    }

    @Override
    @Lock(LockType.READ)
    public InshoreIceReportMerged getInshoreIceReportsMerged(String provider) {
        return inshoreIceReportsMerged.get(provider);
    }

    @Lock(LockType.WRITE)
    private void setInshoreIceReport(String provider, InshoreIceReportMerged notifications) {
        inshoreIceReportsMerged.put(provider, notifications);
    }

    private class FileInfoComparator implements Comparator<FileInfo> {
        @Override
        public int compare(FileInfo o1, FileInfo o2) {
            return o1.file.getName().compareTo(o2.file.getName());
        }
    }

    public static class FileInfo {
        File file;
        DateMidnight date;
        String version;

        @Override
        public String toString() {
            return "FileInfo [file=" + file + ", date=" + date + ", version=" + version + "]";
        }
    }

}
