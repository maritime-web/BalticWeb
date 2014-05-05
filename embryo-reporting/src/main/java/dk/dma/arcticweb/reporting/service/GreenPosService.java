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
package dk.dma.arcticweb.reporting.service;

import java.util.List;

import javax.ejb.Local;

import dk.dma.arcticweb.reporting.model.GreenPosReport;
import dk.dma.arcticweb.reporting.model.GreenposMinimal;
import dk.dma.arcticweb.reporting.model.GreenposSearch;

@Local
public interface GreenPosService{

    List<GreenPosReport> listReports();

    GreenPosReport getLatest(Long vesselMmsi);

    List<GreenposMinimal> getLatest();

    String saveReport(GreenPosReport report, String routeEnavId, Boolean activate, Boolean includeActiveWayPoints, String[] recipients);
    
    List<GreenPosReport> findReports(GreenposSearch search);

    GreenPosReport get(String id);

}
