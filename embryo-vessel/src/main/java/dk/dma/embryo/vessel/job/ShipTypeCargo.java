package dk.dma.embryo.vessel.job;

public class ShipTypeCargo {

	public enum ShipType {
		UNDEFINED("Undefined"), 
		WIG("WIG"), 
		PILOT("Pilot"), 
		SAR("Sar"), 
		TUG("Tug"), 
		PORT_TENDER("Port tender"), 
		ANTI_POLLUTION("Anti pollution"), 
		LAW_ENFORCEMENT("Law enforcement"), 
		MEDICAL("Medical"), 
		FISHING("Fishing"), 
		TOWING("Towing"), 
		TOWING_LONG_WIDE("Towing Long/Wide"), 
		DREDGING("Dredging"), 
		DIVING("Diving"), 
		MILITARY("Military"), 
		SAILING("Sailing"), 
		PLEASURE("Pleasure"), 
		HSC("HSC"), 
		PASSENGER("Passenger"), 
		CARGO("Cargo"), 
		TANKER("Tanker"), 
		SHIPS_ACCORDING_TO_RR("Ships according to RR"), 
		UNKNOWN("Unknown");
		
		private String subType;
		
		ShipType(String subType) {
			this.subType = subType;
		}
		
		public static ShipType getShipTypeFromSubType(String subType) {
			
			for (ShipType shipType : ShipType.values()) {
				if(shipType.subType.equalsIgnoreCase(subType)) {
					return shipType;
				}
			}
			
			return UNDEFINED;
		}
	}
}