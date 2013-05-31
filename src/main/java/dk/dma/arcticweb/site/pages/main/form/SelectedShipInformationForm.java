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

import java.util.List;

import javax.ejb.EJB;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.validation.validator.RangeValidator;
import org.apache.wicket.validation.validator.StringValidator;

import dk.dma.arcticweb.service.StakeholderService;
import dk.dma.enav.model.ship.ShipType;

public class SelectedShipInformationForm extends Form<SelectedShipInformationForm> {

    private static final long serialVersionUID = 1L;

    @EJB
    StakeholderService stakeholderService;

    private TextField<Long> mmsi;
    private TextField<String> name;
    private TextField<Long> imoNo;
    private TextField<String> callsign;
    private DropDownChoice<String> type;
    private TextField<Integer> maxSpeed;
    private TextField<Integer> tonnage;
    private TextField<String> commCapabilities;
    private TextField<Integer> rescueCapacity;
    private TextField<Integer> width;
    private TextField<Integer> length;
    private TextField<String> iceClass;
    private CheckBox helipad;

    private FeedbackPanel feedback;
    private WebMarkupContainer saved;

    public SelectedShipInformationForm(String id) {
        super(id);

        mmsi = new TextField<>("mmsi");
        // With Wicket 6.7.0
        mmsi.setRequired(true).add(new RangeValidator<Long>(100000000L, 999999999L));
        // With Wicket 1.5.8
        // mmsi.setRequired(true).add(new MinimumValidator<Long>(100000000L)).add(new
        // MaximumValidator<Long>(999999999L));
        name = new TextField<>("name");
        name.setRequired(true);
        imoNo = new TextField<>("imoNo");
        callsign = new TextField<>("callsign");
        callsign.add(StringValidator.maximumLength(32));
        List<String> types = ShipType.getStringList();
        type = new DropDownChoice<>("type", types);
        maxSpeed = new TextField<>("maxSpeed");
        // With Wicket 6.7.0
        maxSpeed.add(new RangeValidator<Integer>(null, 200));
        // With Wicket 1.5.8
        // maxSpeed.add(new MaximumValidator<Integer>(200));
        tonnage = new TextField<>("tonnage");
        commCapabilities = new TextField<>("commCapabilities");
        rescueCapacity = new TextField<>("rescueCapacity");
        width = new TextField<>("width");
        length = new TextField<>("length");
        iceClass = new TextField<>("iceClass");
        helipad = new CheckBox("helipad");

        feedback = new FeedbackPanel("ship_information_feedback");
        feedback.setVisible(false);
        saved = new WebMarkupContainer("saved");
        saved.setVisible(false);

        add(mmsi);
        add(name);
        add(imoNo);
        add(callsign);
        add(type);
        add(maxSpeed);
        add(tonnage);
        add(commCapabilities);
        add(rescueCapacity);
        add(width);
        add(length);
        add(iceClass);
        add(helipad);

        add(feedback);
        add(saved);
    }

}
