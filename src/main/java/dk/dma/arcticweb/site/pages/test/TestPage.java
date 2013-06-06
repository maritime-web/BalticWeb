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
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.apache.wicket.markup.html.WebPage;

import dk.dma.arcticweb.dao.RealmDao;
import dk.dma.arcticweb.dao.StakeholderDao;
import dk.dma.arcticweb.dao.UserDao;
import dk.dma.arcticweb.domain.Authority;
import dk.dma.arcticweb.domain.Ship;
import dk.dma.arcticweb.domain.Stakeholder;
import dk.dma.arcticweb.domain.User;
import dk.dma.embryo.domain.AuthorityRole;
import dk.dma.embryo.domain.Permission;
import dk.dma.embryo.domain.Sailor;
import dk.dma.embryo.domain.SecuredUser;
import dk.dma.embryo.domain.Ship2;

public class TestPage extends WebPage {

    private static final long serialVersionUID = 1L;

    @EJB
    private RealmDao realmDao;

    @EJB
    private StakeholderDao stakeholderDao;

    @EJB
    private UserDao userDao;

    public TestPage() {
        init2();
    }

    public void init() {
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

    @Inject
    private EntityManager em;

    @Inject
    UserTransaction tx;

    public void init2() {

        try {
            tx.begin();
            em.createQuery("DELETE VoyageInformation v").executeUpdate();
            em.createQuery("DELETE Ship2 s where s.name = 'ORASILA'").executeUpdate();
            em.createQuery("DELETE SecuredUser u where u.userName = 'ora'").executeUpdate();
            em.createQuery("DELETE Role r where r.logicalName = 'sailor' or r.logicalName='authority'").executeUpdate();
            em.createQuery("DELETE Permission p where p.logicalName = 'ais' or p.logicalName='yourShip'")
                    .executeUpdate();
            tx.commit();
        } catch (NotSupportedException | SystemException | SecurityException | IllegalStateException
                | RollbackException | HeuristicMixedException | HeuristicRollbackException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        // Create ship and user
        Ship2 newShip = new Ship2();
        newShip.setName("ORASILA");
        newShip.setMmsi(220443000L);

        SecuredUser user = new SecuredUser("ora", "qwerty", "obo@dma.dk");

        Permission ais = new Permission("ais");
        Permission yourShip = new Permission("yourShip");

        Sailor sailorRole = new Sailor();
        sailorRole.setShip(newShip);
        sailorRole.add(ais);
        sailorRole.add(yourShip);

        user.addRole(sailorRole);

        realmDao.saveEntity(ais);
        realmDao.saveEntity(yourShip);
        realmDao.saveEntity(newShip);
        realmDao.saveEntity(sailorRole);
        realmDao.saveEntity(user);

        // Create auth and user
        AuthorityRole auth = new AuthorityRole();
        // auth.setName(new Text("en", "Danish Maritime Authority"));
        // auth.add(ais);

        user = new SecuredUser("dma", "qwerty", "obo@dma.dk");
        user.addRole(auth);

        realmDao.saveEntity(auth);
        realmDao.saveEntity(user);

        // List<Stakeholder> stakeholders = stakeholderDao.getAll();
        // for (Stakeholder stakeholder : stakeholders) {
        // if (stakeholder instanceof Ship) {
        // Ship ship = (Ship) stakeholder;
        // System.out.println("mmsi: " + ship.getMmsi());
        // }
        // }
    }
}
