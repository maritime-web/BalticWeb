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
package dk.dma.embryo.user.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;

import dk.dma.embryo.common.persistence.BaseEntity;

@Entity
public class SelectionGroup extends BaseEntity<Long> {

    private static final long serialVersionUID = -8480232439011093135L;

    // //////////////////////////////////////////////////////////////////////
    // Entity fields (also see super class)
    // //////////////////////////////////////////////////////////////////////

    private String name;
    private Boolean active;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String polygonsAsJson;
    
    public SelectionGroup() { }
    
    public SelectionGroup(String name, String polygonsAsJson, Boolean active) {
		super();
		this.name = name;
		this.polygonsAsJson = polygonsAsJson;
		this.active = active;
	}

	// //////////////////////////////////////////////////////////////////////
    // Object methods
    // //////////////////////////////////////////////////////////////////////
    @Override
    public String toString() {
        return "SecuredUser [name=" + name + ", id=" + id + ", active=" + active + "]";
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

	public Boolean getActive() {
		return active;
	}
	public void setActive(Boolean active) {
		this.active = active;
	}

	public String getPolygonsAsJson() {
		return polygonsAsJson;
	}
	public void setPolygonsAsJson(String polygonsAsJson) {
		this.polygonsAsJson = polygonsAsJson;
	}
}