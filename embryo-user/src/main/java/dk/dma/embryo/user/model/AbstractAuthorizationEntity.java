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
package dk.dma.embryo.user.model;

import javax.persistence.MappedSuperclass;

import dk.dma.embryo.common.persistence.BaseEntity;


@MappedSuperclass
public abstract class AbstractAuthorizationEntity<K> extends BaseEntity<K> {

    private static final long serialVersionUID = 6625306470923937976L;

    public AbstractAuthorizationEntity() {
    }

    protected AbstractAuthorizationEntity(String logicalName) {
        this.logicalName = logicalName;
    }

    // //////////////////////////////////////////////////////////////////////
    // Entity fields (also see super class)
    // //////////////////////////////////////////////////////////////////////
    private String logicalName;

    // //////////////////////////////////////////////////////////////////////
    // business logic
    // //////////////////////////////////////////////////////////////////////

    // //////////////////////////////////////////////////////////////////////
    // Utility methods
    // //////////////////////////////////////////////////////////////////////

    // //////////////////////////////////////////////////////////////////////
    // Property methods
    // //////////////////////////////////////////////////////////////////////
    public String getLogicalName() {
        return logicalName;
    }

    public void setLogicalName(String logicalName) {
        this.logicalName = logicalName;
    }

    public String toStringRaw() {
        return "id=" + id + ", logicalName=" + logicalName;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + toStringRaw() + "]";
    }
}
