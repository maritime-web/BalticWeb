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
package dk.dma.embryo.configuration;

import java.io.Serializable;
import java.util.Properties;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;

import dk.dma.embryo.common.configuration.PropertyFileService;

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
