package dk.dma.embryo.dataformats.netcdf;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class NetCDFResult {
    private Map<String, List<? extends Serializable>> metadata;
    private Map<String, List<SmallEntry>> data;
    
    public NetCDFResult(Map<String, List<? extends Serializable>> metadata, Map<String, List<SmallEntry>> data) {
        this.metadata = metadata;
        this.data = data;
    }
    
    public Map<String, List<? extends Serializable>> getMetadata() {
        return metadata;
    }
    
    public Map<String, List<SmallEntry>> getData() {
        return data;
    }
}
