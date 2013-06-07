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

import java.util.Date;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import dk.dma.arcticweb.dao.ShipDao;
import dk.dma.arcticweb.dao.UserDao;
import dk.dma.arcticweb.domain.Ship;
import dk.dma.arcticweb.domain.VoyageInformation;
import dk.dma.embryo.domain.Sailor;
import dk.dma.embryo.domain.Ship2;
import dk.dma.embryo.domain.ShipReport2;
import dk.dma.embryo.domain.VoyageInformation2;
import dk.dma.embryo.security.Subject;
import dk.dma.embryo.security.authorization.YourShip;

@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class ShipServiceImpl implements ShipService {

    @EJB
    ShipDao shipRepository;

    @EJB
    UserDao userDao;

    @Inject
    private Subject subject;

    // TODO implement Security Interceptor for EJB methods
    @Override
    @YourShip
    public void reportForCurrentShip(ShipReport2 shipReport) {
        Ship2 ship = subject.getRole(Sailor.class).getShip();

        // TODO Should report time be modified
        shipReport.setReportTime(new Date());
        shipReport.setCreated(new Date());
        shipReport.setShip(ship);

        shipRepository.saveEntity(shipReport);
    }

    @Override
    public VoyageInformation2 getVoyageInformation(Ship2 ship) {
        ship = (Ship2) shipRepository.getByPrimaryKey(Ship2.class, ship.getId());
        VoyageInformation2 voyageInformation = ship.getVoyageInformation();
        if (voyageInformation == null) {
            voyageInformation = new VoyageInformation2();
            voyageInformation.setShip(ship);
        }
        return voyageInformation;
    }

    @Override
    public void saveVoyageInformation(VoyageInformation2 voyageInformation) {
        Ship2 ship = voyageInformation.getShip();
        shipRepository.saveEntity(ship);
    }

}
