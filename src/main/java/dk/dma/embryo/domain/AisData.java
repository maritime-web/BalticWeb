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
package dk.dma.embryo.domain;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * @author Jesper Tejlgaard
 */
@Embeddable
public class AisData implements Serializable{
    
    private static final long serialVersionUID = -1013693596958206861L;

    @Column(nullable = true, length = 128)
    private String name;
    
    @Column(nullable = true, length = 32)
    private String callsign;

    @Column(nullable = true)
    private Long imoNo;

    @Column(nullable = true)
    private Integer width;

    @Column(nullable = true)
    private Integer length;

    // //////////////////////////////////////////////////////////////////////
    // Utility methods
    // //////////////////////////////////////////////////////////////////////
    Map<String, Object> toJsonModel(){
        Map<String, Object> map = new HashMap<>();
        
        map.put("name", name);
        map.put("callsign", callsign);
        map.put("imoNo", imoNo);
        map.put("width", width);
        map.put("length", length);
        
        return map;
    }
    
    // //////////////////////////////////////////////////////////////////////
    // Property methods
    // //////////////////////////////////////////////////////////////////////
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCallsign() {
        return callsign;
    }

    public void setCallsign(String callsign) {
        this.callsign = callsign;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public Long getImoNo() {
        return imoNo;
    }

    public void setImoNo(Long imoNo) {
        this.imoNo = imoNo;
    }
}
