/* Copyright (c) 2011 Danish Maritime Authority.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dk.dma.embryo.vessel.service;

import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import dk.dma.embryo.vessel.model.Vessel;
import dk.dma.embryo.vessel.persistence.VesselDao;

/**
 * The Class VesselServiceImpl.
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class VesselServiceImpl implements VesselService {

    /** The vessel repository. */
    @Inject
    private VesselDao vesselRepository;

    /**
     * Instantiates a new vessel service impl.
     */
    public VesselServiceImpl() {
    }

    /**
     * Instantiates a new vessel service impl.
     *
     * @param vesselRepository the vessel repository
     */
    public VesselServiceImpl(VesselDao vesselRepository) {
        this.vesselRepository = vesselRepository;
    }

    /* (non-Javadoc)
     * @see dk.dma.embryo.vessel.service.VesselService#save(dk.dma.embryo.vessel.model.Vessel)
     */
    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
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
    
    /* (non-Javadoc)
     * @see dk.dma.embryo.vessel.service.VesselService#getVessel(java.lang.Long)
     */
    @Override
    public Vessel getVessel(Long mmsi) {
        return vesselRepository.getVessel(mmsi);
    }

    /* (non-Javadoc)
     * @see dk.dma.embryo.vessel.service.VesselService#getAll()
     */
    @Override
    public List<Vessel> getAll() {
        return vesselRepository.getAll(Vessel.class);
    }


}
