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
package dk.dma.embryo.db;

import org.hibernate.dialect.MySQL5Dialect;

/**
 * Solution to Hibernate defects: 

 * https://hibernate.atlassian.net/browse/HHH-6935
 * 
 * @author Jesper Tejlgaard
 */
public class MySql5BitBooleanDialect extends MySQL5Dialect {
    public MySql5BitBooleanDialect() {
        super();
        registerColumnType(java.sql.Types.BOOLEAN, "bit");
    }
}
