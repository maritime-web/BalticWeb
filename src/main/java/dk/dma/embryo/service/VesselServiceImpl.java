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
package dk.dma.embryo.service;

import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.interceptor.Interceptors;

import org.slf4j.Logger;

import dk.dma.embryo.dao.VesselDao;
import dk.dma.embryo.domain.SailorRole;
import dk.dma.embryo.domain.Vessel;
import dk.dma.embryo.security.AuthorizationChecker;
import dk.dma.embryo.security.authorization.Roles;
import dk.dma.embryo.security.authorization.RolesAllowAll;

@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Interceptors(value=AuthorizationChecker.class)
public class VesselServiceImpl implements VesselService {

    @Inject
    private VesselDao vesselRepository;

    @Inject
    private Logger logger;

    public VesselServiceImpl() {
    }

    public VesselServiceImpl(VesselDao vesselRepository) {
        this.vesselRepository = vesselRepository;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    @Roles(value=SailorRole.class)
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
    
    @Override
    @RolesAllowAll
    public Vessel getVessel(Long mmsi) {
        return vesselRepository.getVessel(mmsi);
    }

    @Override
    @RolesAllowAll
    public List<Vessel> getAll() {
        return vesselRepository.getAll(Vessel.class);
    }


}
