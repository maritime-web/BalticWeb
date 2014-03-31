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
package dk.dma.arcticweb.reporting.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import dk.dma.arcticweb.reporting.model.GreenposSearch;

public class GreenposSearchTest {

    @Test
    public void testDefaultSortValues() {
        GreenposSearch search = new GreenposSearch();

        assertEquals("ts", search.getSortByField());
        assertEquals("DESC", search.getSortOrder());

        search = new GreenposSearch(null, null, null, null, null, null, null, null, null, null);

        assertEquals("ts", search.getSortByField());
        assertEquals("DESC", search.getSortOrder());
    }

}
