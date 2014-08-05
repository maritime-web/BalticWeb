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
package dk.dma.embryo.dataformats.model;

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
