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
package dk.dma.embryo.weather.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Jesper Tejlgaard
 */
public class Warnings {

    private Integer number;
    private Date from;

    private Map<String, String> gale = new HashMap<>();
    private Map<String, String> storm = new HashMap<>();
    private Map<String, String> icing = new HashMap<>();

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Date getFrom() {
        return from;
    }

    public void setFrom(Date from) {
        this.from = from;
    }

    public Map<String, String> getGale() {
        return gale;
    }

    public void setGale(Map<String, String> gale) {
        this.gale = gale;
    }

    public Map<String, String> getStorm() {
        return storm;
    }

    public void setStorm(Map<String, String> storm) {
        this.storm = storm;
    }

    public Map<String, String> getIcing() {
        return icing;
    }

    public void setIce(Map<String, String> icing) {
        this.icing = icing;
    }

    
}
