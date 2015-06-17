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
package dk.dma.embryo.user.model;

import dk.dma.embryo.vessel.model.Vessel;
import org.junit.Test;
import org.unitils.reflectionassert.ReflectionAssert;

import java.util.Arrays;
import java.util.List;

import static dk.dma.embryo.user.json.UserRestService.User;

/**
 * Created by Jesper Tejlgaard on 6/11/15.
 */
public class SecuredUserTest {


    @Test
    public void testToJsonModel() {
        SecuredUser user1 = new SecuredUser();
        user1.setAisFilterName("FooFilter");
        user1.setUserName("JohnDoe");
        user1.setEmail("john@doe.com");
        SailorRole sailor = new SailorRole();
        Vessel vessel = new Vessel(123456789L);
        sailor.setVessel(vessel);
        user1.setRole(sailor);

        SecuredUser user2 = new SecuredUser();
        user2.setAisFilterName("BarFilter");
        user2.setUserName("Ella");
        user2.setEmail("ella@doe.com");
        ShoreRole shore = new ShoreRole();
        user2.setRole(shore);

        List<SecuredUser> users = Arrays.asList(user1, user2);

        User exp1 = new User();
        exp1.setAisFilterName("FooFilter");
        exp1.setEmail("john@doe.com");
        exp1.setLogin("JohnDoe");
        exp1.setRole("Sailor");
        exp1.setShipMmsi(123456789L);

        User exp2 = new User();
        exp2.setAisFilterName("BarFilter");
        exp2.setEmail("ella@doe.com");
        exp2.setLogin("Ella");
        exp2.setRole("Shore");

        List<User> expected = Arrays.asList(exp1, exp2);

        ReflectionAssert.assertReflectionEquals(expected, SecuredUser.toJsonModel(users));
    }

}
