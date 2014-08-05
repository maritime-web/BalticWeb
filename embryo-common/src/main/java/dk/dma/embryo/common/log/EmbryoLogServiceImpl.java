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

import java.io.PrintWriter;
import java.io.StringWriter;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;


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
