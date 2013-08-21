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

import dk.dma.arcticweb.dao.GeographicDao;
import dk.dma.embryo.domain.Berth;

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
//        if(query == null || query.length() == 0){
//            return Collections.emptyList();
//        }
        return geoDao.findBerths(query);
    }

}
