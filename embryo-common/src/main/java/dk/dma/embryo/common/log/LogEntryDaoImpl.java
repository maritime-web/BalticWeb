/* Copyright (c) 2011 Danish Maritime Authority.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dk.dma.embryo.common.log;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.TypedQuery;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import dk.dma.embryo.common.persistence.DaoImpl;

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
    public List<LogEntry> search(String service, Integer countPerType, DateTime from) {
        if(from == null){
            throw new IllegalArgumentException("You must specify a timestamp for when to search from");
        }
        
        List<String> services = new ArrayList<>();
        if(service != null && service.trim().length() > 0){
            services.add(service);
        }else{
            services.addAll(services(from));
        }
        
        List<LogEntry> result = new ArrayList<>();
        for(String s : services){
            TypedQuery<LogEntry> query = em.createNamedQuery("LogEntry:search", LogEntry.class);
            query.setParameter("date", from);
            query.setParameter("service", s);
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
