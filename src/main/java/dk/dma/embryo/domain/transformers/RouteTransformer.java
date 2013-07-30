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
package dk.dma.embryo.domain.transformers;

import dk.dma.embryo.domain.Position;
import dk.dma.embryo.domain.Route;
import dk.dma.embryo.domain.RouteLeg;
import dk.dma.embryo.domain.WayPoint;
import dk.dma.enav.model.voyage.Waypoint;


public class RouteTransformer {

    public Route transform(dk.dma.enav.model.voyage.Route route){
        Route transformedRoute = new Route(route.getName(), route.getDeparture(), route.getDestination());
        
        for(Waypoint wayPoint : route.getWaypoints()){
            transformedRoute.addWayPoint(transform(wayPoint));
        }
        
        return transformedRoute;
    }
    
    public WayPoint transform(Waypoint wayPoint){
        WayPoint transformed = new WayPoint();
        transformed.setName(wayPoint.getName());
        transformed.setPosition(new Position(wayPoint.getLatitude(), wayPoint.getLongitude()));
        transformed.setRot(wayPoint.getRot());
        transformed.setTurnRadius(wayPoint.getTurnRad());
        
        transformed.setLeg(transform(wayPoint.getRouteLeg()));
        
        return transformed;
    }
    
    public RouteLeg transform(dk.dma.enav.model.voyage.RouteLeg leg){
        return new RouteLeg(leg.getSpeed(), leg.getXtdPort(), leg.getXtdStarboard());
    }
}
