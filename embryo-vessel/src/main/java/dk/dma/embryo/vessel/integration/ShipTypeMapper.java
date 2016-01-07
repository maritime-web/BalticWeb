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
package dk.dma.embryo.vessel.integration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ShipTypeMapper {

    /**
     * Differentiating colors used
     */
    public enum ShipTypeColor {
        BLUE, GREY, GREEN, ORANGE, PURPLE, RED, TURQUOISE, YELLOW
    }

    /**
     * Map from ship type to color
     */
    private Map<VesselType, ShipTypeColor> shipTypeToColorMap = new HashMap<>();

    /**
     * Map from color to list of ship types
     */
    private Map<ShipTypeColor, List<VesselType>> colorToShipTypeMap = new HashMap<>();

    private static ShipTypeMapper instance;

    private ShipTypeMapper() {
        shipTypeToColorMap.put(VesselType.PASSENGER, ShipTypeColor.BLUE);

        shipTypeToColorMap.put(VesselType.CARGO, ShipTypeColor.GREEN);

        shipTypeToColorMap.put(VesselType.TANKER, ShipTypeColor.RED);

        shipTypeToColorMap.put(VesselType.HSC, ShipTypeColor.YELLOW);
        shipTypeToColorMap.put(VesselType.WIG, ShipTypeColor.YELLOW);

        shipTypeToColorMap.put(VesselType.UNDEFINED, ShipTypeColor.GREY);
        shipTypeToColorMap.put(VesselType.UNKNOWN, ShipTypeColor.GREY);

        shipTypeToColorMap.put(VesselType.FISHING, ShipTypeColor.ORANGE);

        shipTypeToColorMap.put(VesselType.SAILING, ShipTypeColor.PURPLE);
        shipTypeToColorMap.put(VesselType.PLEASURE, ShipTypeColor.PURPLE);

        // The rest is turquoise
        for (VesselType shipType : VesselType.values()) {
            if (shipTypeToColorMap.containsKey(shipType)) {
                continue;
            }
            shipTypeToColorMap.put(shipType, ShipTypeColor.TURQUOISE);
        }

        // Initialize array
        for (ShipTypeColor color : ShipTypeColor.values()) {
            List<VesselType> list = new ArrayList<VesselType>();
            colorToShipTypeMap.put(color, list);
        }

        // Fill reverse map
        for (VesselType shipType : shipTypeToColorMap.keySet()) {
            ShipTypeColor color = shipTypeToColorMap.get(shipType);
            colorToShipTypeMap.get(color).add(shipType);
        }

    }

    public ShipTypeColor getColor(VesselType shipType) {
        return shipTypeToColorMap.get(shipType);
    }

    public static ShipTypeMapper getInstance() {
        synchronized (ShipTypeMapper.class) {
            if (instance == null) {
                instance = new ShipTypeMapper();
            }
            return instance;
        }
    }
}
