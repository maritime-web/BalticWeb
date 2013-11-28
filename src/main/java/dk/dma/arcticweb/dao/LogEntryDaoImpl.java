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
package dk.dma.arcticweb.dao;

import dk.dma.embryo.domain.LogEntry;

import javax.persistence.TypedQuery;
import java.util.Date;
import java.util.List;

public class LogEntryDaoImpl extends DaoImpl implements LogEntryDao {
    public void save(LogEntry entry) {
        saveEntity(entry);
    }

    public List<LogEntry> list() {
        TypedQuery<LogEntry> query = em.createNamedQuery("LogEntry:list", LogEntry.class);

        query.setParameter("date", new Date(System.currentTimeMillis() - 48 * 3600 * 1000L));

        return query.getResultList();
    }
}
