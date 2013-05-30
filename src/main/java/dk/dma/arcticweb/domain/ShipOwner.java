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
package dk.dma.arcticweb.domain;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

@Entity
public class ShipOwner extends Stakeholder {
	private static final long serialVersionUID = 1L;
	
	private Set<Ship> ships = new HashSet<Ship>();
	
	public ShipOwner() {
	}

	@OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "owner")
	public Set<Ship> getShips() {
		return ships;
	}
	
	public void setShips(Set<Ship> ships) {
		this.ships = ships;
	}

}
