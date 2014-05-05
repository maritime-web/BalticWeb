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
package dk.dma.embryo.dataformats.job;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

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
