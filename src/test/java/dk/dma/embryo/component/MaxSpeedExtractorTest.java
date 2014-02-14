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
package dk.dma.embryo.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import dk.dma.arcticweb.service.MaxSpeedJob.MaxSpeedRecording;

/**
 * @author Jesper Tejlgaard
 */
public class MaxSpeedExtractorTest {

    @Test
    public void test() {

        Map<String, Map<String, List<Map<String, Object>>>> details = new HashMap<>();
        Map<String, List<Map<String, Object>>> pastTrack = new HashMap<>();
        List<Map<String, Object>> points = new ArrayList<>(10);
        details.put("pastTrack", pastTrack);
        pastTrack.put("points", points);

        points.add(createPoint(0.0));
        points.add(createPoint(10.0));
        points.add(createPoint(0.0));
        points.add(createPoint(12.0));
        points.add(createPoint(11.0));
        points.add(createPoint(0.0));

        MaxSpeedRecording rec = new MaxSpeedExtractor().extractMaxSpeed(details);

        Assert.assertEquals(12.0, rec.getMaxSpeed(), 0.0);
    }

    @Test
    public void testWithNoPastTrack() {
        Map<String, Map<String, List<Map<String, Object>>>> details = new HashMap<>();
        details.put("pastTrack", null);

        MaxSpeedRecording rec = new MaxSpeedExtractor().extractMaxSpeed(details);

        Assert.assertEquals(0.0, rec.getMaxSpeed(), 0.0);
    }

    @Test
    public void testWithNoPoints() {
        Map<String, Map<String, List<Map<String, Object>>>> details = new HashMap<>();
        Map<String, List<Map<String, Object>>> pastTrack = new HashMap<>();
        details.put("pastTrack", pastTrack);
        pastTrack.put("points", null);

        MaxSpeedRecording rec = new MaxSpeedExtractor().extractMaxSpeed(details);

        Assert.assertEquals(0.0, rec.getMaxSpeed(), 0.0);
    }

    private Map<String, Object> createPoint(Double speed) {
        Map<String, Object> point = new HashMap<>();
        point.put("sog", speed);
        return point;
    }

}
