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
package dk.dma.embryo.site.panel;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;

public abstract class EmbryonicForm<T> extends Form<T> {

    private static final long serialVersionUID = 4132041261965905788L;

    private final String title;
    
    public EmbryonicForm(String id, String title) {
        super(id);
        
        add(new Label("title", title));
        
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
    
}
