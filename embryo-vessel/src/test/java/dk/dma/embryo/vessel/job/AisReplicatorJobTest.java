package dk.dma.embryo.vessel.job;

import dk.dma.embryo.vessel.integration.AisVessel;
import dk.dma.embryo.vessel.model.Vessel;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dk.dma.embryo.vessel.job.AisReplicatorJob.VesselsToUpdateBuilder;

/**
 * Created by Jesper Tejlgaard on 8/12/15.
 */
public class AisReplicatorJobTest {

    @Test
    public void testVesselsToUpdateBuilder_AisInformationNotAvailable() {
        //INPUT DATA
        List<Vessel> awVessels = new ArrayList<>();
        awVessels.add(new Vessel(111122223L));
        awVessels.add(new Vessel(111122224L));

        Map<Long, AisVessel> aisVessels = new HashMap<>();

        // EXECUTE
        VesselsToUpdateBuilder builder = new VesselsToUpdateBuilder().setAWVessels(awVessels).setAisVessels(aisVessels);
        List<Vessel> result = builder.build();

        // NullPointerException was not thrown

        // VERIFY
        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.size());
    }
}
