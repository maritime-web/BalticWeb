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
package dk.dma.embryo.security;

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
import org.jglue.cdiunit.DummyHttpRequest;
import org.jglue.cdiunit.DummyHttpSession;
import org.jglue.cdiunit.InSessionScope;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import dk.dma.embryo.dao.RealmDao;
import dk.dma.embryo.domain.ReportingAuthorityRole;
import dk.dma.embryo.domain.Role;
import dk.dma.embryo.domain.SailorRole;
import dk.dma.embryo.domain.SecuredUser;
import dk.dma.embryo.domain.ShoreRole;

// DummyHttpSession and DummyHttpRequest are necessary to test SessionScoped Subject
@AdditionalClasses({ SubjectImpl.class, DummyHttpSession.class, DummyHttpRequest.class })
public class SubjectTest extends AbstractShiroTest {

    private static final String PERMITTED_PERMISSION = "permission";

    @BeforeClass
    public static void initShiroRealm() {
        DefaultSecurityManager securityManager = new DefaultSecurityManager();
        securityManager.setRealm(new TestRealm());
        setSecurityManager(securityManager);
    }

    @Produces
    @Mock
    RealmDao realmDao;

    @Inject
    Subject subject;

    @Test
    @InSessionScope
    public void testHasOneOfRoles() {
        String user = "ole";
        String pw = "pw";

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
            return new SimpleAuthenticationInfo(token.getUsername(), new String(token.getPassword()), getName());
        }

        @Override
        protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals){
            
            
            String userName = (String) principals.fromRealm(getName()).iterator().next();
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
//                info.addStringPermissions(Arrays.asList("never", "really", "used"));

//                info.addStringPermissions(Arrays.asList("dummy", PERMITTED_PERMISSION));
                return info;

            }
        }
    }
}
