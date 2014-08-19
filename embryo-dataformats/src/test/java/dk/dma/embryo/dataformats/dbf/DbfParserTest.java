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
package dk.dma.embryo.dataformats.dbf;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class DbfParserTest {
    @Test
    public void readFileFromDmi() throws IOException {
        List<Map<String,Object>> result = DbfParser.parse(getClass().getResourceAsStream("/ice/201307222045_CapeFarewell_RIC.dbf"));

        assertEquals(10, result.size());
        assertEquals("3", result.get(0).get("FA"));
        assertEquals("W", result.get(9).get("POLY_TYPE"));
    }
}
