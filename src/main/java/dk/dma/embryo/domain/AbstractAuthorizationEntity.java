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

import java.util.Locale;

import javax.persistence.MappedSuperclass;


@MappedSuperclass
public abstract class AbstractAuthorizationEntity<K> extends BaseEntity<K> {

    private static final long serialVersionUID = 6625306470923937976L;

    public AbstractAuthorizationEntity() {
    }

    protected AbstractAuthorizationEntity(String logicalName) {
        this.logicalName = logicalName;
    }

    // //////////////////////////////////////////////////////////////////////
    // Entity fields (also see super class)
    // //////////////////////////////////////////////////////////////////////
    private String logicalName;

    // //////////////////////////////////////////////////////////////////////
    // business logic
    // //////////////////////////////////////////////////////////////////////

    // //////////////////////////////////////////////////////////////////////
    // Utility methods
    // //////////////////////////////////////////////////////////////////////

    // //////////////////////////////////////////////////////////////////////
    // Property methods
    // //////////////////////////////////////////////////////////////////////
    public String getLogicalName() {
        return logicalName;
    }

    public void setLogicalName(String logicalName) {
        this.logicalName = logicalName;
    }

    public String toStringRaw() {
        return "id=" + id + ", logicalName=" + logicalName;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + toStringRaw() + "]";
    }
}
