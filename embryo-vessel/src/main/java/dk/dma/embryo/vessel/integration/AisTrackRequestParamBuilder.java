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
package dk.dma.embryo.vessel.integration;

import dk.dma.embryo.common.EmbryonicException;
import dk.dma.embryo.common.area.AreaFilter;
import dk.dma.embryo.vessel.model.Vessel;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * Created by Jesper Tejlgaard on 6/5/15.
 */
public class AisTrackRequestParamBuilder {

    private AreaFilter areaFilter;
    private String baseArea;
    private List<Long> mmsiNumbers;
    private String sourceFilter;
    private String defaultArea;

    // //////////////////////////////////////////////////////////////////////
    // Builder methods (business logic)
    // //////////////////////////////////////////////////////////////////////
    public AisTrackRequestParamBuilder includeVessels(List<Vessel> vessels) {
        mmsiNumbers = vessels.stream().map(vessel -> vessel.getMmsi()).collect(Collectors.toList());
        return this;
    }

    public AisTrackRequestParamBuilder setBaseArea(String baseArea) {
        this.baseArea = baseArea;
        return this;
    }

    public AisTrackRequestParamBuilder setDefaultArea(String defaultArea) {
        this.defaultArea = defaultArea;
        return this;
    }

    public AisTrackRequestParamBuilder addUserSelectedAreas(AreaFilter areaFilter) {
        this.areaFilter = areaFilter;
        return this;
    }

    public AisTrackRequestParamBuilder setSourceFilter(String defaultSourceFilter, AisSourceFilter aisSourceFilter) {
        if (defaultSourceFilter != null && defaultSourceFilter.trim().length() == 0) {
            defaultSourceFilter = null;
        }

        String sourceFilter = aisSourceFilter == null ? null : aisSourceFilter.getAisFilter();
        this.sourceFilter = sourceFilter == null || sourceFilter.trim().length() == 0 ? defaultSourceFilter : sourceFilter;
        return this;
    }

    // //////////////////////////////////////////////////////////////////////
    // Constructors
    // //////////////////////////////////////////////////////////////////////

    // //////////////////////////////////////////////////////////////////////
    // Object methods
    // //////////////////////////////////////////////////////////////////////
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

    public String requestValuesAsString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getClass().getSimpleName()).append("{").append("baseArea=").append(getBaseArea()).append(", ");
        builder.append("sourceFilter=").append(getSourceFilter()).append(",");
        builder.append("mmsiNumbers=").append(getMmsiNumbers()).append(",");
        builder.append("userSelectedAreas=").append(getUserSelectedAreas()).append("}");
        return builder.toString();
    }

    // //////////////////////////////////////////////////////////////////////
    // Property methods
    // //////////////////////////////////////////////////////////////////////
    public List<Long> getMmsiNumbers() {
        return mmsiNumbers;
    }

    public String getSourceFilter() {
        return sourceFilter;
    }

    public String getBaseArea() {
        return baseArea;
    }

    public List<String> getUserSelectedAreas() {
        if (areaFilter == null) {
            return null;
        }

        List<String> areas = areaFilter.getAreasAsStream().map(
                area -> {
                    StringJoiner joiner = new StringJoiner("|");
                    joiner.add(Double.toString(area.getBottom()));
                    joiner.add(Double.toString(area.getLeft()));
                    joiner.add(Double.toString(area.getTop()));
                    joiner.add(Double.toString(area.getRight()));
                    return joiner.toString();
                }).collect(Collectors.toList());

        if (areas.size() == 0 && defaultArea != null && defaultArea.length() > 0) {
            try {
                areas.add(URLEncoder.encode(defaultArea, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new EmbryonicException(e);
            }
        }
        return areas;
    }


}
