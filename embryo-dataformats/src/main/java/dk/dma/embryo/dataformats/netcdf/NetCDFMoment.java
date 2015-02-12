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
package dk.dma.embryo.dataformats.netcdf;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class NetCDFMoment implements Serializable {

    private static final long serialVersionUID = 3229249855444903196L;

    private int time;

    @JsonSerialize(contentUsing = FloatSerializer.class)
    private Map<NetCDFPoint, Map<Integer, Float>> entries = new HashMap<>();

    public NetCDFMoment(int time) {
        this.time = time;
    }

    public void addEntry(NetCDFPoint point, int order, Float obs) {
        if (entries.containsKey(point)) {
            entries.get(point).put(order, obs);
        } else {
            Map<Integer, Float> observations = new HashMap<>();
            observations.put(order, obs);
            entries.put(point, observations);
        }
    }

    public Map<NetCDFPoint, Map<Integer, Float>> getEntries() {
        return entries;
    }

    public int getTime() {
        return time;
    }

    @Override
    public String toString() {
        return "Time: " + time + ", entries: " + entries;
    }

    public static class FloatSerializer extends JsonSerializer<Map<Integer, Float>> {
        @Override
        public void serialize(Map<Integer, Float> map, JsonGenerator generator, SerializerProvider provider) throws IOException {
            generator.writeStartObject();
            for (Integer key : map.keySet()) {
                generator.writeFieldName(key.toString());
                float val = map.get(key);
                if (val == 0f) {
                    generator.writeNumber(0);
                } else {
                    generator.writeNumber(val);
                }
            }
            generator.writeEndObject();
        }

    }
}
