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

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.inject.Named;

@Named
public class EmbryoLogFactory {
    @Inject
    LogEntryDao logEntryDao;

    @Produces
    public EmbryoLogService getLogger(InjectionPoint injectionPoint) {
        return new EmbryoLogServiceImpl(logEntryDao, injectionPoint.getMember().getDeclaringClass());
    }

    public EmbryoLogService getLogger(Class<?> type, String extraQualifier) {
        return new EmbryoLogServiceImpl(logEntryDao, type, extraQualifier);
    }
}
