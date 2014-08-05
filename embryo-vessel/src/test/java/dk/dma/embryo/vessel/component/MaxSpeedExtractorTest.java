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
package dk.dma.embryo.vessel.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import dk.dma.embryo.vessel.job.MaxSpeedJob.MaxSpeedRecording;

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
