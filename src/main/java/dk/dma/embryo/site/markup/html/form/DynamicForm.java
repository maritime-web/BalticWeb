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
package dk.dma.embryo.site.markup.html.form;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IFormSubmitter;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.iterator.ComponentHierarchyIterator;

public class DynamicForm<T> extends Form<T> {

    private static final long serialVersionUID = 1L;

    public DynamicForm(String id) {
        super(id);
    }

    public DynamicForm(String id, IModel<T> model) {
        super(id, model);
    }

    @Override
    public void process(IFormSubmitter submittingComponent) {
        ComponentHierarchyIterator iterator = visitChildren(IDynamic.class);
        while(iterator.hasNext()){
            IDynamic component = (IDynamic)iterator.next();
            component.update();
        }
        
        super.process(submittingComponent);
    }
    

    
    
}
