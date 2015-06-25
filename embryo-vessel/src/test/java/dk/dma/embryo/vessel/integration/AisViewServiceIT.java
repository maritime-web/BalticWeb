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

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.dma.embryo.common.configuration.Property;
import dk.dma.embryo.common.configuration.PropertyFileService;
import dk.dma.embryo.vessel.integration.AisViewServiceAllAisData.MaxSpeed;
import dk.dma.embryo.vessel.integration.AisViewServiceAllAisData.TrackSingleLocation;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@RunWith(CdiRunner.class)
@AdditionalClasses(value = {AisClientFactory.class, PropertyFileService.class})
public class AisViewServiceIT {

    @Inject
    private AisViewServiceAllAisData aisViewServiceAllAisData;

    @Inject
    @Property("dk.dma.embryo.restclients.fullAisViewServiceInclNorwegianDataUrl")
    private String fullAisViewServiceInclNorwegianDataUrl;

    @Test
    public void test() {

        List<AisVessel> vesselList = aisViewServiceAllAisData.vesselList(AisViewServiceAllAisData.LOOK_BACK_PT24H, AisViewServiceAllAisData.LOOK_BACK_PT24H);

        System.out.println("Full list: " + vesselList.size());
    }

    @Test
    public void testVesselTypes() {

        List<AisVessel> vesselList = aisViewServiceAllAisData.vesselList(AisViewServiceAllAisData.LOOK_BACK_PT24H, AisViewServiceAllAisData.LOOK_BACK_PT24H);
        
        List<String> shipTypes = new ArrayList<String>();
        for (AisVessel vessel : vesselList) {
            
            String shipType = vessel.getVesselType();
            
            boolean exists = false;
            for (String shipTypeInList : shipTypes) {
                if(shipType != null && shipTypeInList.equals(shipType)) {
                    exists = true;
                    break;
                }
            }
            if(shipType != null && !exists) {
                
                shipTypes.add(shipType);
            }
        }
        
        System.out.println("Sker der noget her?");
        
        for (String shipType : shipTypes) {
            System.out.println("Type: " + shipType);
        }
        
        System.out.println("Full list: " + vesselList.size());
    }

    @Test
    public void testHistoricalTrack() {

        List<TrackSingleLocation> historicalTrack = aisViewServiceAllAisData.historicalTrack(220443000, 500, AisViewServiceAllAisData.LOOK_BACK_PT12H);
        System.out.println("Number of points in track: " + historicalTrack.size());
    }

    @Test
    public void testHistoricalTrackLongNoTimeout() {

        List<TrackSingleLocation> historicalTrack = aisViewServiceAllAisData.historicalTrackLong(251377000, 500, AisViewServiceAllAisData.LOOK_BACK_PT120H);
        System.out.println("Number of points in long track: " + historicalTrack.size());
    }

    @Test
    public void testMaxSpeeds() {

        List<MaxSpeed> allMaxSpeeds = aisViewServiceAllAisData.allMaxSpeeds();
        System.out.println("Number of maxSpeeds: " + allMaxSpeeds.size());
    }

    @Test
    public void testHistoricalTrackLongWithTimeout() throws IOException {

        HttpParams httpParams = new BasicHttpParams();
        HttpClient httpClient = new DefaultHttpClient();

        HttpConnectionParams.setConnectionTimeout(httpParams, 3000);
        HttpConnectionParams.setSoTimeout(httpParams, 15000);

        String url = this.fullAisViewServiceInclNorwegianDataUrl;
        System.out.println("URL -> " + this.fullAisViewServiceInclNorwegianDataUrl);

        HttpGet getRequest = new HttpGet(url + "/vessel/longtrack/" + 251377000);
        getRequest.addHeader("accept", "application/json");

        httpParams.setIntParameter("minDist", 500);
        httpParams.setParameter("age", AisViewServiceAllAisData.LOOK_BACK_PT120H);
        getRequest.setParams(httpParams);

        HttpResponse response = httpClient.execute(getRequest);

        String json = EntityUtils.toString(response.getEntity());
        System.out.println("Result -> " + json);

        List<LinkedHashMap<String, Object>> ob = new ObjectMapper().readValue(json, ArrayList.class);

        System.out.println("List size -> " + ob.size());

        List<TrackSingleLocation> historicalTrack = new ArrayList<>();
        for (LinkedHashMap<String, Object> linkedHashMap : ob) {
            Double cog = (Double) linkedHashMap.get("cog");
            Double lat = (Double) linkedHashMap.get("lat");
            Double lon = (Double) linkedHashMap.get("lon");
            Double sog = (Double) linkedHashMap.get("sog");
            Long time = (Long) linkedHashMap.get("time");

            TrackSingleLocation point = new TrackSingleLocation(cog, lat, lon, sog, time);
            historicalTrack.add(point);

//            System.out.println("point -> " + point.toString());
        }

        httpClient.getConnectionManager().shutdown();

    }
}
