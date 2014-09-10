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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.dma.embryo.dataformats.model.PrognosisType;
import dk.dma.embryo.dataformats.model.PrognosisType.Type;
import dk.dma.embryo.dataformats.netcdf.NetCDFResult;
import dk.dma.embryo.dataformats.netcdf.NetCDFVar;

@Singleton
public class PrognosisServiceImpl implements PrognosisService {

    private final Logger logger = LoggerFactory.getLogger(PrognosisServiceImpl.class);

    private List<PrognosisType> prognosisTypes = new ArrayList<>();

    @Inject
    private NetCDFService netCDFService;

    @PostConstruct
    public void init() {
        prognosisTypes = createData();
        reParse();
    }

    @Override
    public List<PrognosisType> getPrognosisTypes() {
        return prognosisTypes;
    }

    @Override
    public PrognosisType getPrognosisType(Type type) {
        for (PrognosisType t : prognosisTypes) {
            if (t.getType() == type) {
                return t;
            }
        }
        return null;
    }

    public List<String> getPrognosisList(Type type) {
        Map<String, NetCDFResult> entries = netCDFService.getEntries(getPrognosisType(type));
        return new ArrayList<String>(entries.keySet());
    }

    @Override
    public NetCDFResult getPrognosis(String id, Type type) {
        return netCDFService.getEntries(getPrognosisType(type)).get(id);
    }

    @Override
    public List<String> listAvailableIcePrognoses() {
        return getPrognosisList(Type.ICE_PROGNOSIS);
    }

    @Override
    public NetCDFResult getIcePrognosis(String id) {
        return getPrognosis(id, Type.ICE_PROGNOSIS);
    }

    @Override
    public List<String> listAvailableWavePrognoses() {
        return getPrognosisList(Type.WAVE_PROGNOSIS);
    }

    @Override
    public NetCDFResult getWavePrognosis(String id) {
        return getPrognosis(id, Type.WAVE_PROGNOSIS);
    }

    @Override
    public List<String> listAvailableCurrentPrognoses() {
        return getPrognosisList(Type.CURRENT_PROGNOSIS);
    }

    @Override
    public NetCDFResult getCurrentPrognosis(String id) {
        return getPrognosis(id, Type.CURRENT_PROGNOSIS);
    }

    @Override
    public void reParse() {
        try {
            logger.info("Reparsing NetCDF files.");
            netCDFService.parseAllFiles(prognosisTypes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<PrognosisType> createData() {

        List<PrognosisType> types = new ArrayList<>();

        // Set up prognosis types
        PrognosisType icePrognosisType = new PrognosisType("Ice prognosis", "ice", Type.ICE_PROGNOSIS);
        Map<String, NetCDFVar> iceVars = icePrognosisType.getVars();

        // DMI
        NetCDFVar.addToMap(iceVars, "ice-concentration", "Ice concentration");
        NetCDFVar.addToMap(iceVars, "ice-thickness", "Ice thickness");
        NetCDFVar.addToMap(iceVars, "u-ice", "Ice speed east");
        NetCDFVar.addToMap(iceVars, "v-ice", "Ice speed north");
        NetCDFVar.addToMap(iceVars, "Icing", "Ice accretion risk");

        // FCOO
        NetCDFVar.addToMap(iceVars, "ICE", "Ice concentration");

        types.add(icePrognosisType);

        PrognosisType currentPrognosisType = new PrognosisType("Current prognosis", "current", Type.CURRENT_PROGNOSIS);
        Map<String, NetCDFVar> currentVars = currentPrognosisType.getVars();

        // DMI
        NetCDFVar.addToMap(currentVars, "u-current", "Current east");
        NetCDFVar.addToMap(currentVars, "v-current", "Current north");

        types.add(currentPrognosisType);

        PrognosisType wavePrognosisType = new PrognosisType("Wave prognosis", "wave", Type.WAVE_PROGNOSIS);
        Map<String, NetCDFVar> waveVars = wavePrognosisType.getVars();

        // DMI
        NetCDFVar.addToMap(waveVars, "var229", "Significant wave height");
        NetCDFVar.addToMap(waveVars, "var230", "Wave direction");
        NetCDFVar.addToMap(waveVars, "var232", "Wave mean period");

        // FCOO
        NetCDFVar.addToMap(waveVars, "DIRMN", "Mean wave direction");
        NetCDFVar.addToMap(waveVars, "Hs", "Wave height");
        NetCDFVar.addToMap(waveVars, "TMN", "Mean wave period");
        NetCDFVar.addToMap(waveVars, "Tz", "Zero upcrossing period");
        NetCDFVar.addToMap(waveVars, "DEPTH", "Water depth");

        types.add(wavePrognosisType);

        PrognosisType windPrognosisType = new PrognosisType("Wind prognosis", "wind", Type.WIND_PROGNOSIS);
        Map<String, NetCDFVar> windVars = windPrognosisType.getVars();

        // DMI
        NetCDFVar.addToMap(windVars, "var245", "Wind speed");
        NetCDFVar.addToMap(windVars, "var249", "Wind direction");

        // FCOO
        NetCDFVar.addToMap(windVars, "WU", "Wind speed east");
        NetCDFVar.addToMap(windVars, "WV", "Wind speed north");

        types.add(windPrognosisType);

        return types;
    }

}
