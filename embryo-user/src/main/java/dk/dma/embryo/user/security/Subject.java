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
package dk.dma.embryo.user.security;

import java.io.Serializable;
import java.util.List;

import dk.dma.embryo.user.model.Role;
import dk.dma.embryo.user.model.SecuredUser;

/**
 * Subject class wrapping all access to shiro security and also decorating with
 * extra syntactic sugar.
 * 
 * 
 * @author Jesper Tejlgaard
 * 
 */
public interface Subject extends Serializable {

    SecuredUser login(String userName, String password, Boolean rememberMe);

    SecuredUser login(String userName, String password);

    /**
     * TODO remove me.
     * 
     * Expected used while transitioning from role base security to feature base
     * security
     * 
     * @param permission
     * @return
     */
    <R extends Role> boolean hasRole(Class<R> roleType);

    Long getUserId();

    SecuredUser getUser();

    boolean hasOneOfRoles(List<Class<? extends Role>> roleTypes);

    boolean authorizedToModifyVessel(Long mmsi);

    void logout();

    boolean isLoggedIn();

    SecuredUser getUserForEmail(String email);

    SecuredUser findUserWithUuid(String uuid);
}
