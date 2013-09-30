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
import org.slf4j.Logger;

import dk.dma.arcticweb.dao.GreenPosDao;
import dk.dma.arcticweb.dao.ShipDao;
import dk.dma.embryo.domain.GreenPosDeviationReport;
import dk.dma.embryo.domain.GreenPosReport;
import dk.dma.embryo.domain.GreenPosSailingPlanReport;
import dk.dma.embryo.domain.GreenposSearch;
import dk.dma.embryo.domain.Sailor;
import dk.dma.embryo.domain.Ship;
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
    private ShipService shipService;

    @Inject
    private ShipDao shipDao;

    public GreenPosServiceImpl() {
    }

    public GreenPosServiceImpl(GreenPosDao reportingDao, ShipDao shipDao, Subject subject, ShipService shipservice) {
        this.greenPosDao = reportingDao;
        this.shipDao = shipDao;
        this.subject = subject;
        this.shipService = shipservice;
    }
    
    @Override
    public List<GreenPosReport> listReports() {
        // TODO Should current ship only be able to list reports for own ship ?
        return greenPosDao.getAll(GreenPosReport.class);
    }

    /**
     * This saves a report coming from a ship
     */
    @Override
    @YourShip
    public String saveReport(GreenPosReport report) {
        checkIfAlreadySaved(report);

        Ship ship = null;

        if (subject.hasRole(Sailor.class)) {
            ship = shipService.getYourShip();
            validateShipData(report, ship);
        } else {
            ship = getShipFromReport(report);
        }

        if(report instanceof GreenPosSailingPlanReport){
            GreenPosSailingPlanReport spReport = (GreenPosSailingPlanReport)report;
            report = spReport.withVoyages(ship.getVoyagePlan().getVoyagePlan());
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

    private void validateShipData(GreenPosReport report, Ship ship) {
        // If report is send by sailor, then validate, that he is reporting on behalf of his own ship
        // Validation if his ship name is still not registered in the system.
        if (ship.getCallsign() != null && !ship.getCallsign().equals(report.getShipCallSign())) {
            throw new IllegalArgumentException("Reported ship call sign must match the call sign of the users ship.");
        }

        if (ship.getMmsi() != null && !ship.getMmsi().equals(report.getShipMmsi())) {
            throw new IllegalArgumentException("Reported ship Mmsi must match the call sign of the users ship.");
        }

        // Validation skipped if his ship name is still not registered in the system.
        if (ship.getName() != null && !ship.getName().equals(report.getShipName())) {
            throw new IllegalArgumentException("Reported ship name must match the call sign of the users ship");
        }
    }

    private Ship getShipFromReport(GreenPosReport report) {
        Ship ship = null;
        if (StringUtils.isNotBlank(report.getShipMaritimeId())) {
            ship = shipDao.getShipByMaritimeId(report.getShipMaritimeId());
        }

        if (ship == null && StringUtils.isNotBlank(report.getShipCallSign())) {
            ship = shipDao.getShipByCallsign(report.getShipCallSign());
        }
        if (ship == null) {
            // TODO relax this. it should be possible to save report in any case, but with below message as a report
            // comment.
            throw new IllegalArgumentException("Could not identify ship from report data.");
        }
        return ship;
    }

    @Override
    public List<GreenPosReport> findReports(GreenposSearch search) {
        return greenPosDao.find(search);
    }

    @Override
    public GreenPosReport getLatest(String shipMaritimeId) {
        return greenPosDao.findLatest(shipMaritimeId);
    }

    @Override
    public GreenPosReport get(String id) {
        return greenPosDao.findById(id);
    }
    
    
}
