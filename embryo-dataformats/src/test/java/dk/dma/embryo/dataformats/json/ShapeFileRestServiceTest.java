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
package dk.dma.embryo.dataformats.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import org.unitils.reflectionassert.ReflectionAssert;

import dk.dma.embryo.dataformats.model.ShapeFileMeasurement;
import dk.dma.embryo.dataformats.model.factory.ShapeFileNameParserFactory;
import dk.dma.embryo.dataformats.persistence.ShapeFileMeasurementDao;
import dk.dma.embryo.dataformats.service.ShapeFileService;
import dk.dma.embryo.dataformats.service.ShapeFileService.Fragment;
import dk.dma.embryo.dataformats.service.ShapeFileService.Shape;

/**
 * @author Jesper Tejlgaard
 */
public class ShapeFileRestServiceTest {

    ShapeFileService service;
    ShapeFileMeasurementDao dao;
    ShapeFileNameParserFactory factory;
    ShapeFileRestService jsonService;

    Request request;
    ResponseBuilder responseBuilder;

    @Before
    public void setup() {
        service = mock(ShapeFileService.class);
        dao = mock(ShapeFileMeasurementDao.class);
        factory = mock(ShapeFileNameParserFactory.class);

        request = mock(Request.class);
        responseBuilder = mock(ResponseBuilder.class);

        jsonService = new ShapeFileRestService(service, dao, factory);
        jsonService.logger = LoggerFactory.getLogger(ShapeFileRestService.class);
    }

    @Test
    public void testCachedRetrieval() throws IOException {
        DateTime now = DateTime.now(DateTimeZone.UTC);

        ShapeFileMeasurement measurement = new ShapeFileMeasurement("dmi", "MyRegion", 12, 0);
        measurement.setCreated(now);
        when(dao.lookup("MyRegion", "dmi")).thenReturn(measurement);
        when(request.evaluatePreconditions(now.toDate())).thenReturn(Response.notModified());

        Response response = jsonService.getSingleFile("dmi.MyRegion", 0, "", true, 0, 0, request);

        assertEquals(304, response.getStatus());

        CacheControl cacheControl = new CacheControl();
        cacheControl.setMaxAge(900);
        cacheControl.setNoTransform(false);

        assertEquals(1, ((List<Object>) response.getMetadata().get("Cache-Control")).size());
        ReflectionAssert.assertLenientEquals(cacheControl, ((List<Object>) response.getMetadata().get("Cache-Control")).get(0));
        assertNull(response.getEntity());
    }

    @Test
    public void testNonCachedRetrieval() throws IOException {
        DateTime now = DateTime.now(DateTimeZone.UTC);

        ShapeFileMeasurement measurement = new ShapeFileMeasurement("dmi", "MyRegion", 12, 0);
        measurement.setCreated(now);
        when(dao.lookup("MyRegion", "dmi")).thenReturn(measurement);
        when(request.evaluatePreconditions(now.toDate())).thenReturn(null);

        Map<String, Object> map = new HashMap<>();
        map.put("test", "test");
        Shape shape = new Shape(map, new ArrayList<Fragment>(), 2);
        
        when(service.readSingleFile("dmi.MyRegion",0, "", true, 0, 0)).thenReturn(shape);
        
        Response response = jsonService.getSingleFile("dmi.MyRegion", 0, "", true, 0, 0, request);

        assertEquals(200, response.getStatus());

        CacheControl cacheControl = new CacheControl();
        cacheControl.setMaxAge(900);
        cacheControl.setNoTransform(false);

        assertEquals(1, ((List<Object>) response.getMetadata().get("Cache-Control")).size());
        ReflectionAssert.assertLenientEquals(cacheControl, ((List<Object>) response.getMetadata().get("Cache-Control")).get(0));
        assertNotNull(response.getEntity());
        assertEquals(shape , response.getEntity());
    }

    @Test
    public void testUpdated() throws IOException {
        DateTime now = DateTime.now(DateTimeZone.UTC);

        ShapeFileMeasurement measurement = new ShapeFileMeasurement("dmi", "MyRegion", 12, 2);
        measurement.setCreated(now);
        when(dao.lookup("MyRegion", "dmi")).thenReturn(measurement);
        when(request.evaluatePreconditions(now.toDate())).thenReturn(null);

        Map<String, Object> map = new HashMap<>();
        map.put("test", "test");
        Shape shape = new Shape(map, new ArrayList<Fragment>(), 2);
        
        when(service.readSingleFile("dmi.MyRegion_v2",0, "", true, 0, 0)).thenReturn(shape);
        
        Response response = jsonService.getSingleFile("dmi.MyRegion", 0, "", true, 0, 0, request);

        assertEquals(200, response.getStatus());

        CacheControl cacheControl = new CacheControl();
        cacheControl.setMaxAge(900);
        cacheControl.setNoTransform(false);

        assertEquals(1, ((List<Object>) response.getMetadata().get("Cache-Control")).size());
        ReflectionAssert.assertLenientEquals(cacheControl, ((List<Object>) response.getMetadata().get("Cache-Control")).get(0));
        assertNotNull(response.getEntity());
        assertEquals(shape , response.getEntity());
    }

}
