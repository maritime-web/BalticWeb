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
package dk.dma.arcticweb.dao;

import dk.dma.arcticweb.domain.IEntity;

public interface Dao {

    /**
     * Get entity by primary key
     * 
     * @param clazz
     * @param id
     * @return
     */
    IEntity getByPrimaryKey(Class<? extends IEntity> clazz, Object id);

    /**
     * Remove entity
     * 
     * @param bean
     */
    void remove(IEntity bean);

    /**
     * Save (insert or update) the entity bean
     * 
     * @param entity
     * @return entity
     */
    IEntity saveEntity(IEntity bean);

}
