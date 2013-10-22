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

import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDateTime;

import dk.dma.arcticweb.dao.GreenPosDao;
import dk.dma.arcticweb.dao.VesselDao;
import dk.dma.embryo.domain.GreenPosDeviationReport;
import dk.dma.embryo.domain.GreenPosReport;
import dk.dma.embryo.domain.GreenPosSailingPlanReport;
import dk.dma.embryo.domain.GreenposSearch;
import dk.dma.embryo.domain.Sailor;
import dk.dma.embryo.domain.Vessel;
import dk.dma.embryo.security.Subject;
import dk.dma.embryo.security.authorization.YourShip;

@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class GreenPosServiceImpl implements GreenPosService {

    @Inject
    private GreenPosDao greenPosDao;

    @Inject
    private Subject subject;

    @Inject
    private VesselService vesselService;

    @Inject
    private VesselDao vesselDao;

    public GreenPosServiceImpl() {
    }

    public GreenPosServiceImpl(GreenPosDao reportingDao, VesselDao vesselDao, Subject subject, VesselService vesselservice) {
        this.greenPosDao = reportingDao;
        this.vesselDao = vesselDao;
        this.subject = subject;
        this.vesselService = vesselservice;
    }
    
    @Override
    public List<GreenPosReport> listReports() {
        // TODO Should current vessel only be able to list reports for own vessel ?
        return greenPosDao.getAll(GreenPosReport.class);
    }

    /**
     * This saves a report coming from a vessel
     */
    @Override
    @YourShip
    public String saveReport(GreenPosReport report) {
        checkIfAlreadySaved(report);

        Vessel vessel = null;

        if (subject.hasRole(Sailor.class)) {
            vessel = vesselService.getYourVessel();
            validateVesselData(report, vessel);
        } else {
            vessel = getVesselFromReport(report);
        }

        if(report instanceof GreenPosSailingPlanReport){
            GreenPosSailingPlanReport spReport = (GreenPosSailingPlanReport)report;
            report = spReport.withVoyages(vessel.getVoyagePlan().getVoyagePlan());
        }else if(report instanceof GreenPosDeviationReport){
            GreenPosDeviationReport spReport = (GreenPosDeviationReport)report;            
        }
        
        report.setReportedBy(subject.getUser().getUserName());
        report.setTs(LocalDateTime.now());
        
        greenPosDao.saveEntity(report);

        return report.getEnavId();
    }

    private void checkIfAlreadySaved(GreenPosReport report) {
        if (report.getId() != null) {
            throw new IllegalArgumentException("Report is already saved in the system. It can not be updated");
        }
    }

    private void validateVesselData(GreenPosReport report, Vessel vessel) {
        // If report is send by sailor, then validate, that he is reporting on behalf of his own vessel
        // Validation if his vessel name is still not registered in the system.
        if (vessel.getAisData().getCallsign() != null && !vessel.getAisData().getCallsign().equals(report.getVesselCallSign())) {
            throw new IllegalArgumentException("Reported vessel call sign must match the call sign of the users vessel.");
        }

        if (vessel.getMmsi() != null && !vessel.getMmsi().equals(report.getVesselMmsi())) {
            throw new IllegalArgumentException("Reported vessel Mmsi must match the call sign of the users vessel.");
        }

        // Validation skipped if his vessel name is still not registered in the system.
        if (vessel.getAisData().getName() != null && !vessel.getAisData().getName().equals(report.getVesselName())) {
            throw new IllegalArgumentException("Reported vessel name must match the call sign of the users vessel");
        }
    }

    private Vessel getVesselFromReport(GreenPosReport report) {
        Vessel vessel = null;
        if (StringUtils.isNotBlank(report.getVesselMaritimeId())) {
            vessel = vesselDao.getVesselByMaritimeId(report.getVesselMaritimeId());
        }

        if (vessel == null && StringUtils.isNotBlank(report.getVesselCallSign())) {
            vessel = vesselDao.getVesselByCallsign(report.getVesselCallSign());
        }
        if (vessel == null) {
            // TODO relax this. it should be possible to save report in any case, but with below message as a report
            // comment.
            throw new IllegalArgumentException("Could not identify vessel from report data.");
        }
        return vessel;
    }

    @Override
    public List<GreenPosReport> findReports(GreenposSearch search) {
        return greenPosDao.find(search);
    }

    @Override
    public GreenPosReport getLatest(String vesselMaritimeId) {
        return greenPosDao.findLatest(vesselMaritimeId);
    }

    @Override
    public GreenPosReport get(String id) {
        return greenPosDao.findById(id);
    }
    
    
}
