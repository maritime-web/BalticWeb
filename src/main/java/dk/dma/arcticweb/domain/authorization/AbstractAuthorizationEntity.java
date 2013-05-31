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
package dk.dma.arcticweb.domain.authorization;

import java.util.Locale;

import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

import dk.dma.arcticweb.domain.BaseEntity;

@MappedSuperclass
public abstract class AbstractAuthorizationEntity<K> extends BaseEntity<K> {

    private static final long serialVersionUID = 6625306470923937976L;

    public AbstractAuthorizationEntity() {
    }

    protected AbstractAuthorizationEntity(String logicalName) {
        this.logicalName = logicalName;
    }

    protected AbstractAuthorizationEntity(String logicalName, Text name) {
        this(logicalName);
        this.name = name;
    }

    // //////////////////////////////////////////////////////////////////////
    // Entity fields (also see super class)
    // //////////////////////////////////////////////////////////////////////
    private String logicalName;

    @ManyToOne
    private Text name;

    // //////////////////////////////////////////////////////////////////////
    // business logic
    // //////////////////////////////////////////////////////////////////////

    // //////////////////////////////////////////////////////////////////////
    // Utility methods
    // //////////////////////////////////////////////////////////////////////
    public String getName(Locale locale) {
        return name.getText(locale);
    }

    // //////////////////////////////////////////////////////////////////////
    // Property methods
    // //////////////////////////////////////////////////////////////////////
    public String getLogicalName() {
        return logicalName;
    }

    public void setLogicalName(String logicalName) {
        this.logicalName = logicalName;
    }

    public Text getName() {
        return name;
    }

    public void setName(Text name) {
        this.name = name;
    }
}
