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

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

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
