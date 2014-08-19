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
