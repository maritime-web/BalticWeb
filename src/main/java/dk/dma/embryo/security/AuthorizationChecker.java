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
package dk.dma.embryo.security;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authz.AuthorizationException;
import org.slf4j.Logger;

import dk.dma.embryo.config.IllegalConfigurationException;
import dk.dma.embryo.domain.Role;
import dk.dma.embryo.security.authorization.Roles;
import dk.dma.embryo.security.authorization.RolesAllowAll;

/**
 * @author Jesper Tejlgaard
 */
public class AuthorizationChecker {

    @Inject
    private Subject subject;

    @Inject
    private Logger logger;

    RoleExtractor extractor = new RoleExtractor();

    @AroundInvoke
    public Object authorize(InvocationContext ctx) throws Exception {
        List<Annotation> rolesAllowed = extractor.getAuthorizationConfiguration(ctx.getMethod());
        if (rolesAllowed != null && rolesAllowed.size() > 0) {
            if (!subject.isLoggedIn()) {
                throw new AuthenticationException("Attemp to call " + getMethodName(ctx) + " without being authenticated");
            }
            
            if (isAuthorized(rolesAllowed)) {
                return ctx.proceed();
            } else {
                throw new AuthorizationException("User " + subject.getUser().getUserName() + " not authorized for "
                        + getMethodName(ctx));
            }
        }

        rolesAllowed = extractor.getAuthorizationConfiguration(ctx.getTarget());
        if (rolesAllowed != null && rolesAllowed.size() > 0) {
            if (!subject.isLoggedIn()) {
                throw new AuthenticationException("Attemp to call " + getMethodName(ctx) + " without being authenticated");
            }

            if (isAuthorized(rolesAllowed)) {
                return ctx.proceed();
            } else {
                throw new AuthorizationException("User " + subject.getUser().getUserName() + " not authorized for "
                        + getMethodName(ctx));
            }
        }

        return ctx.proceed();
    }

    private boolean isAuthorized(List<Annotation> rolesAllowed) {
        if (rolesAllowed.get(0) instanceof Roles) {
            Roles roles = (Roles) rolesAllowed.get(0);
            Class<?>[] roleTypes = roles.value();
            List<Class<? extends Role>> types = new ArrayList<>(roleTypes.length);
            for(Class<?> type : roleTypes){
                types.add((Class<? extends Role>)type);
            }
            
            return subject.hasOneOfRoles(types);
        } else if (rolesAllowed.get(0) instanceof RolesAllowAll) {
            return true;
        }
        return false;
    }

    private String getMethodName(InvocationContext ctx) {
        return ctx.getTarget().getClass().getSimpleName() + "." + ctx.getMethod().getName();
    }

    public void initialize(InvocationContext ctx) throws Exception {
        logger.debug("Initializing " + AuthorizationChecker.class.getSimpleName());

        List<Annotation> rolesAllowed = extractor.getAuthorizationConfiguration(ctx.getMethod());
        if (rolesAllowed.size() > 1) {
            throw new IllegalConfigurationException("Only one Authorization annotation allowed per method");
        }

        ctx.proceed();
    }

}
