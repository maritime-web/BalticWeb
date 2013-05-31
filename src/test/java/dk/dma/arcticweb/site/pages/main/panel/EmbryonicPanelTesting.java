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
package dk.dma.arcticweb.site.pages.main.panel;

import org.apache.wicket.markup.html.basic.Label;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import dk.dma.arcticweb.site.CdiUnitTestBase;
import dk.dma.arcticweb.site.TestPanel;
import dk.dma.embryo.site.component.ReflectiveComponentFactory;

/**
 * Won't pass test phase in Maven. Renamed to disable it. Question posted at stackoverflow:
 * http://stackoverflow.com/questions
 * /16770518/unit-test-with-wickettester-and-cdi-unit-works-in-eclipse-but-fails-during-maven
 * 
 * @author jesper
 * 
 */

@RunWith(CdiRunner.class)
@AdditionalClasses(value = { ReflectiveComponentFactory.class })
public class EmbryonicPanelTesting extends CdiUnitTestBase {

    @Test
    public void testAddComponentWithClass() {

        EmbryonicPanelTested embryPanel = new EmbryonicPanelTested("someId");
        TestPanel panel1 = embryPanel.addComponent(TestPanel.class);
        TestPanel panel2 = embryPanel.addComponent(TestPanel.class);

        getTester().startComponentInPage(embryPanel);

        getTester().assertComponent(panel1.getPageRelativePath(), TestPanel.class);
        getTester().assertComponent(panel2.getPageRelativePath(), TestPanel.class);
        getTester().assertComponent(panel1.getPageRelativePath() + ":text", Label.class);
        getTester().assertComponent(panel2.getPageRelativePath() + ":text", Label.class);
    }

}

@SuppressWarnings("serial")
class EmbryonicPanelTested extends EmbryonicPanel {
    public EmbryonicPanelTested(String id) {
        super(id);
    }
}
