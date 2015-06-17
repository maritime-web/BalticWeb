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
package dk.dma.embryo.metoc.json.client;

import dk.dma.embryo.common.configuration.PropertyFileService;
import dk.dma.embryo.test.DmiIntegration;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.Date;

@RunWith(CdiRunner.class)
@AdditionalClasses(value = {MetocJsonClientFactory.class, PropertyFileService.class})
@Category(DmiIntegration.class)
public class DmiSejlRuteServiceIT {
    @Inject
    DmiSejlRuteService dmiSejlRuteService;

    @Test
    public void testRouteInDanishWaters() {
        DmiSejlRuteService.SejlRuteRequest request = new DmiSejlRuteService.SejlRuteRequest();
        request.setMssi(999999999);
        request.setDatatypes(new String[]{"sealevel", "current", "wave", "wind", "density"});
        request.setDt(15);

        DmiSejlRuteService.Waypoint wp1 = new DmiSejlRuteService.Waypoint();
        wp1.setEta(DmiSejlRuteService.DATE_FORMAT.format(new Date(System.currentTimeMillis() + 1000L * 3600 * 0)));
        wp1.setHeading("RL");
        wp1.setLat(55.70816666666666);
        wp1.setLon(12.3115);

        DmiSejlRuteService.Waypoint wp2 = new DmiSejlRuteService.Waypoint();
        wp2.setEta(DmiSejlRuteService.DATE_FORMAT.format(new Date(System.currentTimeMillis() + 1000L * 3600 * 3)));
        wp2.setHeading("RL");
        wp2.setLat(55.725183333333334);
        wp2.setLon(12.648666666666667);

        request.setWaypoints(new DmiSejlRuteService.Waypoint[]{wp1, wp2});
        System.out.println("request : " + request);

        DmiSejlRuteService.SejlRuteResponse sejlRuteResponse = dmiSejlRuteService.sejlRute(request);

        Assert.assertEquals(0, sejlRuteResponse.getError());
        Assert.assertNull(sejlRuteResponse.getErrorMsg());
        Assert.assertNotNull(sejlRuteResponse.getMetocForecast());
        Assert.assertNotNull(sejlRuteResponse.getMetocForecast().getForecasts());
        Assert.assertTrue(sejlRuteResponse.getMetocForecast().getForecasts().length > 2);
    }

    @Test
    public void testRouteInArcticGreenlandWaters() {
        DmiSejlRuteService.SejlRuteRequest request = new DmiSejlRuteService.SejlRuteRequest();
        request.setMssi(999999999);
        request.setDatatypes(new String[]{"sealevel", "current", "wave", "wind", "density"});
        request.setDt(15);

        DmiSejlRuteService.Waypoint wp1 = new DmiSejlRuteService.Waypoint();
        wp1.setEta(DmiSejlRuteService.DATE_FORMAT.format(new Date(System.currentTimeMillis() + 1000L * 3600 * 0)));
        wp1.setHeading("RL");
        wp1.setLat(58.50698333333333);
        wp1.setLon(-50.66285);

        DmiSejlRuteService.Waypoint wp2 = new DmiSejlRuteService.Waypoint();
        wp2.setEta(DmiSejlRuteService.DATE_FORMAT.format(new Date(System.currentTimeMillis() + 1000L * 3600 * 3)));
        wp2.setHeading("RL");
        wp2.setLat(61.35735);
        wp2.setLon(-52.81616666666667);

        DmiSejlRuteService.Waypoint wp3 = new DmiSejlRuteService.Waypoint();
        wp3.setEta(DmiSejlRuteService.DATE_FORMAT.format(new Date(System.currentTimeMillis() + 1000L * 3600 * 6)));
        wp3.setHeading("RL");
        wp3.setLat(63.354683333333334);
        wp3.setLon(-52.37671666666667);

        request.setWaypoints(new DmiSejlRuteService.Waypoint[]{wp1, wp2, wp3});
        System.out.println("request : " + request);

        DmiSejlRuteService.SejlRuteResponse sejlRuteResponse = dmiSejlRuteService.sejlRute(request);
        System.out.println(sejlRuteResponse.toString());

        Assert.assertEquals(0, sejlRuteResponse.getError());
        Assert.assertNull(sejlRuteResponse.getErrorMsg());
        Assert.assertNotNull(sejlRuteResponse.getMetocForecast());
        Assert.assertNotNull(sejlRuteResponse.getMetocForecast().getForecasts());
        Assert.assertTrue(sejlRuteResponse.getMetocForecast().getForecasts().length > 2);
    }

    @Test
    public void testRouteInArcticIcelandWaters() {
        DmiSejlRuteService.SejlRuteRequest request = new DmiSejlRuteService.SejlRuteRequest();
        request.setMssi(999999999);
        request.setDatatypes(new String[]{"sealevel", "current", "wave", "wind", "density"});
        request.setDt(15);

        DmiSejlRuteService.Waypoint wp1 = new DmiSejlRuteService.Waypoint();
        wp1.setEta(DmiSejlRuteService.DATE_FORMAT.format(new Date(System.currentTimeMillis() + 1000L * 3600 * 0)));
        wp1.setHeading("RL");
        wp1.setLat(63.37405);
        wp1.setLon(-12.2107);

        DmiSejlRuteService.Waypoint wp2 = new DmiSejlRuteService.Waypoint();
        wp2.setEta(DmiSejlRuteService.DATE_FORMAT.format(new Date(System.currentTimeMillis() + 1000L * 3600 * 3)));
        wp2.setHeading("RL");
        wp2.setLat(62.00358333333333);
        wp2.setLon(-17.572016666666666);

        DmiSejlRuteService.Waypoint wp3 = new DmiSejlRuteService.Waypoint();
        wp3.setEta(DmiSejlRuteService.DATE_FORMAT.format(new Date(System.currentTimeMillis() + 1000L * 3600 * 6)));
        wp3.setHeading("RL");
        wp3.setLat(62.27056666666667);
        wp3.setLon(-22.05445);

        request.setWaypoints(new DmiSejlRuteService.Waypoint[]{wp1, wp2, wp3});
        System.out.println("request : " + request);

        DmiSejlRuteService.SejlRuteResponse sejlRuteResponse = dmiSejlRuteService.sejlRute(request);
        System.out.println(sejlRuteResponse.toString());

        Assert.assertEquals(0, sejlRuteResponse.getError());
        Assert.assertNull(sejlRuteResponse.getErrorMsg());
        Assert.assertNotNull(sejlRuteResponse.getMetocForecast());
        Assert.assertNotNull(sejlRuteResponse.getMetocForecast().getForecasts());
        Assert.assertTrue(sejlRuteResponse.getMetocForecast().getForecasts().length > 2);
    }

    @Test
    public void testRouteCloseToSvalbard() {
        DmiSejlRuteService.SejlRuteRequest request = new DmiSejlRuteService.SejlRuteRequest();
        request.setMssi(999999999);
        request.setDatatypes(new String[]{"sealevel", "current", "wave", "wind", "density"});
        request.setDt(15);

        DmiSejlRuteService.Waypoint wp1 = new DmiSejlRuteService.Waypoint();
        wp1.setEta(DmiSejlRuteService.DATE_FORMAT.format(new Date(System.currentTimeMillis() + 1000L * 3600 * 0)));
        wp1.setHeading("RL");
        wp1.setLat(77.9777);
        wp1.setLon(7.382816666666667);

        DmiSejlRuteService.Waypoint wp2 = new DmiSejlRuteService.Waypoint();
        wp2.setEta(DmiSejlRuteService.DATE_FORMAT.format(new Date(System.currentTimeMillis() + 1000L * 3600 * 3)));
        wp2.setHeading("RL");
        wp2.setLat(76.8284);
        wp2.setLon(8.173833333333333);

        DmiSejlRuteService.Waypoint wp3 = new DmiSejlRuteService.Waypoint();
        wp3.setEta(DmiSejlRuteService.DATE_FORMAT.format(new Date(System.currentTimeMillis() + 1000L * 3600 * 6)));
        wp3.setHeading("RL");
        wp3.setLat(75.5055);
        wp3.setLon(6.679683333333333);

        request.setWaypoints(new DmiSejlRuteService.Waypoint[]{wp1, wp2, wp3});
        System.out.println("request : " + request);

        DmiSejlRuteService.SejlRuteResponse sejlRuteResponse = dmiSejlRuteService.sejlRute(request);
        System.out.println(sejlRuteResponse.toString());

        Assert.assertEquals(0, sejlRuteResponse.getError());
        Assert.assertNull(sejlRuteResponse.getErrorMsg());
        Assert.assertNotNull(sejlRuteResponse.getMetocForecast());
        Assert.assertNotNull(sejlRuteResponse.getMetocForecast().getForecasts());
        Assert.assertTrue(sejlRuteResponse.getMetocForecast().getForecasts().length > 2);
    }

}
