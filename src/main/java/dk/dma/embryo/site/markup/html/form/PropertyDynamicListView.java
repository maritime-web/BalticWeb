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

import java.util.List;

import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;

/**
 * 
 * @author Jesper Tejlgaard
 *
 * @param <T>
 */
public abstract class PropertyDynamicListView<T> extends DynamicListView<T> {

    private static final long serialVersionUID = 1L;
    
    public PropertyDynamicListView(String id, IModel<? extends List<? extends T>> model) {
        super(id, model);
    }

    public PropertyDynamicListView(String id, List<? extends T> list) {
        super(id, list);
    }

    public PropertyDynamicListView(String id) {
        super(id);
    }
    

    /**
     * Wraps a ListItemModel in a CompoundPropertyModel.
     * 
     * @param model
     * @param index
     * @return a CompoundPropertyModel wrapping a ListItemModel
     */
    @Override
    protected IModel<T> getListItemModel(final IModel<? extends List<T>> model, final int index)
    {
        return new CompoundPropertyModel<T>(super.getListItemModel(model, index));
    }
}
