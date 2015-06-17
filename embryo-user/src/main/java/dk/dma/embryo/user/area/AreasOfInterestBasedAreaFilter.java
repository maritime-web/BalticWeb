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
package dk.dma.embryo.user.area;

import dk.dma.embryo.common.area.Area;
import dk.dma.embryo.common.area.AreaFilter;
import dk.dma.embryo.user.model.SecuredUser;
import dk.dma.embryo.user.security.Subject;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Jesper Tejlgaard on 6/9/15.
 */
@RequestScoped
public class AreasOfInterestBasedAreaFilter implements AreaFilter, Serializable {

    // //////////////////////////////////////////////////////////////////////
    // Object fields
    // //////////////////////////////////////////////////////////////////////
    @Inject
    private Subject subject;
    private SecuredUser user;

    // //////////////////////////////////////////////////////////////////////
    // Business Logic
    // //////////////////////////////////////////////////////////////////////
    private boolean hasActiveAreasOfInterest() {
        if (this.user == null) {
            this.user = subject.getUser();
        }

        return this.user.hasActiveAreasOfInterest();
    }

    public Stream<Area> getAreasAsStream() {
        if (!hasActiveAreasOfInterest()) {
            return Stream.empty();
        }
        return user.getAreasOfInterest().parallelStream().filter(areasOfInterest -> areasOfInterest.getActive()).flatMap(areasOfInterest -> areasOfInterest.extractBounds());
    }

    public List<Area> getAreasByFilter(Predicate<Area> filter) {
        if (!hasActiveAreasOfInterest()) {
            return Collections.emptyList();
        }
        return user.getAreasOfInterest().parallelStream().filter(areasOfInterest -> areasOfInterest.getActive()).flatMap(areasOfInterest -> areasOfInterest.extractBounds().filter(filter)).collect(Collectors.toList());
    }

    // //////////////////////////////////////////////////////////////////////
    // Constructors
    // //////////////////////////////////////////////////////////////////////
    public AreasOfInterestBasedAreaFilter() {

    }

    public AreasOfInterestBasedAreaFilter(Subject subject) {
        this.subject = subject;
    }
}
