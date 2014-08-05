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
package dk.dma.embryo.dataformats.job;

import static com.google.common.base.Predicates.not;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import org.apache.commons.net.ftp.FTPFile;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

/**
 * @author Jesper Tejlgaard
 */
public class DmiIceChartPredicates {

    public static Predicate<FTPFile> validFormat(Set<String> regions) {
        return new ValidFormatPredicate(regions);
    }

    public static Predicate<FTPFile> validDateValue(LocalDate limit) {
        return new ValidDateValuePredicate(limit);
    }

    public static Predicate<FTPFile> fullyDownloaded(String localChartDirectory, String ... requiredFileTypes) {
        return new FullyDownloadedPredicate(localChartDirectory, requiredFileTypes);
    }

    @SuppressWarnings("unchecked")
    public static Predicate<FTPFile> acceptedIceCharts(Set<String> regions, LocalDate limit, String localChartDirectory, String[] requiredFileTypes) {
        return Predicates.and(validFormat(regions), validDateValue(limit), not(fullyDownloaded(localChartDirectory, requiredFileTypes)));
    }

    private static class ValidFormatPredicate implements Predicate<FTPFile> {
        private static final String DATE_FORMAT_STR = "yyyyMMddHHmm";
        private final Set<String> regions;
        private DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_STR);

        public ValidFormatPredicate(Set<String> regions) {
            this.regions = regions;
        }

        @Override
        public boolean apply(FTPFile input) {
            String fn = input.getName();
            try {
                dateFormat.parse(fn.substring(0, 12));
            } catch (ParseException e) {
                return false;
            }

            String regionAndVersion = fn.substring(13);

            for (String c : regions) {
                if (regionAndVersion.startsWith(c)) {
                    if (regionAndVersion.length() > c.length()) {
                        if (regionAndVersion.length() < c.length() + 3) {
                            return false;
                        }
                        String version = regionAndVersion.substring(c.length() + 1);
                        if (!version.startsWith("v")) {
                            return false;
                        }
                        try {
                            int value = Integer.parseInt(version.substring(1));
                            return value > 0;
                        } catch (NumberFormatException e) {
                            return false;
                        }
                    }
                    return true;
                }
            }
            return false;
        }

    }

    private static class ValidDateValuePredicate implements Predicate<FTPFile> {
        private final LocalDate limit;

        public ValidDateValuePredicate(LocalDate limit) {
            this.limit = limit;
        }

        @Override
        public boolean apply(FTPFile input) {
            try {
                Date date = new SimpleDateFormat("yyyyMMddHHmm").parse(input.getName().substring(0, 12));
                DateTime mapDate = new DateTime(date.getTime());
                return mapDate.toLocalDate().isAfter(limit);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class FullyDownloadedPredicate implements Predicate<FTPFile> {
        private final String localDmiDirectory;
        private final String[] requiredFilesInIceObservation;

        public FullyDownloadedPredicate(String localDmiDirectory, String ... requiredFileTypes) {
            this.localDmiDirectory = localDmiDirectory;
            this.requiredFilesInIceObservation = requiredFileTypes;
        }

        @Override
        public boolean apply(FTPFile file) {
            for (String suffix : requiredFilesInIceObservation) {
                String fileName = localDmiDirectory + "/" + file.getName() + suffix;
                if (!new File(fileName).exists()) {
                    return false;
                }
            }

            return true;
        }
    }
}
