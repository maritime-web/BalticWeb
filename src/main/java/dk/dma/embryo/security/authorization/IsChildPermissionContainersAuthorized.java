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
package dk.dma.embryo.security.authorization;


import org.apache.wicket.Component;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;

import dk.dma.embryo.security.PermissionExtractor;
import dk.dma.embryo.security.Subject;




public class IsChildPermissionContainersAuthorized implements IVisitor<Component, Boolean> {

    private boolean result = false;
    
    private Subject subject;
    
    private PermissionExtractor extractor;
    
    public IsChildPermissionContainersAuthorized(Subject subject, PermissionExtractor extractor) {
        this.subject = subject;
        this.extractor = extractor;
    }
    
    public boolean getResult(){
        return result;
    }

    @Override
    public void component(Component object, IVisit<Boolean> visit) {
        if(extractor.hasPermissionConfiguration(object) && subject.isPermitted(object)){
            result = true;
            visit.stop(Boolean.TRUE);
        }
    }
}
