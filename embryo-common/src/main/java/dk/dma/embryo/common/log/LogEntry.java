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
package dk.dma.embryo.common.log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import dk.dma.embryo.common.persistence.BaseEntity;

@Entity
@NamedQueries({
        @NamedQuery(name = "LogEntry:search", query = "SELECT m FROM LogEntry m WHERE m.ts > :date AND m.service = :service ORDER BY m.ts DESC"),
        @NamedQuery(name = "LogEntry:list", query = "SELECT m FROM LogEntry m WHERE m.ts > :date ORDER BY m.ts"),
        @NamedQuery(name = "LogEntry:latest", query = "SELECT e FROM LogEntry e where e.service = :service order by e.ts desc"),
        @NamedQuery(name = "LogEntry:services", query = "SELECT DISTINCT e.service FROM LogEntry e where e.ts > :date") })
public class LogEntry extends BaseEntity<Long> {

    private static final long serialVersionUID = -7538708790704459110L;

    @Column(length = 100)
    private String service;
    private Status status;
    @Column(length = 200)
    private String message;
    @Column(length = 4000)
    private String stackTrace;

    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime ts;

    // //////////////////////////////////////////////////////////////////////
    // Utility methods
    // //////////////////////////////////////////////////////////////////////
    public JsonLogEntry toJsonModel() {
        Date ts = getTs() == null ? null : getTs().toDate();

        JsonLogEntry logEntry = new JsonLogEntry();
        logEntry.setService(getService());
        logEntry.setMessage(getMessage());
        logEntry.setStatus(getStatus());
        logEntry.setStackTrace(getStackTrace());
        logEntry.setDate(ts);

        return logEntry;
    }

    public static List<JsonLogEntry> fromJsonModel(List<LogEntry> list) {
        List<JsonLogEntry> result = new ArrayList<>(list.size());
        for (LogEntry entry : list) {
            result.add(entry.toJsonModel());
        }
        return result;
    }

    // //////////////////////////////////////////////////////////////////////
    // Object methods
    // //////////////////////////////////////////////////////////////////////
    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

    // //////////////////////////////////////////////////////////////////////
    // Property methods
    // //////////////////////////////////////////////////////////////////////
    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public DateTime getTs() {
        return ts;
    }

    public void setTs(DateTime date) {
        this.ts = date;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    // //////////////////////////////////////////////////////////////////////
    // Inner types
    // //////////////////////////////////////////////////////////////////////
    public enum Status {
        OK, ERROR
    }
}
