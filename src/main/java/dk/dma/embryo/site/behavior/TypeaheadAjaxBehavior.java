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
package dk.dma.embryo.site.behavior;

import java.util.List;

import javax.inject.Inject;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AbstractAjaxBehavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.cycle.RequestCycle;
import org.slf4j.Logger;

import dk.dma.embryo.rest.util.TypeaheadDatum;

/**
 * Abstract class which enables Bootstrap behavior selector.
 * 
 * 
 * @author Jesper Tejlgaard
 */
public class TypeaheadAjaxBehavior<D extends TypeaheadDatum> extends AbstractAjaxBehavior {
    
    private static final long serialVersionUID = -1168601356372046736L;

    private static final String JS_INIT = "embryo.typeahead.init('inputSelector', 'url');";

    @Inject
    private Logger logger;

    // The jQuery selector to be used by the jQuery ready handler that registers the autocomplete behavior
    private final String jQuerySelector;
    
    private final TypeaheadDataSource<D> dataSource;
    
    private final boolean autoInitialize;

    /**
     * 
     * @param jQuerySelector
     *            - a string containing the jQuery selector for the target html element (<input type='text'... of the
     *            jQuery UI Autocomplete component
     */
    public TypeaheadAjaxBehavior(String jQuerySelector, TypeaheadDataSource<D> dataSource, boolean autoInitialize) {
        super();
        this.jQuerySelector = jQuerySelector;
        this.dataSource = dataSource;
        this.autoInitialize = autoInitialize;
    }

    public String getJQuerySelector(){
        return jQuerySelector;
    }
    
    public String getJsonUrl(){
        return getCallbackUrl().toString();
    }
    
    /**
     * Contributes a jQuery ready handler that registers autocomplete behavior for the html element represented by the
     * selector.
     * 
     * The generation of the ready handler uses interpolation, applying the jQuery selector and the variable name of the
     * return call back url.
     * 
     * @param component
     * @param response
     */
    @Override
    public void renderHead(Component component, IHeaderResponse response) {
        super.renderHead(component, response);
        if(autoInitialize){
            String js_init = JS_INIT.replaceAll("inputSelector", jQuerySelector);
            js_init = js_init.replaceAll("url", getCallbackUrl().toString());
            response.render(OnLoadHeaderItem.forScript(js_init));
        }
    }

    private String extractQueryRequestParameter() {
        RequestCycle requestCycle = RequestCycle.get();
        Request request = requestCycle.getRequest();
        IRequestParameters irp = request.getRequestParameters();
        String query = irp.getParameterValue("q").toString();
        logger.debug("?q={}", query);
        return query;
    }
    
    boolean isPrefetchRequest(){
        RequestCycle requestCycle = RequestCycle.get();
        IRequestParameters params = requestCycle.getRequest().getRequestParameters();
        return !params.getParameterNames().contains("q") || params.getParameterValue("q").isNull();
    }

    @Override
    public void onRequest() {
        logger.debug("request received on url {}", getCallbackUrl());

        List<D> matches;
        if(isPrefetchRequest()){
            matches = dataSource.prefetch();
        }else{
            String query = extractQueryRequestParameter();
            matches = dataSource.remoteFetch(query);
        }

        logger.debug("matches={}", matches);
        
        JsonResult json = new JsonResult(matches);

        logger.debug("json={}", json.toJson());

        RequestCycle.get().scheduleRequestHandlerAfterCurrent(new JsonRequestHandler(json));
    }
    
    
}
