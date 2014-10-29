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
package dk.dma.embryo.common.configuration;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import java.io.Serializable;


public class Configuration implements Serializable {

    private static final long serialVersionUID = 5538000455989826397L;

    @Produces
    @PersistenceContext(name = "arcticweb")
    EntityManager entityManager;

    @Produces
    @PersistenceUnit(name = "arcticweb")
    EntityManagerFactory entityManagerFactory;


    public static BeanManager getContainerBeanManager() {
        BeanManager bm;
        try {
            bm = (BeanManager) new InitialContext().lookup("java:comp/BeanManager");
        } catch (NamingException e) {
            throw new IllegalStateException("Unable to obtain CDI BeanManager", e);
        }
        return bm;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getBean(Class<T> clazz) {
        BeanManager bm = getContainerBeanManager();
        Bean<T> bean = (Bean<T>) bm.getBeans(clazz).iterator().next();
        CreationalContext<T> ctx = bm.createCreationalContext(bean);
        T instance = (T) bm.getReference(bean, clazz, ctx); // this
        return instance;
    }

    // public static SecurityManager initShiroSecurity() {
    //
    // DefaultSecurityManager securityManager = new DefaultSecurityManager();
    // securityManager.setRealm(new JpaRealm());
    // return securityManager;
    // }
}
