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
package dk.dma.embryo.common.persistence;

import java.util.List;

public interface Dao {

    /**
     * Get entity by primary key
     * 
     * @param clazz
     * @param id
     * @return
     */
    <E extends IEntity<?>> E getByPrimaryKey(Class<E> clazz, Object id);

    /**
     * Remove entity
     * 
     * @param bean
     */
    void remove(IEntity<?> bean);

    /**
     * Save (insert or update) the entity bean
     * 
     * @param entity
     * @return entity
     */
    <E extends IEntity<?>> E saveEntity(E bean);
    
    /**
     * General purpose method to retrieve all instance of a entityType
     * @param entityType
     * @return
     */
    <E extends IEntity<?>> List<E> getAll(Class<E> entityType);


    <E extends IEntity<?>> Long count(Class<E> entityType);

}
