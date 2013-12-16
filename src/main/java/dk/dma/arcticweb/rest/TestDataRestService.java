/* Copyright (c) 2011 Danish Maritime Authority
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dma.arcticweb.rest;

import javax.inject.Inject;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

import org.slf4j.Logger;

import dk.dma.arcticweb.service.TestServiceBean;

/**
 * 
 * @author Jesper Tejlgaard
 */
@Path("/testData")
public class TestDataRestService {

    @Inject
    private TestServiceBean testService;

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
