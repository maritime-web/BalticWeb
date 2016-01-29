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
package dk.dma.arcticweb.rest;

import javax.inject.Inject;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

import org.slf4j.Logger;

import dk.dma.arcticweb.service.AppDataServiceBean;

/**
 * 
 * @author Jesper Tejlgaard
 */
@Path("/testData")
public class TestDataRestService {

    @Inject
    private AppDataServiceBean testService;

    @Inject
    private Logger logger;

    public TestDataRestService() {
    }

    @PUT
    public String reInitializeTestData() {
        logger.debug("reInitializeTestData()");
        
        testService.resetTestData();
        
        String result = "SUCCESS";
        // String result = "Product created : " + product;
        // return Response.status(201).entity(result).build();
        logger.debug("reInitializeTestData(): {}", result);
        
        return result;
    }

}
