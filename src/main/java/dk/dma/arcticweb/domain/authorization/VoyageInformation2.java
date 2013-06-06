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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

import dk.dma.arcticweb.domain.BaseEntity;

@Entity
public class VoyageInformation2 extends BaseEntity<Long> {
    private static final long serialVersionUID = 1L;

    @Column(unique = false, nullable = true)
    private Integer personsOnboard;

    @Column(unique = false, nullable = true)
    private Boolean doctorOnboard;

    @OneToOne(optional = false)
    private Ship2 ship;

    public VoyageInformation2() {

    }

    public Integer getPersonsOnboard() {
        return personsOnboard;
    }

    public void setPersonsOnboard(Integer personsOnboard) {
        this.personsOnboard = personsOnboard;
    }

    public Boolean getDoctorOnboard() {
        return doctorOnboard;
    }

    public void setDoctorOnboard(Boolean doctorOnboard) {
        this.doctorOnboard = doctorOnboard;
    }

    public Ship2 getShip() {
        return ship;
    }

    public void setShip(Ship2 ship) {
        // poor mans referential integrity
        this.ship = ship;
        ship.voyageInformation = this;
    }

}
