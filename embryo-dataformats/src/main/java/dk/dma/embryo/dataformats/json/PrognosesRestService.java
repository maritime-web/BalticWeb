package dk.dma.embryo.dataformats.json;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.slf4j.Logger;

import dk.dma.embryo.dataformats.netcdf.NetCDFResult;
import dk.dma.embryo.dataformats.service.PrognosisService;

@Path("/prognoses")
public class PrognosesRestService {
    @Inject
    private PrognosisService prognosisService;

    @Inject
    private Logger logger;

    @GET
    @Path("/ice")
    @Produces("application/json")
    @GZIP
    @NoCache
    public List<String> listIcePrognoses() {
        logger.debug("listIcePrognoses()");
        return prognosisService.listAvailableIcePrognoses();
    }

    @GET
    @Path("/ice/{id}")
    @Produces("application/json")
    @GZIP
    @NoCache
    public NetCDFResult getIcePrognosis(@PathParam(value = "id") String id) {
        logger.debug("getIcePrognosis()");

        return prognosisService.getIcePrognosis(id);
    }

    @GET
    @Path("/waves")
    @Produces("application/json")
    @GZIP
    @NoCache
    public List<String> listWavePrognoses() {
        logger.debug("listWavePrognoses()");
        return prognosisService.listAvailableWavePrognoses();
    }

    @GET
    @Path("/waves/{id}")
    @Produces("application/json")
    @GZIP
    @NoCache
    public NetCDFResult getWavePrognosis(@PathParam(value = "id") String id) {
        logger.debug("getWavePrognosis()");

        return prognosisService.getWavePrognosis(id);
    }
    
    @GET
    @Path("/currents")
    @Produces("application/json")
    @GZIP
    @NoCache
    public List<String> listCurrentPrognoses() {
        logger.debug("listCurrentPrognoses()");
        return prognosisService.listAvailableCurrentPrognoses();
    }

    @GET
    @Path("/currents/{id}")
    @Produces("application/json")
    @GZIP
    @NoCache
    public NetCDFResult getCurrentPrognosis(@PathParam(value = "id") String id) {
        logger.debug("getWavePrognosis()");

        return prognosisService.getCurrentPrognosis(id);
    }
}
