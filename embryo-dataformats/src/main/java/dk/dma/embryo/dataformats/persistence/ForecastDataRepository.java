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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.dma.embryo.common.EmbryonicException;
import dk.dma.embryo.dataformats.model.ForecastData;
import dk.dma.embryo.dataformats.model.ForecastDataId;
import dk.dma.embryo.dataformats.model.ForecastHeader;
import dk.dma.embryo.dataformats.model.Type;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Steen on 11-01-2016.
 *
 */
@Singleton
@Startup
public class ForecastDataRepository {
    private HttpCouchClient httpCouchClient;

    //Required by EJB spec.
    @SuppressWarnings("unused")
    protected ForecastDataRepository() {
    }

    @PostConstruct
    public void initialize() {
        httpCouchClient.initialize();
    }

    @Inject
    public ForecastDataRepository(@CouchDatabase(databaseName = "forecast")HttpCouchClient httpCouchClient) {
        this.httpCouchClient = httpCouchClient;
    }

    public void addOrUpdateForecastData(ForecastData forecastData) {
        httpCouchClient.upsert(forecastData.getIdAsString(), forecastData.getJson());
    }

    public String getForecastData(String id) {
        return httpCouchClient.get(id);
    }

    public ForecastHeader getForecastHeader(ForecastDataId id) {
        String viewQuery = "/header_by_id?key=%22"+id.getId()+"%22";

        String result = httpCouchClient.getByView(viewQuery);

        List<Map> resultList = extractViewResultValues(result);
        if (resultList.isEmpty()) {
            return ForecastHeader.createFrom(id);
        }
        return ForecastHeader.createFrom(toJsonString(resultList.get(0)));
    }

    public List<ForecastHeader> list(Type type) {
        String viewQuery = "/header_by_type?key=%22"+type.name()+"%22";

        String result = httpCouchClient.getByView(viewQuery);

        return toForecastHeaders(extractViewResultValues(result));
    }

    private List<ForecastHeader> toForecastHeaders(List<Map> viewResult) {
        return viewResult.stream().map(this::toForecastHeader).collect(Collectors.toList());
    }

    private ForecastHeader toForecastHeader (Map data) {
        return ForecastHeader.createFrom(toJsonString(data));
    }

    @SuppressWarnings("unchecked")
    private List<Map> extractViewResultValues(String result) {
        try {
            List<Map> filteredResult = new ArrayList<>();
            ObjectMapper mapper = new ObjectMapper();
            HashMap jsonMap = mapper.readValue(result, HashMap.class);
            List rows = (List) jsonMap.get("rows");
            rows.forEach(r -> {
                Map row = (Map) r;
                Map value = (Map) row.get("value");
                value.put("id", row.get("id"));
                filteredResult.add(value);
            });

            return filteredResult;
        } catch (IOException e) {
            throw new EmbryonicException("Couldn't extract view result from:  \"" + result + "\"", e);
        }
    }

    private String toJsonString(Object list) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            throw new EmbryonicException("Couldn't convert \"" + list + "\" to json string", e);
        }
    }
}
