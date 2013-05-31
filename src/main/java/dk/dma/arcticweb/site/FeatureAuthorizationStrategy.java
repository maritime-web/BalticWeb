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
package dk.dma.arcticweb.site;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.apache.wicket.Component;
import org.apache.wicket.authorization.Action;
import org.apache.wicket.authorization.IAuthorizationStrategy;
import org.apache.wicket.request.component.IRequestableComponent;

import dk.dma.arcticweb.domain.authorization.IllegalConfigurationException;
import dk.dma.arcticweb.domain.authorization.features.Permission;

/**
 * Simple authentication strategy
 */
public class FeatureAuthorizationStrategy implements IAuthorizationStrategy {

    public FeatureAuthorizationStrategy() {
    }

    public FeatureAuthorizationStrategy init() {
        return this;
    }

    @Override
    public <T extends IRequestableComponent> boolean isInstantiationAuthorized(Class<T> componentClass) {
        return true;
    }

    @Override
    public boolean isActionAuthorized(Component component, Action action) {
        if (action.equals(Action.RENDER)) {

            List<Permission> permissions = getPermissions(component);
            if (!permissions.isEmpty()) {
                Subject subject = SecurityUtils.getSubject();

                for (Permission permission : permissions) {
                    if (subject.isPermitted(permission.value())) {
                        return true;
                    }
                }
                return false;
            }
        }

        return true;
    }

    private List<Permission> getPermissions(Component component) {
        Annotation[] annotations = component.getClass().getAnnotations();

        if (annotations == null) {
            return Collections.emptyList();
        }
        List<Permission> permissions = new ArrayList<>(annotations.length);
        for (Annotation annotation : annotations) {
            Permission permission = annotation.annotationType().getAnnotation(Permission.class);

            if (permission.value() == null) {
                // TODO replaceable with checkstyle rule
                throw new IllegalConfigurationException("Invalid configuration in class "
                        + component.getClass().getSimpleName() + ". " + Permission.class.getSimpleName()
                        + " must have a value.");
            }

            if (permission != null) {
                permissions.add(permission);
            }
        }
        return permissions;
    }

}
