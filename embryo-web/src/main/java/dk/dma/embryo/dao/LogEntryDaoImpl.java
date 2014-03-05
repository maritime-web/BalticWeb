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
package dk.dma.embryo.dao;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.TypedQuery;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import dk.dma.embryo.domain.LogEntry;

@Stateless
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
public class LogEntryDaoImpl extends DaoImpl implements LogEntryDao {

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void save(LogEntry entry) {
        saveEntity(entry);
    }

    @Override
    public List<LogEntry> list() {
        TypedQuery<LogEntry> query = em.createNamedQuery("LogEntry:list", LogEntry.class);
        query.setParameter("date", DateTime.now(DateTimeZone.UTC).minusDays(2));

        return query.getResultList();
    }

    @Override
    public List<LogEntry> search(String type, Integer countPerType, DateTime from) {
        if(from == null){
            throw new IllegalArgumentException("You must specify a timestamp for when to search from");
        }
        
        List<String> services = new ArrayList<>();
        if(type != null && type.trim().length() > 0){
            services.add(type);
        }else{
            services.addAll(services(from));
        }
        
        List<LogEntry> result = new ArrayList<>();
        for(String service : services){
            TypedQuery<LogEntry> query = em.createNamedQuery("LogEntry:search", LogEntry.class);
            query.setParameter("date", from);
            query.setParameter("service", service);
            if(countPerType != null){
                query.setFirstResult(0);
                query.setMaxResults(countPerType);
            }
            result.addAll(query.getResultList());
        }
        
        return result;
    }


    
    @Override
    public LogEntry latest(String service) {
        TypedQuery<LogEntry> query = em.createNamedQuery("LogEntry:latest", LogEntry.class);
        query.setParameter("service", service);
        query.setMaxResults(1);

        return getSingleOrNull(query.getResultList());
    }

    @Override
    public List<String> services(DateTime from) {
        TypedQuery<String> query = em.createNamedQuery("LogEntry:services", String.class);
        query.setParameter("date", from);

        return query.getResultList();
    }

    
}
