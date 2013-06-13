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

import javax.enterprise.event.Event;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import org.apache.wicket.cdi.CdiConfiguration;
import org.apache.wicket.cdi.ConversationPropagation;
import org.apache.wicket.cdi.DetachEventEmitter;
import org.apache.wicket.util.tester.WicketTester;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.junit.Before;
import org.junit.runner.RunWith;

import dk.dma.embryo.config.Configuration;
import dk.dma.embryo.site.component.ReflectiveComponentFactory;

@RunWith(CdiRunner.class)
@AdditionalClasses(value = { ReflectiveComponentFactory.class, DetachEventEmitter.class, Event.class })
public class CdiUnitTestBase {

    private WicketTester tester;

    @Inject
    private BeanManager beanManager;

    @Before
    public void setup() {
        CdiConfiguration cdi = new CdiConfiguration(beanManager).setPropagation(ConversationPropagation.NONE);
        tester = new WicketTester();
        cdi.configure(tester.getApplication());
    }

    protected WicketTester getTester() {
        return tester;
    }

}
