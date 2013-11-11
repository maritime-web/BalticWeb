package dk.dma.arcticweb.service;

import dk.dma.configuration.Property;
import dk.dma.enav.model.geometry.CoordinateSystem;
import dk.dma.enav.model.geometry.Position;

import javax.ejb.Singleton;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class AisDataServiceImpl implements AisDataService {
    private List<String[]> vesselsInAisCircle = new ArrayList<>();

    @Inject
    @Property("embryo.aisCircle.latitude")
    private double aisCircleLatitude;

    @Inject
    @Property("embryo.aisCircle.longitude")
    private double aisCircleLongitude;

    @Inject
    @Property("embryo.aisCircle.radius")
    private double aisCircleRadius;

    public List<String[]> getVesselsInAisCircle() {
        return vesselsInAisCircle;
    }

    public void setVesselsInAisCircle(List<String[]> vesselsInAisCircle) {
        this.vesselsInAisCircle = vesselsInAisCircle;
    }

    public boolean isWithinAisCircle(double x, double y) {
        return Position.create(y, x).distanceTo(Position.create(aisCircleLatitude, aisCircleLongitude), CoordinateSystem.GEODETIC) < aisCircleRadius;
    }
}
