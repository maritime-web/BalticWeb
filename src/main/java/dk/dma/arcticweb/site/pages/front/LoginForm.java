package dk.dma.arcticweb.site.pages.front;

import javax.ejb.EJB;

import org.apache.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;

import dk.dma.arcticweb.domain.User;
import dk.dma.arcticweb.service.UserService;
import dk.dma.arcticweb.site.pages.main.MainPage;
import dk.dma.arcticweb.site.session.ArcticWebSession;

public class LoginForm extends StatelessForm<LoginForm> {
	
	private static final long serialVersionUID = 1L;
	
	private static final Logger LOG = Logger.getLogger(LoginForm.class);

	@EJB
	UserService userService;	
	
	private String username;
	private String password;
	private Boolean rememberMe;
	
	private TextField<String> usernameField;
	private PasswordTextField passwordField;
	private FeedbackPanel feedback;
	private AjaxSubmitLink submitLink;
	
	public LoginForm(String id) {
		super(id);
		usernameField = new TextField<String>("username");
		passwordField = new PasswordTextField("password");
		feedback = new FeedbackPanel("login_feedback");
		feedback.setVisible(false);
		submitLink = new AjaxSubmitLink("login_btn") {
			private static final long serialVersionUID = 1L;
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				User user = userService.login(username, password);
				if (user != null) {
					feedback.setVisible(false);
					LOG.info("User logged in: " + username);
					ArcticWebSession.get().loginUser(user);					
					setResponsePage(new MainPage());
				} else {
					feedback.setVisible(true);
					error("Wrong username or password");
					target.add(this.getParent());
				}
			}
		};
		
		usernameField.setRequired(true);
		passwordField.setRequired(true);		
		
		setModel(new CompoundPropertyModel<LoginForm>(this));
		add(usernameField);
		add(passwordField);
		add(feedback);
		add(submitLink);
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Boolean getRememberMe() {
		return rememberMe;
	}

	public void setRememberMe(Boolean rememberMe) {
		this.rememberMe = rememberMe;
	}

}
