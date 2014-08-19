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
package dk.dma.embryo.msi.rs;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.annotations.cache.NoCache;

import dk.dma.embryo.msi.MsiClient;

@Path("/msi")
public class MsiRestService {
    @Inject
    private MsiClient msiClient;

    @GET
    @Path("/list")
    @Produces("application/json")
    @GZIP
    @NoCache
    public List<MsiClient.MsiItem> listActiveWarnings(@QueryParam("regions") List<String> regions) {
        return msiClient.getActiveWarnings(regions);
    }
    
    @GET
    @Path("/regions")
    @Produces("application/json")
    @GZIP
    @NoCache
    public List<MsiClient.Region> getRegions() {
        return msiClient.getRegions();
    }
}
