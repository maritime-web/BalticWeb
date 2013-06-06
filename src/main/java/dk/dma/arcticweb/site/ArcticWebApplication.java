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
package dk.dma.arcticweb.site;

import static org.apache.wicket.cdi.ConversationPropagation.NONE;

import javax.enterprise.inject.spi.BeanManager;

import org.apache.wicket.Page;
import org.apache.wicket.Session;
import org.apache.wicket.authorization.strategies.CompoundAuthorizationStrategy;
import org.apache.wicket.cdi.CdiConfiguration;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;

import dk.dma.arcticweb.config.Configuration;
import dk.dma.arcticweb.site.pages.front.FrontPage;
import dk.dma.arcticweb.site.pages.main.MainPage;
import dk.dma.arcticweb.site.pages.test.TestPage;
import dk.dma.arcticweb.site.session.ArcticWebSession;

public class ArcticWebApplication extends WebApplication {

    @Override
    public Class<? extends Page> getHomePage() {
        return FrontPage.class;
    }

    @Override
    protected void init() {
        
        System.out.println("Initializing application");
        
        super.init();

        enableCdi(Configuration.getContainerBeanManager());

        configureSecurity();

        // Mount pages
        mountPage("/main", MainPage.class);
        mountPage("/front", FrontPage.class);
        mountPage("/test", TestPage.class);
    }

    @Override
    public Session newSession(Request request, Response response) {
        return new ArcticWebSession(request);
    }
    
    private void enableCdi(BeanManager beanManager) {
        // Enable CDI CDI, disabling Conversations as we aren't using them
        new CdiConfiguration(beanManager).setPropagation(NONE).configure(this);
    }
    
    private void configureSecurity(){
        // This provokes configuration of Shiro SecurityManager
        //Configuration.initShiroSecurity();
        CompoundAuthorizationStrategy s = new CompoundAuthorizationStrategy();
        s.add(new AuthStrategy());
        s.add(new FeatureAuthorizationStrategy());
        
        // Set Wicket Authorization strategy
        getSecuritySettings().setAuthorizationStrategy(s);
    }

}
