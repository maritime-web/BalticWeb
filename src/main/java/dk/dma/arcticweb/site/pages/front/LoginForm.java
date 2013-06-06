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
package dk.dma.arcticweb.site.pages.front;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;

import dk.dma.arcticweb.site.pages.main.MainPage;
import dk.dma.embryo.domain.SecuredUser;
import dk.dma.embryo.security.Subject;

public class LoginForm extends StatelessForm<LoginForm> {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = Logger.getLogger(LoginForm.class);

    @Inject
    Subject subject;
    
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
//                User user = userService.login(username, password);
                
                System.out.println("username = " + username + ", password=" + password);
                
                SecuredUser user = subject.login(username, password); 
                if (user != null) {
                    feedback.setVisible(false);
                    LOG.info("User logged in: " + username);
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
