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
package dk.dma.arcticweb.service;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.slf4j.Logger;

import dk.dma.arcticweb.dao.RealmDao;
import dk.dma.arcticweb.dao.VesselDao;
import dk.dma.embryo.domain.Sailor;
import dk.dma.embryo.domain.Vessel;
import dk.dma.embryo.security.Subject;
import dk.dma.embryo.security.authorization.YourShip;

@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class VesselServiceImpl implements VesselService {

    @Inject
    private VesselDao vesselRepository;

    @Inject
    private RealmDao realmDao;

    @Inject
    private Subject subject;

    @Inject
    private Logger logger;

    public VesselServiceImpl() {
    }

    public VesselServiceImpl(VesselDao vesselRepository) {
        this.vesselRepository = vesselRepository;
    }

    @Override
    @YourShip
    public void save(Vessel vessel) {
        Vessel managed = vesselRepository.getVessel(vessel.getMmsi());

        if (managed != null) {
            // copying all values to managed entity to avoid resetting JPA association fields.

            managed.setMmsi(vessel.getMmsi());
            managed.setCommCapabilities(vessel.getCommCapabilities());
            managed.setHelipad(vessel.getHelipad());
            managed.setMaxSpeed(vessel.getMaxSpeed());
            managed.setPersons(vessel.getPersons());
            managed.setGrossTonnage(vessel.getGrossTonnage());

            managed = vesselRepository.saveEntity(managed);
        } else {
            vessel = vesselRepository.saveEntity(vessel);
        }
    }
    
    public Vessel getYourVessel() {
        if (subject.hasRole(Sailor.class)) {
            Sailor sailor = realmDao.getSailor(subject.getUserId());
            return sailor.getVessel();
        }
        return new Vessel();
    }

    @Override
    public Vessel getVessel(Long mmsi) {
        return vesselRepository.getVessel(mmsi);
    }

    
}
