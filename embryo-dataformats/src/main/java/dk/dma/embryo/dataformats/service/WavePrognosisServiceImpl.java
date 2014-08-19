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
package dk.dma.embryo.dataformats.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import dk.dma.embryo.dataformats.netcdf.NetCDFResult;
import dk.dma.embryo.dataformats.netcdf.NetCDFType;

@Stateless
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
public class WavePrognosisServiceImpl implements WavePrognosisService {
    
    @Inject
    private NetCDFService netCDFService;
    
    @Override
    public List<String> listAvailableWavePrognoses() {
        List<String> names = new ArrayList<>(netCDFService.getEntries(NetCDFType.WAVE_PROGNOSIS).keySet());
        Collections.sort(names, Collections.reverseOrder());
        return names;
    }
    
    @Override
    public NetCDFResult getPrognosis(String id) {
        return netCDFService.getEntries(NetCDFType.WAVE_PROGNOSIS).get(id);
    }
}
