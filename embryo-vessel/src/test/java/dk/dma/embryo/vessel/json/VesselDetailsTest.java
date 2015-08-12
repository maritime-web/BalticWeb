package dk.dma.embryo.vessel.json;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Jesper Tejlgaard on 8/12/15.
 */
public class VesselDetailsTest {

    @Test
    public void testGetMmsiNumber_noMmsiNoAisVessel() {
        VesselDetails details = new VesselDetails();
        Assert.assertNull(details.getMmsiNumber());
    }
}
