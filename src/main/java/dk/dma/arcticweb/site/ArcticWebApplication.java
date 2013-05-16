package dk.dma.arcticweb.site;

import static net.ftlines.wicket.cdi.ConversationPropagation.NONE;

import javax.enterprise.inject.spi.BeanManager;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import net.ftlines.wicket.cdi.CdiConfiguration;

import org.apache.wicket.Page;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;

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
		super.init();
		
		// Enable CDI		
		enableCdi();

		// Set security
		getSecuritySettings().setAuthorizationStrategy(new AuthStrategy());

		// Mount pages
		mountPage("/main", MainPage.class);
		mountPage("/front", FrontPage.class);
		mountPage("/test", TestPage.class);
	}
	
	@Override
	public Session newSession(Request request, Response response) {		
		return new ArcticWebSession(request);
	}

	private void enableCdi() {
		// Enable CDI
		BeanManager bm;
		try {
			bm = (BeanManager) new InitialContext().lookup("java:comp/BeanManager");
		} catch (NamingException e) {
			throw new IllegalStateException("Unable to obtain CDI BeanManager", e);
		}

		// Configure CDI, disabling Conversations as we aren't using them
		new CdiConfiguration(bm).setPropagation(NONE).configure(this);
	}

}
