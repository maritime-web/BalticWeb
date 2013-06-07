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
package dk.dma.arcticweb.site.pages.main.form;

import java.util.Date;

import javax.inject.Inject;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.extensions.yui.calendar.DateTimeField;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;

import dk.dma.arcticweb.service.ShipService;
import dk.dma.arcticweb.site.pages.main.MainPage;
import dk.dma.embryo.domain.Ship2;
import dk.dma.embryo.domain.ShipReport2;
import dk.dma.embryo.security.authorization.YourShip;

public class ShipReportForm extends Form<ShipReportForm> {

    private static final long serialVersionUID = 1L;

//    @EJB
//    StakeholderService stakeholderService;

    private TextField<Double> lat;
    private TextField<Double> lon;
    private TextArea<String> weather;
    private TextArea<String> iceObservations;
    private DateTimeField reportTime;

    private FeedbackPanel feedback;
    private AjaxSubmitLink saveLink;

    private ShipReport2 shipReport;
    
    @Inject @YourShip
    private Ship2 ship;
    
    @Inject
    private ShipService shipService;

    public ShipReportForm(String id) {
        super(id);
//        final Ship ship = (Ship) ArcticWebSession.get().getStakeholder();

        shipReport = new ShipReport2();
        shipReport.setReportTime(new Date());
        setDefaultModel(new CompoundPropertyModel<ShipReport2>(shipReport));

        lat = new TextField<>("lat");
        lat.setRequired(true);

        lon = new TextField<>("lon");
        lon.setRequired(true);

        // TODO try to get position from AIS (if updated recently)
        // TODO formatted output

        weather = new TextArea<>("weather");

        iceObservations = new TextArea<>("iceObservations");

        reportTime = new DateTimeField("reportTime") {
            private static final long serialVersionUID = 1L;

            @Override
            protected boolean use12HourFormat() {
                // this will force to use 24 hours format
                return false;
            }
        };
        reportTime.setRequired(true);

        feedback = new FeedbackPanel("ship_report_feedback");
        feedback.setVisible(false);

        saveLink = new AjaxSubmitLink("save") {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                shipService.reportForCurrentShip(shipReport);
                feedback.setVisible(false);
                target.add(this.getParent());
                setResponsePage(new MainPage());
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                feedback.setVisible(true);
                target.add(this.getParent());
            }
        };

        add(lat);
        add(lon);
        add(weather);
        add(iceObservations);
        add(feedback);
        add(saveLink);
        add(reportTime);

    }

}
