package dk.dma.embryo.dataformats.model;

import dk.dma.embryo.dataformats.netcdf.NetCDFType;

public class PrognosisType extends NetCDFType {

    private Type type;
    
    public PrognosisType(String name, String code, Type type) {
        setName(name);
        setCode(code);
        this.type = type;
    }
    
    public Type getType() {
        return type;
    }
    
    
    public enum Type {
        ICE_PROGNOSIS, CURRENT_PROGNOSIS, WAVE_PROGNOSIS, WIND_PROGNOSIS 
    }
    
    @Override
    public String toString() {
        return "Prognosis type: " + getName() + " (" + type + ")";
    }
    
    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }
        if(obj == this) {
            return true;
        }
        if(obj.getClass() != getClass()) {
            return false;
        }
        return ((PrognosisType)obj).getType() == getType();
    }
    
    @Override
    public int hashCode() {
        return getType().hashCode();
    }
}
