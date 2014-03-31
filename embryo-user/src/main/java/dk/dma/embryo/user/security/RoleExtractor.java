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
package dk.dma.embryo.user.security;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dk.dma.embryo.user.security.authorization.Authorization;

public class RoleExtractor implements Serializable {

    private static final long serialVersionUID = 1434693548735954680L;

    public List<Annotation> getAuthorizationConfiguration(Method method) {
        Annotation[] annotations = method.getAnnotations();

        return getAuthorizationConfiguration(annotations);
    }

    public List<Annotation> getAuthorizationConfiguration(Object object) {
        Annotation[] annotations = object.getClass().getAnnotations();
        return getAuthorizationConfiguration(annotations);
    }

    private List<Annotation> getAuthorizationConfiguration(Annotation[] annotations) {
        if (annotations == null) {
            return Collections.emptyList();
        }

        List<Annotation> roles = new ArrayList<>(annotations.length);
        for (Annotation annotation : annotations) {
            Authorization authorization = annotation.annotationType().getAnnotation(Authorization.class);
            if (authorization != null) {
                roles.add(annotation);
            }
        }

        return roles;
    }
}
