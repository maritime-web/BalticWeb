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
package dk.dma.embryo.user.json;

import dk.dma.embryo.common.json.AbstractRestService;
import dk.dma.embryo.user.model.AreasOfInterest;
import dk.dma.embryo.user.model.SecuredUser;
import dk.dma.embryo.user.security.Subject;
import dk.dma.embryo.user.service.UserService;
import org.jboss.resteasy.annotations.GZIP;
import org.slf4j.Logger;

import javax.ejb.FinderException;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.ArrayList;
import java.util.List;

@Path("/areasOfInterest")
public class AreasOfInterestRestService extends AbstractRestService {

    @Inject
    private Logger logger;

    @Inject
    private Subject subject;

    @Inject
    private UserService userService;

    @GET
    @GZIP
    @Path("/list")
    @Produces("application/json")
    public Response list(@Context Request request) {
        
        SecuredUser securedUser = this.subject.getUser();
        logger.info("Calling list all Selection Groups for logged on user -> " + securedUser.getUserName());

        List<AreasOfInterestDTO> result = new ArrayList<AreasOfInterestDTO>();

        for (AreasOfInterest selectionGroup : securedUser.getAreasOfInterest()) {

            AreasOfInterestDTO areasOfInterestDTO = new AreasOfInterestDTO();
            areasOfInterestDTO.setId(selectionGroup.getId());
            areasOfInterestDTO.setActive(selectionGroup.getActive());
            areasOfInterestDTO.setName(selectionGroup.getName());
            areasOfInterestDTO.setPolygonsAsJson(selectionGroup.getPolygonsAsJson());

            result.add(areasOfInterestDTO);
        }
        
        return super.getResponse(request, result, NO_CACHE);
    }

    @POST
    @Path("/update")
    @Consumes("application/json")
    public void update(List<AreasOfInterestDTO> areasOfInterestDTOs) {


        if (areasOfInterestDTOs != null) {

            List<AreasOfInterest> areasOfInterests = new ArrayList<AreasOfInterest>();
            for (AreasOfInterestDTO selectionGroupDTO : areasOfInterestDTOs) {

                AreasOfInterest selectionGroup = new AreasOfInterest(
                        selectionGroupDTO.name, selectionGroupDTO.polygonsAsJson, selectionGroupDTO.active);

                areasOfInterests.add(selectionGroup);
            }

            try {
                this.userService.updateAreasOfInterest(areasOfInterests, this.subject.getUser().getUserName());
            } catch (FinderException e) {
                throw new WebApplicationException(Response.status(Status.NOT_FOUND).entity(e.getMessage()).build());
            }
        }
    }

    public static class AreasOfInterestDTO {

        private Long id;
        private String name;
        private Boolean active;
        private String polygonsAsJson;

        @Override
        public int hashCode() {

            final int prime = 31;
            int result = 1;
            result = prime * result + ((id == null) ? 0 : id.hashCode());
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            result = prime * result + ((active == null) ? 0 : active.hashCode());
            result = prime * result + ((polygonsAsJson == null) ? 0 : polygonsAsJson.hashCode());
            
            return result;
        }
        
        public Long getId() {
            return id;
        }
        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }

        public Boolean getActive() {
            return active;
        }
        public void setActive(Boolean active) {
            this.active = active;
        }

        public String getPolygonsAsJson() {
            return polygonsAsJson;
        }
        public void setPolygonsAsJson(String polygonsAsJson) {
            this.polygonsAsJson = polygonsAsJson;
        }
    }
}
