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
package dk.dma.embryo.site.component;

import java.io.Serializable;
import java.lang.reflect.Constructor;

import javax.enterprise.context.ApplicationScoped;

import org.apache.wicket.Component;

@ApplicationScoped
public class ReflectiveComponentFactory implements Serializable {
    private static final long serialVersionUID = -4587243549845349456L;

    public <CT extends Component> CT createComponent(Class<CT> componentType, String componentId) {
        try {
            Constructor<CT> constructor = componentType.getConstructor(String.class);
            return constructor.newInstance(componentId);
        } catch (ReflectiveOperationException | SecurityException | IllegalArgumentException e) {
            throw new ComponentCreationException(e);
        }
    }
}
