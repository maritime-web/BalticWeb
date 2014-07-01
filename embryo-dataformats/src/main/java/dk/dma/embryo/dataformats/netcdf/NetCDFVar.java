/* Copyright (c) 2011 Danish Maritime Authority
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dma.embryo.dataformats.netcdf;

import java.util.Map;

import ucar.nc2.Variable;

/**
 * This class maps a NetCDF variable to an ArcticWeb concept.
 * 
 * Simple variables are dimensions like latitude, longitude and time. Complex
 * variables are variables containing a multiple of the above mentioned
 * dimensions.
 * 
 * @author avlund
 *
 */
public class NetCDFVar {
    private String varname, description;
    private Variable variable;
    private boolean complex;

    public NetCDFVar(String varname, String description, boolean complex) {
        this.varname = varname;
        this.description = description;
        this.complex = complex;
    }

    public String getVarname() {
        return varname;
    }

    public String getDescription() {
        return description;
    }

    public Variable getVariable() {
        return variable;
    }

    public void setVariable(Variable variable) {
        this.variable = variable;
    }

    public boolean isComplex() {
        return complex;
    }

    /**
     * Convenience method to add a CDF variable to a map using the varname as map key. 
     * 
     * @param map
     * @param varname
     * @param description
     * @param complex
     */
    public static void addToMap(Map<String, NetCDFVar> map, String varname, String description, boolean complex) {
        map.put(varname, new NetCDFVar(varname, description, complex));
    }
}
