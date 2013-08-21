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

import javax.inject.Inject;

import org.apache.wicket.Component;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.string.StringValue;
import org.slf4j.Logger;

import dk.dma.embryo.EmbryonicException;

/**
 * 
 * @author Jesper Tejlgaard
 * 
 * @param <T>
 */
public abstract class DynamicListView<T> extends ListView<T> implements IDynamic {

    @Inject
    private Logger logger;

    private static final long serialVersionUID = 1L;

    private static final String JS_INIT = "embryo.dynamicListView.init('#id', 'name', autoExpand);";
    private String js_init;

    private String name;

    private Class<T> type;

    private boolean autoExpand;

    public DynamicListView(String id, IModel<? extends List<? extends T>> model) {
        super(id, model);
    }

    public DynamicListView(String id, List<? extends T> list) {
        super(id, list);
    }

    public DynamicListView(String id) {
        super(id);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        name = this.getPageRelativePath() + ":" + getId();

        js_init = JS_INIT.replaceAll("id", this.getMarkupId());
        js_init = js_init.replaceAll("name", name);
        js_init = js_init.replaceAll("autoExpand", Boolean.toString(autoExpand));

        super.setReuseItems(true);
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        // initialize component

        response.render(OnLoadHeaderItem.forScript(js_init));
    }

    @Override
    public final void update() {
        Integer count = extractCount();

        updateList(count);
    }

    private Integer extractCount() {
        StringValue value = RequestCycle.get().getRequest().getRequestParameters().getParameterValue(name);
        if (value.isEmpty()) {
            logger.error("request parameter '{}' not available as expected.");
            return null;
        }

        return value.toInteger();
    }

    protected void updateList(Integer count) {
        List<T> list = getModelObject();

        if (count != list.size()) {
            // decrease list if greater than count
            for (int i = list.size(); i > count; i--) {
                list.remove(i);
            }

            // increase list if less than count
            for (int i = list.size(); i < count; i++) {
                list.add(newItemInstance());
            }

            removeAll();
            onPopulate();
        }
        
        if(autoExpand){
            list.remove(list.size()-1);
        }
    }

    /**
     * Method uses the default constructor of type <T> to create new instances. Type must be known. You must call
     * {@link #setInstanceType(Class)} if model object (list) can be empty.
     * 
     * <p>
     * Overwrite this method to create new empty instances using a different strategy.
     * </p>
     * 
     * @return
     */
    protected T newItemInstance() {
        if (type == null) {
            throw new NullPointerException(
                    "type must be set or this method must be overwritten to supply new instances");
        }
        try {
            return type.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new EmbryonicException(e);
        }
    }

    public DynamicListView<T> setInstanceType(Class<T> type) {
        this.type = type;
        return this;
    }

    /**
     * If set to true {@link DynamicListView} support displaying an extra row, which is auto expanded provided <input>
     * elements are available in the markup.
     */
    public final DynamicListView<T> setAutoExpand(boolean b) {
        this.autoExpand = b;
        return this;
    }

    @Override
    protected void onBeforeRender() {
        System.out.println("onBeforeRender");
        
        if(autoExpand){
            getModelObject().add(newItemInstance());
        }
        super.onBeforeRender();
    }

    @Override
    protected void onModelChanged() {
        // TODO Auto-generated method stub
        super.onModelChanged();
    }
    
    

}
