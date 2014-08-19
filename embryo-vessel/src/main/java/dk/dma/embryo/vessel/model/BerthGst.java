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
package dk.dma.embryo.vessel.model;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import dk.dma.embryo.common.persistence.BaseEntity;

@Entity
@Table(name = "berth_gst")
public class BerthGst extends BaseEntity<Long> {

    private static final long serialVersionUID = -7720878907095105915L;

    // //////////////////////////////////////////////////////////////////////
    // Entity fields (also see super class)
    // //////////////////////////////////////////////////////////////////////
    @NotNull
    private String name;

    private String latitude;

    private String longitude;

    // //////////////////////////////////////////////////////////////////////
    // business logic
    // //////////////////////////////////////////////////////////////////////

    // //////////////////////////////////////////////////////////////////////
    // Utility methods
    // //////////////////////////////////////////////////////////////////////

    // //////////////////////////////////////////////////////////////////////
    // Constructors
    // //////////////////////////////////////////////////////////////////////
    public BerthGst() {
    }

    // //////////////////////////////////////////////////////////////////////
    // Property methods
    // //////////////////////////////////////////////////////////////////////
    public String getName() {
        String[] parts = name.split("\\(");
        return parts[0].trim();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlias() {
        String[] parts = name.split("\\(");
        return parts.length > 1 ? parts[1].replaceAll("\\)", "").trim() : null;
    }

    public String getLatitude() {
        return getValue(latitude);
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return getValue(longitude);
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    private String getValue(String value) {
        return value.replace("º", " ").replaceAll(",", ".").replaceAll("´", "");
    }

    // //////////////////////////////////////////////////////////////////////
    // Object methods
    // //////////////////////////////////////////////////////////////////////
    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

    public String asBerthConstructor() {
        return "new Berth(" + asStringLiteral(getName()) + ", " + asStringLiteral(getAlias()) + ", "
                + asStringLiteral(getLatitude()) + ", " + asStringLiteral(getLongitude()) + ")";
    }

    private String asStringLiteral(String value) {
        return value == null ? null : "\"" + value + "\"";
    }

}
