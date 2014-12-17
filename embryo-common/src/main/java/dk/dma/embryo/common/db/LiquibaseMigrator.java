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

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.util.LiquibaseUtil;
import liquibase.util.NetUtil;

import org.hibernate.cfg.Configuration;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.metamodel.source.MetadataImplementor;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jesper Tejlgaard
 */
public class LiquibaseMigrator implements Integrator {

    private static final Logger LOGGER = LoggerFactory.getLogger(LiquibaseMigrator.class);

    private LiquibaseConfig config;

    @Override
    public void integrate(final Configuration configuration, final SessionFactoryImplementor sessionFactoryImplementor,
            final SessionFactoryServiceRegistry sessionFactoryServiceRegistry) {
        try {
            config = new LiquibaseConfig();
        } catch (IOException | URISyntaxException e) {
            LOGGER.error("DATABASE MIGRATION FAILED", e);
            return;
        }

        String hostName;
        try {
            hostName = NetUtil.getLocalHostName();
        } catch (Exception e) {
            LOGGER.warn("Cannot find hostname: {}", e.getMessage());
            LOGGER.debug("", e);
            return;
        }
        if (!config.isEnabled()) {
            LOGGER.info("Liquibase did not run on " + hostName
                    + " because  'embryo.liquibase.enable' property was set to false or not present");
            return;
        }

        List<String> disAllowed = Arrays.asList("update", "create", "create-drop");
        String hbm2ddl = configuration.getProperty("hibernate.hbm2ddl.auto");
        if (hbm2ddl != null && disAllowed.contains(hbm2ddl.trim())) {
            throw new IllegalStateException("Hibernate can not be used with hibernate.hbm2ddl.auto=" + hbm2ddl
                    + " when Liquibase is enabled.");
        }

        LOGGER.info("Booting Liquibase " + LiquibaseUtil.getBuildVersion());
        try {
            performUpdate();
        } catch (LiquibaseException | NamingException e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    private void performUpdate() throws LiquibaseException, NamingException {
        Connection c = null;
        Liquibase liquibase = null;
        try {
            InitialContext initialContext = new InitialContext();
            DataSource dataSource = (DataSource) initialContext.lookup("java:jboss/datasources/embryoDS");
            
            c = dataSource.getConnection();
            // c = connectionProvider.getConnection();
            liquibase = createLiquibase(c);
            liquibase.update(config.getContexts());
        } catch (SQLException e) {
            throw new DatabaseException(e);
        } catch (LiquibaseException ex) {
            throw ex;
        } finally {
            if (c != null) {
                try {
                    c.rollback();
                    c.close();
                } catch (SQLException e) {
                    // nothing to do
                }

            }

        }
    }

    private Liquibase createLiquibase(Connection c) throws LiquibaseException {
        LOGGER.info("LIQUIBASE CHANGELOG: {}", config.getChangeLog());

        Liquibase liquibase = new Liquibase(config.getChangeLog(), new ClassLoaderResourceAccessor(getClass()
                .getClassLoader()), createDatabase(c));
        if (config.getParameters() != null) {
            for (Map.Entry<String, String> entry : config.getParameters().entrySet()) {
                liquibase.setChangeLogParameter(entry.getKey(), entry.getValue());
            }
        }

        if (config.isDropFirst()) {
            liquibase.dropAll();
        }

        return liquibase;
    }

    /**
     * Subclasses may override this method add change some database settings such as default schema before returning the
     * database object.
     * 
     * @param c
     * @return a Database implementation retrieved from the {@link liquibase.database.DatabaseFactory}.
     * @throws DatabaseException
     */
    protected Database createDatabase(Connection c) throws DatabaseException {
        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(c));
        if (config.getDefaultSchema() != null) {
            database.setDefaultSchemaName(config.getDefaultSchema());
        }
        return database;
    }

    @Override
    public void integrate(final MetadataImplementor metadataImplementor,
            final SessionFactoryImplementor sessionFactoryImplementor,
            final SessionFactoryServiceRegistry sessionFactoryServiceRegistry) {
        // no-op
    }

    @Override
    public void disintegrate(final SessionFactoryImplementor sessionFactoryImplementor,
            final SessionFactoryServiceRegistry sessionFactoryServiceRegistry) {
        // no-op
    }
}
