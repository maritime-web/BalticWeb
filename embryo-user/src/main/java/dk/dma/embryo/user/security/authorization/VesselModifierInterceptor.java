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
package dk.dma.embryo.user.security.authorization;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

import org.apache.shiro.authz.AuthorizationException;

import dk.dma.embryo.user.security.Subject;
import dk.dma.embryo.vessel.model.Vessel;

public class VesselModifierInterceptor{
    
    @Inject
    private Subject subject;
    
    @AroundInvoke
    Object onlyOwnVessel(InvocationContext ctx) throws Exception{
        Long mmsi;
        if(ctx.getParameters()[0] instanceof Vessel){
            mmsi = ((Vessel)ctx.getParameters()[0]).getMmsi();
        }else if(ctx.getParameters()[0] instanceof Long){
            mmsi = (Long)ctx.getParameters()[0];
        }else{
            throw new IllegalArgumentException("First argument must be one of types " + Vessel.class.getName() + ", " + Long.class.getName());
        }
        
        if(!subject.authorizedToModifyVessel(mmsi)){
            throw new AuthorizationException("Not authorized to modify data for vessel");
        }
        
        return ctx.proceed();
    }
}
