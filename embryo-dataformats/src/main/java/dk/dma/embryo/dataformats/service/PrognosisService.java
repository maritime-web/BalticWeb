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

import java.util.List;

import dk.dma.embryo.dataformats.model.PrognosisType;
import dk.dma.embryo.dataformats.model.PrognosisType.Type;

public interface PrognosisService {
    List<PrognosisType> getPrognosisTypes();

    PrognosisType getPrognosisType(Type type);

    void reParse();

    String getPrognosis(String id, Type prognosisType);

    List<String> getPrognosisList(Type type);

    List<String> listAvailableIcePrognoses();

    String getIcePrognosis(String id);

    List<String> listAvailableWavePrognoses();

    String getWavePrognosis(String id);
    
    List<String> listAvailableCurrentPrognoses();
    
    String getCurrentPrognosis(String id);

}
