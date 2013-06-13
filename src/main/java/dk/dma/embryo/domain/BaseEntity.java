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

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

/**
 * Base class for all entity beans
 */
@MappedSuperclass
public abstract class BaseEntity<K> implements IEntity<K> {

    private static final long serialVersionUID = 2387085281343623228L;

    // TABLE strategy necessary to use id in equals and hashCode. Otherwise id is not available for newly created
    // objects yet not persisted in database (which is done when transaction committed).
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    @Column(unique = true, nullable = false)
    protected K id;

    public K getId() {
        return this.id;
    }

    public boolean isNew() {
        return getId() == null;
    }

    public boolean isPersisted() {
        return !isNew();
    }

    /**
     * Hash code is based on entity id for all Embryonic Entities.
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    /**
     * equals is based on entity id for all Embryonic Entities. This means equals is NOT behaving as normally, where
     * equals is based on object state (all fields).
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        
        BaseEntity<K> other = (BaseEntity<K>) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }

}
