package dk.dma.embryo.vessel.job;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dk.dma.embryo.vessel.job.ShipTypeCargo;
import dk.dma.embryo.vessel.job.ShipTypeCargo.ShipType;

public class ShipTypeMapper {

	/**
     * Differentiating colors used
     */
    public enum ShipTypeColor {
        BLUE, GREY, GREEN, ORANGE, PURPLE, RED, TURQUOISE, YELLOW
    }

    /**
     * Map from ship type to color
     */
    private Map<ShipType, ShipTypeColor> shipTypeToColorMap = new HashMap<ShipTypeCargo.ShipType, ShipTypeMapper.ShipTypeColor>();

    /**
     * Map from color to list of ship types
     */
    private Map<ShipTypeColor, List<ShipType>> colorToShipTypeMap = new HashMap<ShipTypeMapper.ShipTypeColor, List<ShipType>>();

    private static ShipTypeMapper instance;

    private ShipTypeMapper() {
        shipTypeToColorMap.put(ShipType.PASSENGER, ShipTypeColor.BLUE);

        shipTypeToColorMap.put(ShipType.CARGO, ShipTypeColor.GREEN);

        shipTypeToColorMap.put(ShipType.TANKER, ShipTypeColor.RED);

        shipTypeToColorMap.put(ShipType.HSC, ShipTypeColor.YELLOW);
        shipTypeToColorMap.put(ShipType.WIG, ShipTypeColor.YELLOW);

        shipTypeToColorMap.put(ShipType.UNDEFINED, ShipTypeColor.GREY);
        shipTypeToColorMap.put(ShipType.UNKNOWN, ShipTypeColor.GREY);

        shipTypeToColorMap.put(ShipType.FISHING, ShipTypeColor.ORANGE);

        shipTypeToColorMap.put(ShipType.SAILING, ShipTypeColor.PURPLE);
        shipTypeToColorMap.put(ShipType.PLEASURE, ShipTypeColor.PURPLE);

        // The rest is turquoise
        for (ShipType shipType : ShipType.values()) {
            if (shipTypeToColorMap.containsKey(shipType)) {
                continue;
            }
            shipTypeToColorMap.put(shipType, ShipTypeColor.TURQUOISE);
        }

        // Initialize array
        for (ShipTypeColor color : ShipTypeColor.values()) {
            List<ShipType> list = new ArrayList<ShipType>();
            colorToShipTypeMap.put(color, list);
        }

        // Fill reverse map
        for (ShipType shipType : shipTypeToColorMap.keySet()) {
            ShipTypeColor color = shipTypeToColorMap.get(shipType);
            colorToShipTypeMap.get(color).add(shipType);
        }

    }

    public ShipTypeColor getColor(ShipType shipType) {
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
