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
package dk.dma.embryo.user.vessel.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.enterprise.inject.Default;
import javax.inject.Inject;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import dk.dma.embryo.user.model.SecuredUser;
import dk.dma.embryo.user.model.SelectionGroup;
import dk.dma.embryo.user.security.Subject;
import dk.dma.embryo.vessel.job.filter.UserSelectionGroupsFilter;
import dk.dma.embryo.vessel.json.VesselOverview;

@Default
public class UserSelectionGroupsFilterImpl implements UserSelectionGroupsFilter {

    private static final long serialVersionUID = -7771436245663646148L;
    final boolean MATCH = true;
    final boolean NO_MATCH = false;

    final String LEFT = "left";
    final String RIGHT = "right";
    final String BOTTOM = "bottom";
    final String TOP = "top";

    @Inject
    private Subject subject;

    private List<LinkedHashMap<String, Double>> allBounds;
    private SecuredUser user;

    @Override
    public boolean isVesselInActiveUserSelectionGroups(VesselOverview vessel) {

        if(this.user == null) {
            this.user = subject.getUser();
        }

        if(user.hasActiveSelectionGroups()) {

            // Concatenate all Bounds across groups
            if(this.allBounds == null) {
                extractAllBoundsForActiveSelectionGroups(user.getSelectionGroups());
            }

            // Find out if vessel position match within at least 1 Bounds/square
            if(this.vesselPositionIsWithinSelectionGroups(vessel)) {
                return MATCH;
            }
        }

        return NO_MATCH;
    }

    private boolean vesselPositionIsWithinSelectionGroups(VesselOverview vessel) {

        for (LinkedHashMap<String, Double> square : this.allBounds) {

            if(vessel.getX() > square.get(LEFT) && 
                    vessel.getX() < square.get(RIGHT) &&
                    vessel.getY() > square.get(BOTTOM) &&
                    vessel.getY() < square.get(TOP)) {

                return MATCH;
            }
        }

        return NO_MATCH;
    }

    private void extractAllBoundsForActiveSelectionGroups(List<SelectionGroup> groups){

        for (SelectionGroup selectionGroup : groups) {

            if(selectionGroup.getActive()) {

                this.extractBounds(selectionGroup);
            }
        }
    }

    private void extractBounds(SelectionGroup group) {

        if(group.getPolygonsAsJson() != null && !group.getPolygonsAsJson().trim().equalsIgnoreCase("[]")) {
            ObjectMapper mapper = new ObjectMapper();

            try {

                ArrayList<LinkedHashMap<String, Double>> bounds = (ArrayList<LinkedHashMap<String, Double>>)mapper.readValue(group.getPolygonsAsJson(), ArrayList.class);

                this.addBounds(bounds);
            } catch (JsonParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (JsonMappingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private void addBounds(List<LinkedHashMap<String, Double>> bounds) {

        if(this.allBounds == null) {
            this.allBounds = new ArrayList<LinkedHashMap<String, Double>>();
        }

        this.allBounds.addAll(bounds);
    }

    @Override
    public boolean loggedOnUserHasSelectionGroups() {

        return subject.getUser().hasActiveSelectionGroups();
    }
}
