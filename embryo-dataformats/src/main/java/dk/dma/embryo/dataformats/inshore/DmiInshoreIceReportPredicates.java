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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import org.apache.commons.net.ftp.FTPFile;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import dk.dma.embryo.dataformats.inshore.InshoreIceReportServiceImpl.FileInfo;

/**
 * @author Jesper Tejlgaard
 */
public class DmiInshoreIceReportPredicates {

    public static Predicate<FTPFile> validFormat() {
        return new ValidFormatPredicate();
    }

    public static Predicate<FTPFile> validDateValue(LocalDate limit) {
        return new ValidDateValuePredicate(limit);
    }

    public static Predicate<FTPFile> acceptedReports(LocalDate limit) {
        return Predicates.and(validFormat(), validDateValue(limit));
    }

    public static Predicate<FTPFile> rejectedReports() {
        return Predicates.not(validFormat());
    }

    public static Predicate<FileInfo> allPredicates(Collection<FileInfo> allFiles, DateMidnight date) {
        return Predicates.and(dateLimit(date), fileInfoPredicate(allFiles));
    }

    public static Predicate<FileInfo> fileInfoPredicate(Collection<FileInfo> allFiles) {
        return new FileInfoPredicate(allFiles);
    }

    public static Predicate<FileInfo> dateLimit(DateMidnight date) {
        return new DateLimitPredicate(date);
    }
    
    private static class ValidFormatPredicate implements Predicate<FTPFile> {
        private static final String DATE_FORMAT_STR = "yyyy-MM-dd";
        private DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_STR);

        public ValidFormatPredicate() {
            dateFormat.setLenient(false);
        }

        @Override
        public boolean apply(FTPFile input) {
            String fn = input.getName();
            try {
                dateFormat.parse(fn.substring(0, 10));
            } catch (ParseException e) {
                return false;
            }
            
            int lastDotIndex = 10;
            int versionIndex = fn.indexOf("_v");
            if(versionIndex == 10){
                lastDotIndex = fn.lastIndexOf(".");
                if(lastDotIndex < 0){
                    return false;
                }
                String vStr = fn.substring(12, lastDotIndex);
                try{
                    Integer.parseInt(vStr);
                }catch(NumberFormatException e){
                    return false;
                }
            }
            
            return ".txt".equals(fn.substring(lastDotIndex));
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
                Date date = new SimpleDateFormat("yyyy-MM-dd").parse(input.getName().substring(0, 10));
                DateTime mapDate = new DateTime(date.getTime());
                return mapDate.toLocalDate().isAfter(limit);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static final class FileInfoPredicate implements Predicate<FileInfo> {
        private Collection<FileInfo> allFiles;

        private FileInfoPredicate(Collection<FileInfo> allFiles) {
            this.allFiles = allFiles;
        }

        @Override
        public boolean apply(FileInfo input) {
            
            final boolean DO_APPLY = true;
            final boolean DO_NOT_APPLY = false;
            
            if(input == null) {
                return DO_NOT_APPLY;
            }
            
            for (FileInfo other : allFiles) {
                
                if(other == null) {
                    return DO_NOT_APPLY;
                }
                
                if (other.date.isEqual(input.date)) {
                    if (other.version != null && input.version == null) {
                        return DO_NOT_APPLY;
                    }
                    if (other.version != null && input.version.compareTo(other.version) < 0) {
                        return DO_NOT_APPLY;
                    }
                }
            }

            return DO_APPLY;
        }
    }

    private static final class DateLimitPredicate implements Predicate<FileInfo> {
        private final DateMidnight limit;

        private DateLimitPredicate(DateMidnight date) {
            limit = date;
        }

        @Override
        public boolean apply(FileInfo input) {
            
            final boolean INVALID = false;
            
            if(input == null) {
                return INVALID;
            }
            
            boolean valid = !input.date.isBefore(limit);

            return valid;
        }
    }

}
