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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.interceptor.InvocationContext;

import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;

import dk.dma.embryo.config.Configuration;
import dk.dma.embryo.config.LogConfiguration;
import dk.dma.embryo.domain.Role;
import dk.dma.embryo.domain.SailorRole;
import dk.dma.embryo.domain.SecuredUser;
import dk.dma.embryo.security.authorization.Roles;

/**
 * @author Jesper Tejlgaard
 */
@RunWith(CdiRunner.class)
@AdditionalClasses({ Configuration.class, LogConfiguration.class })
public class AuthorizationCheckerTest {

    @Produces
    @Mock
    Subject subject;

    @Produces
    @Mock
    InvocationContext context;

    @Inject
    AuthorizationChecker checker;

    @Before
    public void setup() {

    }

    static public class Foo {
        public Foo(){
        }
        @Roles(SailorRole.class)
        public void doSomething() {
        }
    }

    @Test
    public void test() {
        // SETUP MOCKS
        Mockito.when(subject.isLoggedIn()).thenReturn(true);
        Mockito.when(subject.getUser()).thenReturn(new SecuredUser("Ole", "bole", null));
        Mockito.when(subject.hasRole(SailorRole.class)).thenReturn(true);
        
        List<Class<? extends Role>> types = new ArrayList<>();
        types.add(SailorRole.class);
        Mockito.when(subject.hasOneOfRoles(types)).thenReturn(true);

        try {
            Method method = Foo.class.getMethod("doSomething");
            Mockito.when(context.getMethod()).thenReturn(method);
            Mockito.when(context.getTarget()).thenReturn(Foo.class.newInstance());
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException e1) {
            e1.printStackTrace();
            Assert.fail("Unexpected Exception: " + e1.getMessage());
        }

        // EXECUTE
        try {
            checker.authorize(context);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Exception caught ");
        }
    }
}
