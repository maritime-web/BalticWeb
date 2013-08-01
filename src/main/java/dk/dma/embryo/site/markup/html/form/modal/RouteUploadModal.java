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
package dk.dma.embryo.site.markup.html.form.modal;

import java.io.IOException;

import javax.inject.Inject;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.feedback.ContainerFeedbackMessageFilter;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.slf4j.Logger;

import dk.dma.arcticweb.service.ShipService;
import dk.dma.embryo.domain.Route;
import dk.dma.embryo.domain.Voyage;
import dk.dma.embryo.security.authorization.YourShip;
import dk.dma.embryo.site.markup.html.dialog.Modal;
import dk.dma.embryo.site.markup.html.form.DynamicForm;
import dk.dma.embryo.site.markup.html.menu.ReachedFromMenu;

@YourShip
public class RouteUploadModal extends Modal<RouteUploadModal> implements ReachedFromMenu {

    private static final long serialVersionUID = 1L;

    // private final WebMarkupContainer voyageInformation;

    private FeedbackPanel feedback;

    private DynamicForm form;

    @Inject
    private Logger logger;

    @Inject
    private ShipService shipService;

    private FileUploadField fileUpload;

    private Model<Long> voyageId = Model.of();
    private Model<String> voyageName = Model.of();

    /**
     * Modal panel for editing route information
     * 
     * @param id
     */
    public RouteUploadModal(String id) {
        super(id);

        title("Upload Route");

        modalContainer.add(form = new DynamicForm<>("uploadForm"));

        form.add(new HiddenField<>("voyageId", voyageId));
        form.add(new TextField<>("voyageName", voyageName));

        feedback = new FeedbackPanel("feedback", new ContainerFeedbackMessageFilter(form));
        feedback.setOutputMarkupId(true);
        feedback.setVisible(false);
        form.add(feedback);

        form.add(fileUpload = new FileUploadField("file"));

        AjaxSubmitLink saveLink = new AjaxSubmitLink(Modal.FOOTER_BUTTON, form) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                FileUpload upload = fileUpload.getFileUpload();

                Route route = null;

                try {
                    route = shipService.parseRoute(upload.getInputStream());

                    if (voyageId.getObject() != null) {
                        route.setVoyageName(voyageName.getObject());
                        Voyage voyage = shipService.getVoyage(voyageId.getObject());
                        route.setVoyage(voyage);
                    }

                    Long routeId = shipService.saveRoute(route);
                    appendSaveDialogJS(target, routeId);
                } catch (IOException e) {
                    e.printStackTrace();
                } 

                feedback.setVisible(false);
                target.add(form);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                feedback.setVisible(true);
                target.add(this.getParent());
            }
        };

        saveLink.add(AttributeModifier.replace("value", "Upload"));
        saveLink.add(AttributeModifier.append("class", " btn btn-primary"));

        addFooterButton(saveLink);
    }
    
    public void appendSaveDialogJS(final AjaxRequestTarget target, Long routeId) {
        target.appendJavaScript("embryo.modal.close('"+ modalContainer.getMarkupId() + "', embryo.route.fetchAndDraw('" + routeId + "'));");        
    }

    @Override
    public String getBookmark() {
        return modalContainer.getMarkupId();
    }
}
