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
package dk.dma.embryo.common.db;

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
