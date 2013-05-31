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
package dk.dma.arcticweb.site.pages.test;

import java.util.List;

import javax.ejb.EJB;

import org.apache.wicket.markup.html.WebPage;

import dk.dma.arcticweb.dao.StakeholderDao;
import dk.dma.arcticweb.dao.UserDao;
import dk.dma.arcticweb.domain.Authority;
import dk.dma.arcticweb.domain.Ship;
import dk.dma.arcticweb.domain.Stakeholder;
import dk.dma.arcticweb.domain.User;

public class TestPage extends WebPage {

    private static final long serialVersionUID = 1L;

    @EJB
    private StakeholderDao stakeholderDao;
    @EJB
    private UserDao userDao;

    public TestPage() {
        // Create ship and user
        Ship newShip = new Ship();
        newShip.setName("ORASILA");
        newShip.setMmsi(220443000L);
        User user = new User();
        user.setUsername("ora");
        user.setPassword("qwerty");
        user.setEmail("obo@dma.dk");
        user.setStakeholder(newShip);
        newShip.getUsers().add(user);
        stakeholderDao.saveEntity(newShip);
        userDao.saveEntity(user);

        // Create auth and user
        Authority auth = new Authority();
        auth.setName("Danish Maritime Authority");
        user = new User();
        user.setUsername("dma");
        user.setPassword("qwerty");
        user.setEmail("obo@dma.dk");
        user.setStakeholder(auth);
        auth.getUsers().add(user);
        stakeholderDao.saveEntity(auth);
        userDao.saveEntity(user);

        List<Stakeholder> stakeholders = stakeholderDao.getAll();
        for (Stakeholder stakeholder : stakeholders) {
            if (stakeholder instanceof Ship) {
                Ship ship = (Ship) stakeholder;
                System.out.println("mmsi: " + ship.getMmsi());
            }
        }

    }

}
