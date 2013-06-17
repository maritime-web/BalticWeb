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
package dk.dma.embryo.site.panel;

import org.jglue.cdiunit.AdditionalClasses;
import org.junit.Test;

import dk.dma.arcticweb.site.CdiUnitTestBase;
import dk.dma.embryo.security.PermissionExtractor;

@AdditionalClasses({PermissionExtractor.class})
public class MenuPanelTest extends CdiUnitTestBase{
    
    @Test
    public void testBuildMenu() {
        MenuPanel menuPanel = new MenuPanel("someId");

        MenuHeader header1 = menuPanel.addMenuHeader("Header 1");
        header1.addMenuItem("Item 1", new ZoomToShipJSExecutor());
        header1.addMenuItem(new MenuTestPanel("someId2", "Item 2"));
        header1.addMenuItem(new MenuTestPanel("someId3", "Item 3"));

        MenuHeader header2 = menuPanel.addMenuHeader("Header 2");
        header2.addMenuItem(new MenuTestPanel("someId4", "Item 4"));
        header2.addMenuItem(new MenuTestPanel("someId5", "Item 5"));
        header2.addMenuItem(new MenuTestPanel("someId6", "Item 6"));
        
        getTester().startComponentInPage(menuPanel);

        getTester().assertComponent(header1.getPageRelativePath(), MenuHeader.class);
        getTester().assertLabel(header1.getPageRelativePath() + ":menuHeaderText", "Header 1");

        getTester().assertLabel(header1.getPageRelativePath() + ":menuItem:1:menuItemLink:menuItemText", "Item 1");
        getTester().assertLabel(header1.getPageRelativePath() + ":menuItem:2:menuItemLink:menuItemText", "Item 2");
        getTester().assertLabel(header1.getPageRelativePath() + ":menuItem:3:menuItemLink:menuItemText", "Item 3");

        getTester().assertComponent(header2.getPageRelativePath(), MenuHeader.class);
        getTester().assertLabel(header2.getPageRelativePath() + ":menuHeaderText", "Header 2");

        getTester().assertLabel(header2.getPageRelativePath() + ":menuItem:1:menuItemLink:menuItemText", "Item 4");
        getTester().assertLabel(header2.getPageRelativePath() + ":menuItem:2:menuItemLink:menuItemText", "Item 5");
        getTester().assertLabel(header2.getPageRelativePath() + ":menuItem:3:menuItemLink:menuItemText", "Item 6");
}
    

}
