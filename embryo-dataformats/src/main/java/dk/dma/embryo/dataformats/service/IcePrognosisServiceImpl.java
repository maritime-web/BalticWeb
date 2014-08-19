/* Copyright (c) 2011 Danish Maritime Authority.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
public class IcePrognosisServiceImpl implements IcePrognosisService {
    
    @Inject
    private NetCDFService netCDFService;
    
    @Override
    public List<String> listAvailableIcePrognoses() {
        List<String> names = new ArrayList<>(netCDFService.getEntries(NetCDFType.ICE_PROGNOSIS).keySet());
        Collections.sort(names, Collections.reverseOrder());
        return names;
    }
    
    @Override
    public NetCDFResult getPrognosis(String id) {
        return netCDFService.getEntries(NetCDFType.ICE_PROGNOSIS).get(id);
    }
}
