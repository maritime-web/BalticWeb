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
package dk.dma.embryo.vessel.component;


import java.util.ArrayList;
import java.util.Date;

import dk.dma.embryo.common.util.Converter;
import dk.dma.enav.model.geometry.Position;
import dk.dma.enav.model.voyage.Route;
import dk.dma.enav.model.voyage.RouteLeg.Heading;

public class RouteDecorator {

    private Route route;
    
    private Long estimatedTotalTime;

    private ArrayList<Waypoint> waypoints = new ArrayList<Waypoint>();
    
    // //////////////////////////////////////////////////////////////////////
    // Constructors
    // //////////////////////////////////////////////////////////////////////

    public RouteDecorator(Route route, Date departure) {
        
        super();
        
        if(departure != null && route.getWaypoints() != null && route.getWaypoints().size() > 0) {
            route.getWaypoints().get(0).setEta(departure);
        }

        initData(route);
    }
    
    public RouteDecorator(Route route) {
        super();
        
        initData(route);
    }
    
    private void initData(Route route){
        
        this.route = route;

        Waypoint lastWp = null;
        for(dk.dma.enav.model.voyage.Waypoint wp : route.getWaypoints()){
            Waypoint waypoint = new Waypoint(wp);
            waypoints.add(waypoint);
            if(lastWp != null){
                lastWp.getRouteLeg().setTo(waypoint);
            }
            lastWp = waypoint;
        }
        
        // Reuse reference
        lastWp = null;
        
        this.estimatedTotalTime = 0L;
        
        for(Waypoint wp : waypoints){
            if(lastWp != null){
                if(lastWp.getEta() != null  && lastWp.getRouteLeg().getSpeed() != null && wp.getEta() == null){
                    long eta = lastWp.getEta().getTime() + lastWp.getRouteLeg().calcTtg();
                    wp.setEta(new Date(eta));
                    estimatedTotalTime += eta;
                }else{
                    
                }
                
            }
            lastWp = wp;
        }
        
    }

    // //////////////////////////////////////////////////////////////////////
    // Property methods
    // //////////////////////////////////////////////////////////////////////
    public String getDeparture() {
        return route.getDeparture();
    }

    public void getEstimatedTotalTime(){
        
    }
    
    public String getDestination() {
        return route.getDestination();
    }

    public Date getEta() {
        if(waypoints.size() == 0){
            return null;
        }
        return waypoints.get(waypoints.size() - 1).getEta();
    }

    public String getName() {
        return route.getName();
    }

    public void setDeparture(String departure) {
        route.setDeparture(departure);
    }

    public void setDestination(String destination) {
        route.setDestination(destination);
    }

    public void setName(String name) {
        route.setName(name);
    }

    public ArrayList<Waypoint> getWaypoints() {
        return waypoints;
    }


    
    // //////////////////////////////////////////////////////////////////////
    // Inner classes
    // //////////////////////////////////////////////////////////////////////
    public static class Waypoint{
        private dk.dma.enav.model.voyage.Waypoint waypoint;
        private RouteLeg routeLeg;
        
        Waypoint(dk.dma.enav.model.voyage.Waypoint wp){
            this.routeLeg = new RouteLeg(wp.getRouteLeg(), this);
            this.waypoint = wp;
        }
        
        
        // //////////////////////////////////////////////////////////////////////
        // Inner class Property methods
        // //////////////////////////////////////////////////////////////////////
        public Date getEta() {
            return waypoint.getEta();
        }

        public double getLatitude() {
            return waypoint.getLatitude();
        }

        public double getLongitude() {
            return waypoint.getLongitude();
        }

        public String getName() {
            return waypoint.getName();
        }

        public Double getRot() {
            return waypoint.getRot();
        }

        public RouteLeg getRouteLeg() {
            return routeLeg;
        }

        public Double getTurnRad() {
            return waypoint.getTurnRad();
        }

        public void setEta(Date eta) {
            waypoint.setEta(eta);
        }

        public void setLatitude(double latitude) {
            waypoint.setLatitude(latitude);
        }

        public void setLongitude(double longitude) {
            waypoint.setLongitude(longitude);
        }

        public void setName(String name) {
            waypoint.setName(name);
        }

        public void setRot(Double rot) {
            waypoint.setRot(rot);
        }

//        public void setRouteLeg(RouteLeg routeLeg) {
//            waypoint.setRouteLeg(routeLeg);
//        }

        public void setTurnRad(Double turnRad) {
            waypoint.setTurnRad(turnRad);
        }
    }
    
    public static class RouteLeg{
        private Waypoint from, to;
        private dk.dma.enav.model.voyage.RouteLeg routeLeg;
        
        public RouteLeg(dk.dma.enav.model.voyage.RouteLeg routeLeg, Waypoint from) {
            super();
            this.from = from;
            this.routeLeg = routeLeg;
        }
        
        
        // //////////////////////////////////////////////////////////////////////
        // Logic
        // //////////////////////////////////////////////////////////////////////
        public double calcRange() {
            double meters;
            
            Position pos1 = Position.create(from.getLatitude(), from.getLongitude());
            Position pos2 = Position.create(to.getLatitude(), to.getLongitude());
            
            if (getHeading() == Heading.RL) {
                meters = pos1.rhumbLineDistanceTo(pos2);
            } else {
                meters = pos1.geodesicDistanceTo(pos2);
            }
            return Converter.metersToNm(meters);
        }
        
        public long calcTtg() {
            if (getSpeed() < 0.1) {
                return -1L;
            }
            return Math.round(calcRange() * 3600.0 / getSpeed() * 1000.0);
        }


        
        // //////////////////////////////////////////////////////////////////////
        // Inner class Property methods
        // //////////////////////////////////////////////////////////////////////
        void setTo(Waypoint to) {
            this.to = to;
        }

        public Double getSFLen() {
            return routeLeg.getSFLen();
        }

        public Double getSFWidth() {
            return routeLeg.getSFWidth();
        }

        public Double getSpeed() {
            return routeLeg.getSpeed();
        }

        public Heading getHeading() {
            return routeLeg.getHeading();
        }

        public Double getXtdPort() {
            return routeLeg.getXtdPort();
        }

        public Double getXtdStarboard() {
            return routeLeg.getXtdStarboard();
        }

        public void setSFLen(Double sFLen) {
            routeLeg.setSFLen(sFLen);
        }

        public void setSFWidth(Double sFWidth) {
            routeLeg.setSFWidth(sFWidth);
        }

        public void setSpeed(Double speed) {
            routeLeg.setSpeed(speed);
        }

        public void setXtdPort(Double xtdPort) {
            routeLeg.setXtdPort(xtdPort);
        }

        public void setXtdStarboard(Double xtdStarboard) {
            routeLeg.setXtdStarboard(xtdStarboard);
        }
        
        public void setHeading(Heading heading) {
            routeLeg.setHeading(heading);
        }
    }    
}
