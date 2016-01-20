/*
 *  Copyright (c) 2011 Danish Maritime Authority.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package dk.dma.embryo.dataformats.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.dma.embryo.common.EmbryonicException;
import dk.dma.embryo.dataformats.model.ForecastData;
import dk.dma.embryo.dataformats.model.ForecastType;

import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Steen on 11-01-2016.
 *
 */
@Singleton
@Startup
public class ForecastDataRepository {
    private HttpCouchClient httpCouchClient;

    //Required by EJB spec.
    protected ForecastDataRepository() {
    }

    @Inject
    public ForecastDataRepository(@CouchDatabase(databaseName = "forecast")HttpCouchClient httpCouchClient) {
        this.httpCouchClient = httpCouchClient;
    }

    public void addOrUpdateForecastData(ForecastData forecastData) {
        httpCouchClient.upsert(forecastData.getId(), forecastData.getJson());
    }

    public String getForecastData(String id) {
        return httpCouchClient.get(id);
    }

    public String list(ForecastType.Type type) {
        String viewQuery = "/header_by_type?key=%22"+type.name()+"%22";

        String result = httpCouchClient.getByView(viewQuery);

        return extractViewResultValue(result);
    }

    private String extractViewResultValue(String result) {
        try {
            List filteredResult = new ArrayList<>();
            ObjectMapper mapper = new ObjectMapper();
            HashMap jsonMap = mapper.readValue(result, HashMap.class);
            List rows = (List) jsonMap.get("rows");
            rows.forEach(r -> {
                Map row = (Map) r;
                Map value = (Map) row.get("value");
                value.put("id", row.get("id"));
                filteredResult.add(value);
            });

            return mapper.writeValueAsString(filteredResult);
        } catch (IOException e) {
            throw new EmbryonicException("Couldn't extract view result from:  \"" + result + "\"", e);
        }
    }
}
