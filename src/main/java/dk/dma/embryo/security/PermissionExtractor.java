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

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;

import dk.dma.embryo.config.IllegalConfigurationException;
import dk.dma.embryo.security.authorization.Permission;

@Named
public class PermissionExtractor implements Serializable{
    
    private static final long serialVersionUID = 1434693548735954680L;
    
    public Permission[] getPermissions(Object object) {
        Annotation[] annotations = object.getClass().getAnnotations();

        if (annotations == null) {
            return new Permission[0];
        }
        List<Permission> permissions = new ArrayList<>(annotations.length);
        for (Annotation annotation : annotations) {
            Permission permission = annotation.annotationType().getAnnotation(Permission.class);

            if (permission != null) {
                if (permission.value() == null) {
                    // TODO replaceable with checkstyle rule
                    throw new IllegalConfigurationException("Invalid configuration in class "
                            + object.getClass().getSimpleName() + ". " + Permission.class.getSimpleName()
                            + " must have a value.");
                }
                permissions.add(permission);
            }

        }
        
       return permissions.toArray(new Permission[permissions.size()]);
    }
    
    public boolean hasPermissionConfiguration(Object o){
        if(o instanceof PermissionContainer){
            return true;
        }
        
        return getPermissions(o).length > 0;
    }

}
