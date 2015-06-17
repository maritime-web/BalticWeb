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
package dk.dma.embryo.user.vessel;

import dk.dma.embryo.common.EmbryonicException;
import dk.dma.embryo.user.model.SecuredUser;
import dk.dma.embryo.user.security.Subject;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.when;

/**
 * Created by Jesper Tejlgaard on 6/11/15.
 */
public class UserDefinedAisSourceFilterTest {

    @Test
    public void testGetAisFilter() {
        Subject subject = Mockito.mock(Subject.class);
        SecuredUser user = Mockito.mock(SecuredUser.class);
        when(subject.getUser()).thenReturn(user);
        when(user.getAisFilterName()).thenReturn("FooSources");

        Map<String, String> namedSourceFilter = new HashMap<>();
        namedSourceFilter.put("FooSources", "SourceFilterValue");

        UserDefinedAisSourceFilter filter = new UserDefinedAisSourceFilter(subject, namedSourceFilter);

        Assert.assertEquals("SourceFilterValue", filter.getAisFilter());
    }

    @Test
    public void testGetAisFilter_UnknownFilterName() {
        Subject subject = Mockito.mock(Subject.class);
        SecuredUser user = Mockito.mock(SecuredUser.class);
        when(subject.getUser()).thenReturn(user);
        when(user.getAisFilterName()).thenReturn("UnknownFilterName");

        Map<String, String> namedSourceFilter = new HashMap<>();
        namedSourceFilter.put("FooSources", "SourceFilterValue");

        UserDefinedAisSourceFilter filter = new UserDefinedAisSourceFilter(subject, namedSourceFilter);

        try {
            filter.getAisFilter();
            Assert.fail("Expected exception of type " + EmbryonicException.class.getSimpleName());
        } catch (EmbryonicException e) {
            Assert.assertTrue(e.getMessage().contains("UnknownFilterName"));
        }
    }
}
