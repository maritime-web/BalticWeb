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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Jesper Tejlgaard
 */
public class InshoreIceReportMerged {
    
    private Date latestReportDate;
    
    private List<String> header = new ArrayList<>();

    private final Map<Integer, Observation> observations = new HashMap<>();
    
    private List<String> footer = new ArrayList<>();;
    
    // //////////////////////////////////////////////////////////////////////
    // Business Logic
    // //////////////////////////////////////////////////////////////////////
    public void mergeInReport(Date date, String source, InshoreIceReport report){
        if(latestReportDate == null || latestReportDate.getTime() < date.getTime()){
            latestReportDate = date;
        }
        
        mergeInList(footer, report.getFooters());

        for(Map.Entry<Integer, String> newObservation : report.getNotifications().entrySet()){
            if(!observations.containsKey(newObservation.getKey())){
                addObservation(newObservation.getKey(), newObservation.getValue(), date);
            }
            
            Observation obs = observations.get(newObservation.getKey());
            if(obs.getFrom().getTime() < date.getTime()){
                addObservation(newObservation.getKey(), newObservation.getValue(), date);
            }
        }
    }
    
    private void mergeInList(List<String> destination, List<String> source){
        destination.clear();
        for(String value : source){
            destination.add(value);
        }
    }
    
    
    // //////////////////////////////////////////////////////////////////////
    // Constructors methods
    // //////////////////////////////////////////////////////////////////////
    public InshoreIceReportMerged() {
        super();
        
        header.add("Danish Meteorological Institute");
        header.add("Ice patrol, Narsarsuaq");
    }
    
    // //////////////////////////////////////////////////////////////////////
    // Object methods
    // //////////////////////////////////////////////////////////////////////
    @Override
    public String toString() {
        return "InshoreIceReport [latestReportDate=" + latestReportDate + " ,header=" + header + ", observations=" + observations
                + ", footer=" + footer + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((footer == null) ? 0 : footer.hashCode());
        result = prime * result + ((header == null) ? 0 : header.hashCode());
        result = prime * result + ((latestReportDate == null) ? 0 : latestReportDate.hashCode());
        result = prime * result + ((observations == null) ? 0 : observations.hashCode());
        return result;
    }

    // //////////////////////////////////////////////////////////////////////
    // Property methods
    // //////////////////////////////////////////////////////////////////////
    public Date getLatestReportDate() {
        return latestReportDate;
    }

    public List<String> getHeader() {
        return header;
    }
    
    public void addHeader(String header) {
        this.header.add(header);
    }

    public Map<Integer, Observation> getObservations() {
        return observations;
    }

    public void addObservation(Integer number, String desc, Date from){
        observations.put(number, new Observation(from, desc));
    }
    
    public List<String> getFooter() {
        return footer;
    }
    
    public void addFooter(String value) {
        footer.add(value);
    }
    
    public static class Observation{
        private Date from;
        private String text;
        
        public Observation(Date from, String text) {
            super();
            this.from = from;
            this.text = text;
        }
        
        ///////////////////////////////
        // Object methods
        ///////////////////////////////
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((from == null) ? 0 : from.hashCode());
            result = prime * result + ((text == null) ? 0 : text.hashCode());
            return result;
        }
        
        ///////////////////////////////
        // Property methods
        ///////////////////////////////
        public Date getFrom() {
            return from;
        }

        public void setCreated(Date from) {
            this.from = from;
        }
        public String getText() {
            return text;
        }
        public void setText(String text) {
            this.text = text;
        }
        
        
        
        
    }    
}
