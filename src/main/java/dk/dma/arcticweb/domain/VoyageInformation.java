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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity
public class VoyageInformation extends AbstractEntity {
	private static final long serialVersionUID = 1L;

	private Integer personsOnboard;
	private Boolean doctorOnboard;
	private Ship ship;

	public VoyageInformation() {

	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(unique = true, nullable = false)
	@Override
	public Integer getId() {
		return id;
	}

	@Column(unique = false, nullable = true)
	public Integer getPersonsOnboard() {
		return personsOnboard;
	}

	public void setPersonsOnboard(Integer personsOnboard) {
		this.personsOnboard = personsOnboard;
	}

	@Column(unique = false, nullable = true)
	public Boolean getDoctorOnboard() {
		return doctorOnboard;
	}

	public void setDoctorOnboard(Boolean doctorOnboard) {
		this.doctorOnboard = doctorOnboard;
	}

	@OneToOne(optional = false)
	public Ship getShip() {
		return ship;
	}

	public void setShip(Ship ship) {
		this.ship = ship;
	}

}
