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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import dk.dma.embryo.domain.SailorRole;
import dk.dma.embryo.security.authorization.Roles;
import dk.dma.embryo.security.authorization.RolesAllowAll;

/**
 * @author Jesper Tejlgaard
 */
public class RoleExtractorTest {

    private RoleExtractor extractor = new RoleExtractor();
    
    @Test
    public void testGetAuthorizationConfiguration_Method_Roles() throws NoSuchMethodException{
        Method method;
        List<Annotation> result;
        class Foo{
            @Roles(SailorRole.class)
            public void doSomething(){
            }
        }
        
        method = Foo.class.getMethod("doSomething");
        result = extractor.getAuthorizationConfiguration(method);
        
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(Roles.class, result.get(0).annotationType());        
    }

    @Test
    public void testGetAuthorizationConfiguration_Method_Deprecated() throws NoSuchMethodException {
        Method method;
        List<Annotation> result;
        class Foo{
            /**
             * @deprecated for testing purpose
             */
            @Deprecated
            public void doSomething(){
            }
        }
        
        method = Foo.class.getMethod("doSomething");
        result = extractor.getAuthorizationConfiguration(method);
        
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void testGetAuthorizationConfiguration_Method_RolesAllowAll() throws NoSuchMethodException{
        Method method;
        List<Annotation> result;
        class Foo{
            @RolesAllowAll
            public void doSomething(){
            }
        }
        
        method = Foo.class.getMethod("doSomething");
        result = extractor.getAuthorizationConfiguration(method);
        
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(RolesAllowAll.class, result.get(0).annotationType());        
    }

}
