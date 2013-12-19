package dk.dma.embryo.security.authorization;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

import org.apache.shiro.authz.AuthorizationException;

import dk.dma.embryo.domain.Vessel;
import dk.dma.embryo.security.Subject;

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
