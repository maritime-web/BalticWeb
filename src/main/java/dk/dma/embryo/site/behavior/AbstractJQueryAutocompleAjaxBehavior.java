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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AbstractAjaxBehavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.TextRequestHandler;
import org.apache.wicket.util.string.StringValue;
import org.apache.wicket.util.template.PackageTextTemplate;
import org.slf4j.Logger;

import com.google.gson.Gson;

public abstract class AbstractJQueryAutocompleAjaxBehavior extends AbstractAjaxBehavior {
    
    @Inject
    private Logger logger;

    // The jQuery selector to be used by the jQuery ready handler that registers the autocomplete behavior
    final private String jQuerySelector;

    /**
     * Constructor
     * 
     * @param jQuerySelector
     *            - a string containing the jQuery selector for the target html element (<input type='text'... of the
     *            jQuery UI Autocomplete component
     */
    public AbstractJQueryAutocompleAjaxBehavior(String jQuerySelector) {
        super();
        this.jQuerySelector = jQuerySelector;
    }
    
    /*
     * Convert List to json object.
     * 
     * Dependency on google-gson library which is 
     * available at http://code.google.com/p/google-gson/
     * and which must be on your classpath when using this
     * library.
     */
    private String convertListToJson(List<?> matches) {
        Gson gson = new Gson();
        String json = gson.toJson(matches);
        return json;
    }
    
    public abstract List<?> getMatches(String term);

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

        Map<String, CharSequence> map = new HashMap<String, CharSequence>(2);
        map.put("selector", jQuerySelector);
        map.put("callbackUrl", getCallbackUrl());
        PackageTextTemplate packageTextTemplate = new PackageTextTemplate(getClass(), "autocomplete.js",
                "text/javascript");
        String resource = packageTextTemplate.asString(map);
        //response.renderJavaScript(resource, jQuerySelector);
    }
    
    @Override
    public void onRequest() {
        logger.trace("ajax request received");

        RequestCycle requestCycle = RequestCycle.get();
        Request request = requestCycle.getRequest();
        IRequestParameters irp = request.getRequestParameters();
        StringValue term = irp.getParameterValue("term");
        List<?> matches = getMatches(term.toString());
        String json = convertListToJson(matches);
        requestCycle.scheduleRequestHandlerAfterCurrent(new TextRequestHandler("application/json", "UTF-8", json));
    }
}
