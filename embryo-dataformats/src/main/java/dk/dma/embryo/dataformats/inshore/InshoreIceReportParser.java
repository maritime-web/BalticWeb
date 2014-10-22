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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;


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

    private InshoreIceReport parseNotifications(InshoreIceReport notifications) throws IOException {
        String line = skipEmptyLines();
        
        List<String> footer = new ArrayList<>();
        Integer previous = null;

        do{
            if(line.trim().length() > 0){
                int index = line.indexOf(".");
                if(index < 0){
                    addInfoLine(line, notifications, footer);
                } else{
                    String number = line.substring(0, index);
                    try{
                        Integer integer = Integer.valueOf(number);
                        String desc = line.substring(index + 1).trim();
                        notifications.addNotification(integer, desc);

                        // if previous notification contained a line break, then this part will be registered as a footer
                        if(previous != null && footer.size() > 0){
                            String prevDesc = notifications.getNotifications().get(previous);
                            for(String str : footer){
                                if(!prevDesc.endsWith(".")){
                                    prevDesc = prevDesc + ".";
                                }
                                prevDesc += " " + str.trim();
                            }
                            notifications.addNotification(previous, prevDesc);
                            footer.clear();
                        }

                        previous = integer;
                    }catch(NumberFormatException e){
                        addInfoLine(line, notifications, footer);
                    }
                }
            }
        }while((line = reader.readLine()) != null);
        
        for(String str : footer){
            notifications.addFooter(str);
        }
        
        return notifications;
    }

    private void addInfoLine(String line, InshoreIceReport notifications, List<String> footer){
        if(notifications.getNotifications().size() == 0) {
            if (isHeader(line)) {
                notifications.addHeader(trimLine(line));
            } else if(isOverview(line)) {
                addOverview(line, notifications);
            }
            return;
        }

        String value = line.trim();
        if(!value.startsWith("Iscentralen")){
            footer.add(line.trim());
        }
    }

    private InshoreIceReport addOverview(String line, InshoreIceReport notifications){
        String overview = notifications.getOverview() == null ? "" : notifications.getOverview();
        overview += " " + trimLine(line);
        notifications.setOverview(overview.trim());
        return notifications;
    }

    private String trimLine(String line){
        line = line.replaceAll("\\t", " ");
        line = line.trim();
        String copy = null;
        do {
            copy = line;
            line = line.replace("  ", " ");
        } while (!copy.equals(line));
        return line.trim();
    }

    private static boolean isHeader(String line){
        return line.startsWith("\t") || line.startsWith(" ");
    }

    private static boolean isOverview(String line){
        return line.length() > 0 && line.matches("[a-zA-ZæøåÆØÅ].*");
    }
}
