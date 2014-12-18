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
package dk.dma.embryo.user.security;

import java.util.LinkedList;
import java.util.List;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.ContextController;
import org.jglue.cdiunit.DummyHttpRequest;
import org.jglue.cdiunit.DummyHttpSession;
import org.jglue.cdiunit.InSessionScope;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import dk.dma.embryo.user.model.ReportingAuthorityRole;
import dk.dma.embryo.user.model.Role;
import dk.dma.embryo.user.model.SailorRole;
import dk.dma.embryo.user.model.SecuredUser;
import dk.dma.embryo.user.model.ShoreRole;
import dk.dma.embryo.user.persistence.RealmDao;
import dk.dma.embryo.vessel.model.Vessel;
import dk.dma.embryo.vessel.persistence.ScheduleDao;

// DummyHttpSession and DummyHttpRequest are necessary to test SessionScoped Subject
@AdditionalClasses({ SubjectImpl.class, DummyHttpSession.class, DummyHttpRequest.class })
public class SubjectTest extends AbstractShiroTest {

    @Inject
    ContextController contextController;
    
    @BeforeClass
    public static void initShiroRealm() {
        DefaultSecurityManager securityManager = new DefaultSecurityManager();
        securityManager.setRealm(new TestRealm());
        setSecurityManager(securityManager);
    }

    @Produces
    @Mock
    RealmDao realmDao;

    @Produces
    @Mock
    ScheduleDao scheduleDao;

    @Inject
    Subject subject;

    @Test
    @InSessionScope
    public void testHasOneOfRoles() {
        String user = "ole";
        String pw = "pw";
        
        //contextController.openRequest().getSession();

        Mockito.when(realmDao.findByUsername(user)).thenReturn(new SecuredUser(user, pw ,null));

        subject.login(user, pw);
        
        List<Class<? extends Role>> roleTypes = new LinkedList<>();
        roleTypes.add(SailorRole.class);

        Assert.assertTrue(subject.hasOneOfRoles(roleTypes));

        roleTypes.clear();
        roleTypes.add(ReportingAuthorityRole.class);
        Assert.assertTrue(subject.hasOneOfRoles(roleTypes));

        roleTypes.clear();
        roleTypes.add(ShoreRole.class);
        Assert.assertFalse(subject.hasOneOfRoles(roleTypes));

        roleTypes.clear();
        roleTypes.add(ShoreRole.class);
        roleTypes.add(SailorRole.class);
        roleTypes.add(ReportingAuthorityRole.class);
        Assert.assertTrue(subject.hasOneOfRoles(roleTypes));

        
        subject.logout();
    }

    @Test
    @InSessionScope
    public void testAuthorizedToModifyVessel() {
        String user = "Ole";
        String pw = "pw";
        Long mmsi = 2L;
        
        //contextController.openRequest().getSession();
        
        SailorRole sailor = new SailorRole();
        sailor.setVessel(new Vessel(mmsi));
        
        Mockito.when(realmDao.findByUsername(user)).thenReturn(new SecuredUser(user, pw ,null));
        Mockito.when(realmDao.getSailor(1L)).thenReturn(sailor);
        Mockito.when(scheduleDao.getMmsiByRouteEnavId("foo")).thenReturn(mmsi);

        subject.login(user, pw);
        
        boolean authorized = subject.authorizedToModifyVessel(mmsi);
        
        Assert.assertTrue(authorized);
    }

    
    public static class TestRealm extends AuthorizingRealm {

        public TestRealm() {
            setName("test");
        }

        /**
         * Always authorize a user
         */
        @Override
        protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authToken) {
            UsernamePasswordToken token = (UsernamePasswordToken) authToken;
            return new SimpleAuthenticationInfo(1L, new String(token.getPassword()), getName());
        }

        @Override
        protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals){
            Long userName = (Long) principals.fromRealm(getName()).iterator().next();
            if ("nopermission".equals(userName)) {
                SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
                info.addRole("role");
                return info;
            } else {
                SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
                try {
                    info.addRole(SailorRole.class.newInstance().getLogicalName());
                    info.addRole(ReportingAuthorityRole.class.newInstance().getLogicalName());
                } catch (InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                }
                return info;

            }
        }
    }
}
