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

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;


@Entity
@NamedQueries({@NamedQuery(name="VoyagePlan:getByMmsi", query="SELECT DISTINCT v FROM VoyagePlan v LEFT JOIN FETCH v.voyages where v.ship.mmsi = :mmsi")})
public class VoyagePlan extends BaseEntity<Long> {

    private static final long serialVersionUID = 1L;

    // //////////////////////////////////////////////////////////////////////
    // Entity fields (also see super class)
    // //////////////////////////////////////////////////////////////////////
    @Column(unique = false, nullable = true)
    private Integer personsOnboard;

    @Column(unique = false, nullable = true)
    private Boolean doctorOnboard;

    @OneToOne(optional = false)
    Ship2 ship;
    
    @OneToMany(cascade={CascadeType.ALL}, mappedBy="plan")
    @OrderBy("departure")
    private List<Voyage> voyages = new LinkedList<>(); 

    // //////////////////////////////////////////////////////////////////////
    // Utility methods
    // //////////////////////////////////////////////////////////////////////
    public void addVoyageEntry(Voyage entry){
        voyages.add(entry);
        entry.plan = this;
    }
    
    public Map<String, Voyage> getVoyagePlanAsMap(){
        Map<String, Voyage> m = new HashMap<>();
        for(Voyage v : voyages){
            m.put(v.getEnavId(), v);
        }
        return m;
    }
    
    public void removeLastVoyage(){
        voyages.remove(voyages.size()-1).plan = null;
    }
    

    public void removeVoyage(Voyage v){
        v.plan = null;
        voyages.remove(v);
    }

    
    @Override
    public String toString() {
        return "VoyagePlan [personsOnboard=" + personsOnboard + ", doctorOnboard=" + doctorOnboard
                + ", voyagePlan=" + voyages + "]";
    }


    // //////////////////////////////////////////////////////////////////////
    // Constructors
    // //////////////////////////////////////////////////////////////////////
    public VoyagePlan() {

    }

    public VoyagePlan(Integer personsOnboard, boolean doctorOnboard) {
        this.personsOnboard = personsOnboard;
        this.doctorOnboard = doctorOnboard;
    }

    // //////////////////////////////////////////////////////////////////////
    // Property methods
    // //////////////////////////////////////////////////////////////////////
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
    
    public List<Voyage> getVoyagePlan(){
        return Collections.unmodifiableList(voyages);
    }
}
