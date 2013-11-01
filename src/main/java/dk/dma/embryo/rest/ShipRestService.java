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
package dk.dma.embryo.rest;

import java.util.List;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.jboss.resteasy.annotations.GZIP;
import org.slf4j.Logger;

import dk.dma.arcticweb.service.GreenPosService;
import dk.dma.arcticweb.service.VesselService;
import dk.dma.embryo.rest.json.Ship;
import dk.dma.enav.model.ship.ShipType;

/**
 * 
 * @author Jesper Tejlgaard
 */
@Path("/ship")
public class ShipRestService {

    @Inject
    private VesselService shipService;

    @Inject
    private GreenPosService greenposService;

    @Inject
    private Logger logger;

    public ShipRestService() {
    }

    @GET
    @Path("/yourship")
    @Produces("application/json")
    @GZIP
    public Ship getYourShip() {
        logger.debug("getYourShip()");

        dk.dma.embryo.domain.Vessel ship = shipService.getYourVessel();
        
        Ship result = null;
        if(ship != null){
            result = ship.toJsonModel();
        }
        
        logger.debug("getYourShip(): {}", result);
        
        return result;
    }

    @GET
    @Path("/shiptypes")
    @Produces("application/json")
    @GZIP
    public List<String> getShipTypes() {
        logger.debug("getShipTypes()");

        List<String> result = ShipType.getStringList(); 

        logger.debug("getShipTypes(): {}", result);
        
        return result;
    }

    @PUT
    @Consumes("application/json")
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public String save(Ship ship) {
        logger.debug("save({})", ship);

        dk.dma.embryo.domain.Vessel toBeSaved = dk.dma.embryo.domain.Vessel.fromJsonModel(ship);
        
        String maritimeId = shipService.save(toBeSaved);
        
        logger.debug("save(): {}", maritimeId);
        
        return maritimeId;
    }
    
//    @GET
//    @Path("/reportingstatus")
//    @Produces("application/json")
//    public Ship getReportStatus() {
//        logger.debug("getReportStatus()");
//        
//        Ship result = null;
//        if(ship != null){
//            result = ship.toJsonModel();
//        }
//        
//        logger.debug("getReportStatus(): {}", result);
//        
//        return result;
//    }

//    @GET
//    @Path("/{maritimeId}")
//    @Consumes("application/json")
//    public void get(@PathParam("maritimeId") String maritimeId) {
//        logger.debug("get({})", maritimeId);
//
//        GreenPosReport toBeSaved = GreenPosReport.from(report);
//
//        shipService.get
//        // String result = "Product created : " + product;
//        // return Response.status(201).entity(result).build();
//        logger.debug("save() - done", report);
//    }

}
