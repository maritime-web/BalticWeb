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
package dk.dma.embryo.vessel.json;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.fasterxml.jackson.databind.ObjectMapper;

import dk.dma.embryo.common.util.ParseUtils;
import dk.dma.embryo.vessel.component.RouteParserComponent;
import dk.dma.embryo.vessel.component.ScheduleParser;
import dk.dma.embryo.vessel.component.ScheduleUploadPostProcessor;
import dk.dma.embryo.vessel.model.Voyage;
import dk.dma.embryo.vessel.service.ScheduleService;

/**
 * @author Jesper Tejlgaard
 */
@Path("/routeUpload")
public class RouteUploadRestService {

    @Inject
    private ScheduleService scheduleService;

    @Inject
    private ScheduleParser scheduleParser;

    @Inject
    private ScheduleUploadPostProcessor scheduleUploadPostProcessor;

    @Inject
    private Logger logger;

    public RouteUploadRestService() {
    }

    /**
     * Handles upload of a single route file.
     * <p/>
     * Required request parameter : file - the one file, which contains the new route. Optional request parameter :
     * active - indicates whether the uploaded route is the new active or not. Optional request parameter : voyageId -
     * the enav id of the voyage, which the route belongs to (if any).
     *
     * @param req
     * @return
     * @throws FileUploadException
     * @throws IOException
     */
    @POST
    @Path("/single")
    @Consumes("multipart/form-data")
    @Produces({"application/json"})
    public Files uploadFile(@Context HttpServletRequest req) throws FileUploadException, IOException {
        FileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);

        List<FileItem> items = upload.parseRequest(req);
        String voyageId = null;
        Boolean active = null;
        Map<String, String> context = new HashMap<>();

        for (FileItem item : items) {
            if (item.isFormField()) {
                logger.debug("Found FORM FIELD: {}={}", item.getFieldName(), item.getString());
                if ("voyageId".equals(item.getFieldName())) {
                    voyageId = item.getString();
                } else if ("active".equals(item.getFieldName())) {
                    active = "true".equals(item.getString()) || "TRUE".equals(item.getString());
                } else if ("name".equals(item.getFieldName())) {
                    context.put("name", item.getString());
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

                dk.dma.embryo.vessel.model.Route route = new RouteParserComponent().parseRoute(item.getName(),
                        item.getInputStream(), context);

                String enavId = scheduleService.saveRoute(route, voyageId, active);

                result.files.add(new RestFile(item.getName(), item.getSize(), enavId));

                fileItemCount++;
            }
        }

        return result;
    }

    /**
     * This is a workaround to the fact that Internet Explorer v <= 9 does not allow file submit using Ajax (See more
     * here https://github.com/blueimp/jQuery-File-Upload/issues/123). by setting content type to text/html but
     * returning JSON it works.
     *
     * @param req
     * @return
     * @throws FileUploadException
     * @throws IOException
     */
    @POST
    @Path("/single")
    @Consumes("multipart/form-data")
    @Produces({"text/html"})
    public String uploadFileHtml(@Context HttpServletRequest req) throws FileUploadException, IOException {
        Files files = uploadFile(req);
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(files);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @POST
    @Path("/schedule")
    @Consumes("multipart/form-data")
    @Produces({"application/json"})
    public ScheduleResponse uploadSchedule(@Context HttpServletRequest req) throws FileUploadException, IOException {
        logger.debug("uploadSchedule(...)");

        FileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        InputStream inputStream = null;
        Date lastDeparture = null;
        Long mmsi = null;

        List<FileItem> items = upload.parseRequest(req);
        for (FileItem item : items) {
            if ("lastDeparture".equals(item.getFieldName())) {
                String date = item.getString();
                if(date == null || date.isEmpty()) {
                    lastDeparture = new Date(0);
                } else {
                    Long lastDepartureLong = ParseUtils.parseLong(date);
                    lastDeparture = new Date(lastDepartureLong);
                }
            } else if ("mmsi".equals(item.getFieldName())) {
                mmsi = Long.valueOf(item.getString());
            } else {
                inputStream = item.getInputStream();
            }
        }
        ScheduleResponse response = scheduleParser.parse(inputStream);
        response = scheduleUploadPostProcessor.validate(response, mmsi, lastDeparture);

        if (response.getErrors() == null || response.getErrors().length == 0) {
            try {
                List<Voyage> voyages = Voyage.fromJsonModel(response.getVoyages());
                scheduleService.updateSchedule(mmsi, voyages, new String[0]);
                response.setVoyages(new dk.dma.embryo.vessel.json.Voyage[0]);
            } catch (Exception e) {
                response.setErrors(new String[]{"Could not save uploaded schedule data due to internal error: " + e.getMessage() + ".", "Please correct invalid data if possible and try to Save."});
            }
        }

        logger.debug("uploadSchedule(): {}", response);
        return response;
    }

    public static class Files {
        List<RestFile> files = new ArrayList<>(2);

        public List<RestFile> getFiles() {
            return files;
        }
    }

    public static class RestFile {
        private String name;
        private long size;
        private String routeId;

        public RestFile(String name, long size, String routeId) {
            super();
            this.name = name;
            this.size = size;
            this.routeId = routeId;
        }

        public String getName() {
            return name;
        }

        public long getSize() {
            return size;
        }

        public String getRouteId() {
            return routeId;
        }
    }

}
