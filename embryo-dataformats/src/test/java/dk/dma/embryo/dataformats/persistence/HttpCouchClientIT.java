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

package dk.dma.embryo.dataformats.persistence;

import dk.dma.embryo.dataformats.model.Type;
import org.hamcrest.CoreMatchers;
import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;

@Ignore("To run this suite insert url to a known CouchDb.")
public class HttpCouchClientIT {

    @Test
    public void testInitialize() throws Exception {
        getHttpCouchClient();
    }

    @Test
    public void testUpsert() throws Exception {
        HttpCouchClient cut = getHttpCouchClient();

        cut.upsert("testForecast", "{\"weather\": \"fine\"}");
    }

    @Test
    public void testGet() throws Exception {
        HttpCouchClient cut = getHttpCouchClient();
        String testForecast = "testForecast";
        cut.upsert(testForecast, "{\"weather\": \"fine\"}");

        String result = cut.get(testForecast);

        assertThat(result, CoreMatchers.containsString("weather"));
    }

    @Test
    public void testGetByView() throws Exception {
        HttpCouchClient cut = getHttpCouchClient();
        String viewQuery = "/header_by_type?key=%22"+ Type.CURRENT_FORECAST.name()+"%22";

        String result = cut.getByView(viewQuery);

        System.out.println(result);
    }

    private HttpCouchClient getHttpCouchClient() {
        HttpCouchClient cut = new HttpCouchClient(getConfig());
        cut.initialize();
        return cut;
    }

    private CouchDbConfig getConfig() {
        return new CouchDbConfig("/forecast", "/couchdb/forecast-design.json", "_design/forecast", "192.168.99.101", 5984, "embryo", "embryo");
    }
}
