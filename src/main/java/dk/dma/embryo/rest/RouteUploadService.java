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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;

import dk.dma.arcticweb.service.ShipService;
import dk.dma.embryo.domain.Voyage;

@Path("/routeUpload")
public class RouteUploadService {

    @Inject
    private ShipService shipService;

    @Inject
    private Logger logger;

    public RouteUploadService() {
    }

    @POST
    @Path("/single")
    @Consumes("multipart/form-data")
    @Produces("application/json")
    public Files uploadFile(@Context HttpServletRequest req) throws FileUploadException, IOException {
        FileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);

        List<FileItem> items = upload.parseRequest(req);

        Long voyageId = null;

        for (FileItem item : items) {
            if (item.isFormField()) {
                if ("voyageId".equals(item.getFieldName())) {
                    logger.debug("Found voyageId as FORM FIELD: {}={}", item.getFieldName(), item.getString());
                    voyageId = Long.valueOf(item.getString());
                }
            }
        }

        Files result = new Files();

        int fileItemCount = 0;
        for (FileItem item : items) {
            if (!item.isFormField()) {
                if (fileItemCount == 1) {
                    throw new IllegalArgumentException("This REST service can only handle one file");
                }
                logger.debug("Handling uploaded route with file name: {}", item.getName());

                dk.dma.embryo.domain.Route route = shipService.parseRoute(item.getInputStream());

                if (voyageId != null) {
                    Voyage voyage = shipService.getVoyage(voyageId);
                    route.setVoyage(voyage);
                }
                Long routeId = shipService.saveRoute(route);

                result.files.add(new RestFile(item.getName(), item.getSize()));
                
                fileItemCount++;
            }
        }

        return result;
    }
    
    public static class Files{
        List<RestFile> files = new ArrayList<>(2);
        
        public List<RestFile> getFiles(){
            return files;
        }
    }

    public static class RestFile{
        private String name;
        private long size;
        
        public RestFile(String name, long size) {
            super();
            this.name = name;
            this.size = size;
        }

        public String getName() {
            return name;
        }

        public long getSize() {
            return size;
        }
    }
}
