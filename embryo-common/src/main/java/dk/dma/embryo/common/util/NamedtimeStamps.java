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
package dk.dma.embryo.common.util;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Jesper Tejlgaard
 */
public class NamedtimeStamps {
    private Map<String, DateTime> notifications = new HashMap<>();

    public void clearOldThanMinutes(int minutes) {
        DateTime now = DateTime.now(DateTimeZone.UTC);

        List<String> toDelete = new ArrayList<>(notifications.size());

        for (Entry<String, DateTime> entry : notifications.entrySet()) {
            if (entry.getValue().plusMinutes(minutes).isBefore(now)) {
                toDelete.add(entry.getKey());
            }
        }

        for (String name : toDelete) {
            notifications.remove(name);
        }
    }

    public boolean contains(String name) {
        return notifications.containsKey(name);
    }

    public void add(String name, DateTime ts) {
        notifications.put(name, ts);
    }

}
