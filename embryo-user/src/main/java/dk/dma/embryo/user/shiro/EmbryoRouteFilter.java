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
public class EmbryoRouteFilter extends EmbryoVesselDataFilter {

    Logger logger = LoggerFactory.getLogger(EmbryoRouteFilter.class);

    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue)
            throws Exception {
        Object jsonObject = Util.getJson(request.getInputStream(), Object.class);
        String routeId = Util.getValue(jsonObject, ((String[]) mappedValue)[0]);
        
        ScheduleDao scheduleService = Configuration.getBean(ScheduleDao.class);
        Long mmsi = null;
        if(((Object[])mappedValue).length > 1){
            String voyageId = Util.getValue(jsonObject, ((String[]) mappedValue)[1]);
            mmsi = scheduleService.getMmsiByVoyageEnavId(voyageId);
        }else{
            mmsi = scheduleService.getMmsiByRouteEnavId(routeId);
        }
        
        Subject subject = Configuration.getBean(Subject.class);
        return subject.authorizedToModifyVessel(mmsi);
    }


}
