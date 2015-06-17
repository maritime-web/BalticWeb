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
package dk.dma.embryo.vessel.integration;

import dk.dma.embryo.common.area.Area;
import dk.dma.embryo.common.area.AreaFilter;
import org.junit.Test;
import org.mockito.Mockito;
import org.unitils.reflectionassert.ReflectionAssert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;

public class AisTrackRequestParamBuilderTest {

    @Test
    public void testUserSelectedAreas() {
        List<Area> testAreas = new ArrayList<>();
        testAreas.add(new Area(10, 40, 20, 30));
        testAreas.add(new Area(4, 10, 5, 9));

        AreaFilter areaFilter = Mockito.mock(AreaFilter.class);
        when(areaFilter.getAreasAsStream()).thenReturn(testAreas.stream());

        AisTrackRequestParamBuilder builder = new AisTrackRequestParamBuilder();
        builder.addUserSelectedAreas(areaFilter);

        ReflectionAssert.assertReflectionEquals(Arrays.asList(new String[]{"30.0|10.0|40.0|20.0", "9.0|4.0|10.0|5.0"}), builder.getUserSelectedAreas());
    }

    @Test
    public void testDefaultArea() {
        List<Area> testAreas = new ArrayList<>();
        AreaFilter areaFilter = Mockito.mock(AreaFilter.class);
        when(areaFilter.getAreasAsStream()).thenReturn(testAreas.stream());

        AisTrackRequestParamBuilder builder = new AisTrackRequestParamBuilder();
        builder.addUserSelectedAreas(areaFilter).setDefaultArea("circle(70,-46,1800000)");

        ReflectionAssert.assertReflectionEquals(Arrays.asList(new String[]{"circle%2870%2C-46%2C1800000%29"}), builder.getUserSelectedAreas());
    }

}
