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
package dk.dma.embryo.dao;

import java.util.List;

import javax.ejb.Local;

import dk.dma.embryo.domain.SailorRole;
import dk.dma.embryo.domain.SecuredUser;

@Local
public interface RealmDao extends Dao {

    SecuredUser findByUsername(String username);

    SecuredUser getByPrimaryKeyReturnAll(Long key);

    List<SecuredUser> list();

    SailorRole getSailor(Long userid);
}
