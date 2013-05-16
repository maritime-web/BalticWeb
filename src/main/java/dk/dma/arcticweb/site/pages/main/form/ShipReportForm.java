package dk.dma.arcticweb.site.pages.main.form;

import java.util.Date;

import javax.ejb.EJB;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.extensions.yui.calendar.DateTimeField;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;

import dk.dma.arcticweb.domain.Ship;
import dk.dma.arcticweb.domain.ShipReport;
import dk.dma.arcticweb.service.StakeholderService;
import dk.dma.arcticweb.site.pages.main.MainPage;
import dk.dma.arcticweb.site.session.ArcticWebSession;

public class ShipReportForm extends Form<ShipReportForm> {
	
	private static final long serialVersionUID = 1L;
	
	@EJB
	StakeholderService stakeholderService;
	
	private TextField<Double> lat;
	private TextField<Double> lon;
	private TextArea<String> weather;
	private TextArea<String> iceObservations;
	private DateTimeField reportTime;
	
	private FeedbackPanel feedback;
	private AjaxSubmitLink saveLink;
	
	private ShipReport shipReport;
	
	public ShipReportForm(String id) {
		super(id);		
		final Ship ship = (Ship) ArcticWebSession.get().getStakeholder();
		
		shipReport = new ShipReport();
		shipReport.setReportTime(new Date());
		setDefaultModel(new CompoundPropertyModel<ShipReport>(shipReport));
		
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
		        //this will force to use 24 hours format
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
				shipReport.setReportTime(new Date());
				stakeholderService.addShipReport(ship, shipReport);				
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
