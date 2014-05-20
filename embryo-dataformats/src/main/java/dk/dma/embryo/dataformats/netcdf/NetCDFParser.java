package dk.dma.embryo.dataformats.netcdf;

import ucar.nc2.NetcdfFile;

public class NetCDFParser {
    public void parse(String filename) throws Exception {
        NetcdfFile netcdfFile = NetcdfFile.open(filename);
        
    }
}
