package dk.dma.embryo.vessel.integration;


/**
 * Created by Jesper Tejlgaard on 12/22/15.
 */
public enum VesselType {
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

    private String text;

    VesselType(String text) {
        this.text = text;
    }

    public static VesselType getShipTypeFromTypeText(String typeText) {
        for (VesselType shipType : VesselType.values()) {
            if (shipType.text.equalsIgnoreCase(typeText)) {
                return shipType;
            }
        }
        return UNDEFINED;
    }

    public void getText() {

    }
}
