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
package dk.dma.embryo.user.area;

import dk.dma.embryo.common.area.Area;
import dk.dma.embryo.user.model.AreasOfInterest;
import dk.dma.embryo.user.model.SecuredUser;
import dk.dma.embryo.user.security.Subject;
import org.junit.Test;
import org.mockito.Mockito;
import org.unitils.reflectionassert.ReflectionAssert;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.Mockito.when;

/**
 * Created by Jesper Tejlgaard on 6/10/15.
 */
public class AreasOfInterestBasedAreaFilterTest {

    @Test
    public void testGetAreasAsStream() {
        List<AreasOfInterest> areasOfInterest = new ArrayList<>();
        String jsonAreasOfInterest = "[{'left':10.0,'bottom':30.0,'right':20.0,'top':40.0},{'left':4.0,'bottom':9.0,'right':5.0,'top':10.0}]".replace("'", "\"");
        areasOfInterest.add(new AreasOfInterest("foo", jsonAreasOfInterest, true));
        areasOfInterest.add(new AreasOfInterest("bar", "json does not matter. Never used", false));

        Subject subject = Mockito.mock(Subject.class);
        SecuredUser user = Mockito.mock(SecuredUser.class);
        when(subject.getUser()).thenReturn(user);
        when(user.hasActiveAreasOfInterest()).thenReturn(true);
        when(user.getAreasOfInterest()).thenReturn(areasOfInterest);

        AreasOfInterestBasedAreaFilter filter = new AreasOfInterestBasedAreaFilter(subject);
        List<Area> result = filter.getAreasAsStream().collect(Collectors.toList());

        List<Area> expected = new ArrayList<>(2);
        expected.add(new Area(10, 40, 20, 30));
        expected.add(new Area(4, 10, 5, 9));

        ReflectionAssert.assertReflectionEquals(expected, result);
    }
}
