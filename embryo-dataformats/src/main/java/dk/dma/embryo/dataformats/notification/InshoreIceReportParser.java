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
package dk.dma.embryo.dataformats.notification;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import dk.dma.embryo.dataformats.model.InshoreIceReport;

/**
 * @author Jesper Tejlgaard
 */
public class InshoreIceReportParser {

    private BufferedReader reader;

    public InshoreIceReportParser(Reader reader) {
        if (reader instanceof BufferedReader) {
            this.reader = (BufferedReader) reader;
        } else {
            this.reader = new BufferedReader(reader);
        }
    }

    public InshoreIceReportParser(File file) throws FileNotFoundException, UnsupportedEncodingException {
        this(new InputStreamReader(new FileInputStream(file), "ISO-8859-1") );
    }

    public InshoreIceReportParser(InputStream io) throws UnsupportedEncodingException {
        this(new InputStreamReader(io, "ISO-8859-1"));
    }

    public InshoreIceReport parse() throws IOException {
        InshoreIceReport notifications = new InshoreIceReport();

        try {
            notifications = parseHeader(notifications);
            notifications = parseOverview(notifications);
            notifications = parseNotifications(notifications);
        } catch (IOException e) {
            throw new IOException("Error parsing ice notifications", e);
        }
        return notifications;
    }

    private String skipEmptyLines() throws IOException {
        String line = null;
        int count = 0;
        while (count++ < 50 && ((line = reader.readLine()) == null || line.trim().length() == 0)) {
        }
        return line;
    }

    private InshoreIceReport parseHeader(InshoreIceReport notifications) throws IOException {
        String line = skipEmptyLines();
        do {
            if (line != null && line.trim().length() > 0) {
                line = line.replaceAll("\\t", " ");
                line = line.trim();
                String copy = null;
                do {
                    copy = line;
                    line = line.replace("  ", " ");
                } while (!copy.equals(line));
                notifications.addHeader(line);
            }
        } while ((line = reader.readLine()) != null && line.trim().length() > 0);

        return notifications;
    }

    private InshoreIceReport parseOverview(InshoreIceReport notifications) throws IOException {
        String overview = "";
        String line = skipEmptyLines();
        do {
            if (line != null && line.trim().length() > 0) {
                line = line.replaceAll("\\t", " ");
                line = line.trim();
                String copy = null;
                do {
                    copy = line;
                    line = line.replace("  ", " ");
                } while (!copy.equals(line));
                overview += " " + line.trim();
            }
        } while ((line = reader.readLine()) != null && line.trim().length() > 0);
        notifications.setOverview(overview.trim());
        return notifications;
    }

    private InshoreIceReport parseNotifications(InshoreIceReport notifications) throws IOException {
        String line = skipEmptyLines();
        
        do{
            if(line.trim().length() > 0){
                int index = line.indexOf(".");
                String number = line.substring(0, index);
                
                try{
                    Integer integer = Integer.valueOf(number);
                    String desc = line.substring(index + 1).trim();
                    notifications.addNotification(integer, desc);
                }catch(NumberFormatException e){
                    notifications.addFooter(line.trim());
                }                
            }
        }while((line = reader.readLine()) != null);
        
        return notifications;
    }
}
