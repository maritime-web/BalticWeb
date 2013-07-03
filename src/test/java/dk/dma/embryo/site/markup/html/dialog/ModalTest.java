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
package dk.dma.embryo.site.markup.html.dialog;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.junit.Test;

import dk.dma.arcticweb.site.CdiUnitTestBase;

public class ModalTest extends CdiUnitTestBase{

    @Test
    public void constructor() {
        Modal modal = new Modal("someId").title("My title").size(Modal.SIZE.XLARGE);
        
        getTester().startComponentInPage(modal);

        getTester().assertComponent(modal.getPageRelativePath(), Modal.class);
        getTester().assertLabel(modal.getPageRelativePath() + ":modalContainer:title", "My title");
        getTester().assertComponent(modal.getPageRelativePath() + ":modalContainer:footer", WebMarkupContainer.class);
    }

}
