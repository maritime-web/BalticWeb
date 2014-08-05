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
