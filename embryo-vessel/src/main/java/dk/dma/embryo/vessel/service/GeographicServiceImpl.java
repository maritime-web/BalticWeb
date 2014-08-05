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

import dk.dma.embryo.vessel.model.Berth;
import dk.dma.embryo.vessel.persistence.GeographicDao;

@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class GeographicServiceImpl implements GeographicService {

    @Inject
    private GeographicDao geoDao;

    public GeographicServiceImpl() {
    }
    
    public GeographicServiceImpl(GeographicDao geoDao) {
        this.geoDao = geoDao;
    }

    @Override
    public List<Berth> findBerths(String query) {
        return geoDao.findBerths(query);
    }

}
