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
package dk.dma.embryo.user.shiro;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.dma.embryo.common.configuration.Configuration;
import dk.dma.embryo.user.security.Subject;
import dk.dma.embryo.vessel.persistence.ScheduleDao;

/**
 * @author Jesper Tejlgaard
 */
public class EmbryoRouteUploadFilter extends EmbryoVesselDataFilter {

    Logger logger = LoggerFactory.getLogger(EmbryoRouteUploadFilter.class);

    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue)
            throws Exception {

        // NOT WORKING. voyageId is not a POST parameter
        String voyageId = request.getParameter("voyageId");
        logger.debug("voyageId={}", voyageId);
        ScheduleDao scheduleService = Configuration.getBean(ScheduleDao.class);
        Long mmsi = scheduleService.getMmsiByVoyageEnavId(voyageId);

        Subject subject = Configuration.getBean(Subject.class);
        return subject.authorizedToModifyVessel(mmsi);
    }
}
