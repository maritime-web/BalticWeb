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
package dk.dma.embryo.enav.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * @author Jesper Tejlgaard
 */
public class RouteParserTest {

    @Test
    public void testGetRouteParser() throws IOException {
        // Data

        InputStream is = new ByteArrayInputStream(new byte[] {});
        Map<String, String> config = new HashMap<>();

        RouteParser parser = RouteParser.getRouteParser("map1234t", is, config);
        Assert.assertEquals(SAMRouteParser.class, parser.getClass());
    }

}
