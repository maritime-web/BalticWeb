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
package dk.dma.embryo.dataformats.model;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;

import dk.dma.embryo.common.persistence.BaseEntity;

/**
 * The actual forecast data. Will not be loaded until a specific forecast has
 * been requested.
 * 
 * @author avlund
 *
 */
@Entity
public class ForecastData extends BaseEntity<Long> {

    private static final long serialVersionUID = 4455436663950833441L;

    @Lob
    @Column(columnDefinition = "mediumtext")
    @Basic(fetch = FetchType.LAZY)
    private String json;

    ForecastData() {
    }

    ForecastData(String json) {
        this.json = json;
    }

    public String getJson() {
        return json;
    }

}
