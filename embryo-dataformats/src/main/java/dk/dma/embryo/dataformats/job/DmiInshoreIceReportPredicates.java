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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.net.ftp.FTPFile;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

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

    private static class ValidFormatPredicate implements Predicate<FTPFile> {
        private static final String DATE_FORMAT_STR = "yyyy-MM-dd";
        private DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_STR);
        
        public ValidFormatPredicate(){
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
            return ".txt".equals(fn.substring(10));
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
}
