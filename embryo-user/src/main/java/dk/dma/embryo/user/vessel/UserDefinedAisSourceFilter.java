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
package dk.dma.embryo.user.vessel;

import dk.dma.embryo.common.EmbryonicException;
import dk.dma.embryo.common.configuration.Property;
import dk.dma.embryo.user.model.SecuredUser;
import dk.dma.embryo.user.security.Subject;
import dk.dma.embryo.vessel.integration.AisSourceFilter;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import java.util.Map;

/**
 * Created by Jesper Tejlgaard on 6/11/15.
 */
@RequestScoped
public class UserDefinedAisSourceFilter implements AisSourceFilter {

    @Inject
    private Subject subject;
    private SecuredUser user;

    @Inject
    @Property("embryo.ais.filters.namedSourceFilters")
    private Map<String, String> namedSourceFilters;

    // //////////////////////////////////////////////////////////////////////
    // Builder methods (business logic)
    // //////////////////////////////////////////////////////////////////////
    private SecuredUser getUser() {
        if (user == null) {
            user = subject.getUser();
        }
        return user;
    }

    public String getAisFilter() {
        if (getUser().getAisFilterName() == null || getUser().getAisFilterName().length() == 0) {
            return null;
        }

        if (!namedSourceFilters.containsKey(getUser().getAisFilterName())) {
            throw new EmbryonicException("Named source filter '" + getUser().getAisFilterName() + "' does not exist in application configuration");
        }

        return namedSourceFilters.get(getUser().getAisFilterName());
    }

    // //////////////////////////////////////////////////////////////////////
    // Constructors
    // //////////////////////////////////////////////////////////////////////
    public UserDefinedAisSourceFilter() {
    }

    public UserDefinedAisSourceFilter(Subject subject, Map<String, String> namedSourceFilters) {
        this.subject = subject;
        this.namedSourceFilters = namedSourceFilters;
    }
}
