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
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptors;
import javax.interceptor.InvocationContext;

import org.apache.shiro.authz.AuthorizationException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import dk.dma.arcticweb.dao.GreenPosDao;
import dk.dma.arcticweb.report.ReportMail;
import dk.dma.embryo.common.configuration.PropertyFileService;
import dk.dma.embryo.common.mail.MailSender;
import dk.dma.embryo.dao.RealmDao;
import dk.dma.embryo.domain.GreenPosDeviationReport;
import dk.dma.embryo.domain.GreenPosReport;
import dk.dma.embryo.domain.GreenPosSailingPlanReport;
import dk.dma.embryo.domain.GreenposMinimal;
import dk.dma.embryo.domain.GreenposSearch;
import dk.dma.embryo.domain.ReportedRoute;
import dk.dma.embryo.domain.SailorRole;
import dk.dma.embryo.security.AuthorizationChecker;
import dk.dma.embryo.security.Subject;
import dk.dma.embryo.security.authorization.Roles;
import dk.dma.embryo.security.authorization.RolesAllowAll;
import dk.dma.embryo.vessel.component.RouteActivator;
import dk.dma.embryo.vessel.model.Route;
import dk.dma.embryo.vessel.model.Vessel;
import dk.dma.embryo.vessel.persistence.ScheduleDao;
import dk.dma.embryo.vessel.persistence.VesselDao;

@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Interceptors(value=AuthorizationChecker.class)
@RolesAllowAll
public class GreenPosServiceImpl implements GreenPosService {

    @Inject
    private GreenPosDao greenPosDao;

    @Inject
    private MailSender mailSender;

    @Inject
    private Subject subject;

    @Inject
    private VesselDao vesselDao;

    @Inject
    private ScheduleDao scheduleDao;
    
    @Inject 
    private PropertyFileService propertyFileService;

    @Inject
    private RealmDao realmDao;

    public GreenPosServiceImpl() {
    }

    public GreenPosServiceImpl(GreenPosDao reportingDao, VesselDao vesselDao, Subject subject,
            RealmDao realmDao, MailSender mailSender, ScheduleDao scheduleDao) {
        this.greenPosDao = reportingDao;
        this.vesselDao = vesselDao;
        this.subject = subject;
        this.realmDao = realmDao;
        this.mailSender = mailSender;
        this.scheduleDao = scheduleDao;
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
    @Roles(SailorRole.class)
    @Interceptors(SaveReportInterceptor.class)
    public String saveReport(GreenPosReport report, String routeEnavId, Boolean activate, Boolean includeActiveWaypoints) {
        
        checkIfAlreadySaved(report);

        Vessel vessel = null;

        if (subject.hasRole(SailorRole.class)) {
            vessel = realmDao.getSailor(subject.getUserId()).getVessel();
            validateVesselData(report, vessel);
        } else {
            vessel = getVesselFromReport(report);
        }

        report.setReportedBy(subject.getUser().getUserName());
        report.setTs(DateTime.now(DateTimeZone.UTC));

        if(includeActiveWaypoints != null && includeActiveWaypoints.booleanValue()){
            Route route = scheduleDao.getActiveRoute(report.getVesselMmsi());
            
            if(route == null){
                throw new IllegalArgumentException("Active route with enavId " + routeEnavId + " not found.");
            }
            
            ReportedRoute reported = ReportedRoute.fromModel(route);
            
            if(report instanceof GreenPosSailingPlanReport){
                ((GreenPosSailingPlanReport)report).setRoute(reported);
            }
            if(report instanceof GreenPosDeviationReport){
                ((GreenPosDeviationReport)report).setRoute(reported);
            }
        }
        
        report = greenPosDao.saveEntity(report);
        
        if(routeEnavId != null && activate != null){
            new RouteActivator(scheduleDao).activateRoute(routeEnavId, activate);
        }

        mailSender.sendEmail(new ReportMail(report, subject.getUser().getEmail(), propertyFileService));

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
        if (vessel.getAisData().getCallsign() != null
                && !vessel.getAisData().getCallsign().equals(report.getVesselCallSign())) {
            throw new IllegalArgumentException(
                    "Reported vessel call sign must match the call sign of the users vessel.");
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
        Vessel vessel = vesselDao.getVessel(report.getVesselMmsi());
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
    public GreenPosReport getLatest(Long vesselMmsi) {
        return greenPosDao.findLatest(vesselMmsi);
    }

    @Override
    public List<GreenposMinimal> getLatest() {
        return greenPosDao.getLatest();
    }

    @Override
    public GreenPosReport get(String id) {
        return greenPosDao.findById(id);
    }

    public static class SaveReportInterceptor{
        
        @Inject
        private Subject subject;
        
        @AroundInvoke
        Object onlyForOwnVessel(InvocationContext ctx) throws Exception{
            GreenPosReport report = (GreenPosReport)ctx.getParameters()[0];
            if(report.getVesselMmsi() == null || !subject.authorizedToModifyVessel(report.getVesselMmsi())){
                throw new AuthorizationException("Not authorized to submit GreenposReports for vessel");
            }
            
            return ctx.proceed();
        }
    }


}
