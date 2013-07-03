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
package dk.dma.embryo.site.markup.html.dialog;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

public class Modal<M extends Modal> extends Panel {

    private static final long serialVersionUID = 8742628521947906862L;

    protected WebMarkupContainer modalContainer;
    private WebMarkupContainer footer;
    private Label titleLabel;

    public Modal(String id) {
        super(id);

        add(modalContainer = new WebMarkupContainer("modalContainer"));

        // TODO somehow configure close button (cross)
        modalContainer.add(this.titleLabel = new Label("title"));

        modalContainer.add(footer = new WebMarkupContainer("footer"));

    }

    /**
     * Use this method in the construction phase.
     * 
     * @param size
     * @return
     */
    public M size(SIZE size) {
        modalContainer.add(AttributeAppender.append("class", size.cssClass));
        return (M)this;
    }

    /**
     * Set the title of the modal. Use this method in the construction phase.
     * 
     * @param modalTitle
     * @return
     */
    public M title(String modalTitle) {
        titleLabel.setDefaultModel(Model.of(modalTitle));
        return (M)this;
    }

    protected WebMarkupContainer getFooter() {
        return footer;
    }
    
    public String getTitle(){
        return titleLabel.getDefaultModelObjectAsString();
    }

    /**
     * Representation of css classes large and xlarge. They must be present in browser.
     * 
     * @author Jesper Tejlgaard
     */
    public static enum SIZE {
        LARGE("large"), XLARGE("xlarge");

        SIZE(String cssClass) {
            this.cssClass = cssClass;
        }

        String cssClass;
    }
}
