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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Jesper Tejlgaard
 */
public class InshoreIceReport {
    
    private List<String> headers = new ArrayList<>();
    private String overview;
    private final Map<Integer, String> notifications = new HashMap<>();
    
    private List<String> footers = new ArrayList<>();;
    
    
    // //////////////////////////////////////////////////////////////////////
    // Constructors methods
    // //////////////////////////////////////////////////////////////////////
    public InshoreIceReport() {
        super();
    }
    
    // //////////////////////////////////////////////////////////////////////
    // Object methods
    // //////////////////////////////////////////////////////////////////////
    @Override
    public String toString() {
        return "InshoreIceReport [headers=" + headers + ", overview=" + overview + ", notifications=" + notifications
                + ", footers=" + footers + "]";
    }

    // //////////////////////////////////////////////////////////////////////
    // Property methods
    // //////////////////////////////////////////////////////////////////////
    public List<String> getHeader() {
        return headers;
    }
    
    public void addHeader(String header) {
        this.headers.add(header);
    }

    public Map<Integer, String> getNotifications() {
        return notifications;
    }

    public void addNotification(Integer number, String desc){
        notifications.put(number, desc);
    }
    
    public List<String> getFooters() {
        return footers;
    }
    
    public void addFooter(String footer) {
        footers.add(footer);
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }    
}
