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
package dk.dma.embryo.service;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import dk.dma.embryo.dao.LogEntryDao;
import dk.dma.embryo.domain.LogEntry;

public class EmbryoLogServiceImpl implements EmbryoLogService {
    private LogEntryDao logEntryDao;

    private String className;

    public EmbryoLogServiceImpl(LogEntryDao logEntryDao, Class<?> klass) {
        this.logEntryDao = logEntryDao;
        this.className = klass.getName();
    }

    public EmbryoLogServiceImpl(LogEntryDao logEntryDao, Class<?> klass, String extraQualifier) {
        this.logEntryDao = logEntryDao;
        this.className = klass.getName() + "." + extraQualifier;
    }

    public void info(String message) {
        LogEntry entry = new LogEntry();
        entry.setTs(DateTime.now(DateTimeZone.UTC));
        entry.setMessage(message.substring(0, Math.min(message.length(), 200)));
        entry.setService(className);
        entry.setStatus(LogEntry.Status.OK);
        logEntryDao.save(entry);
    }

    public void error(String message) {
        LogEntry entry = new LogEntry();
        entry.setTs(DateTime.now(DateTimeZone.UTC));
        entry.setMessage(message.substring(0, Math.min(message.length(), 200)));
        entry.setService(className);
        entry.setStatus(LogEntry.Status.ERROR);
        logEntryDao.save(entry);
    }

    public void error(String message, Throwable exception) {
        LogEntry entry = new LogEntry();
        entry.setTs(DateTime.now(DateTimeZone.UTC));
        entry.setMessage(message.substring(0, Math.min(message.length(), 200)));
        entry.setService(className);
        entry.setStatus(LogEntry.Status.ERROR);

        StringWriter sw = new StringWriter();
        exception.printStackTrace(new PrintWriter(sw));
        entry.setStackTrace(sw.toString().substring(0, Math.min(sw.toString().length(), 4000)));

        logEntryDao.save(entry);
    }
}
