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
package dk.dma.embryo.db;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
import org.hibernate.service.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.dma.embryo.configuration.IllegalConfigurationException;

/**
 * @author Jesper Tejlgaard
 */
public class LiquibaseMigrator implements Integrator {

    private static final Logger LOGGER = LoggerFactory.getLogger(LiquibaseMigrator.class);

    private ConnectionProvider connectionProvider;

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
            throw new IllegalConfigurationException("Hibernate can not be used with hibernate.hbm2ddl.auto=" + hbm2ddl
                    + " when Liquibase is enabled.");
        }

        LOGGER.info("Booting Liquibase " + LiquibaseUtil.getBuildVersion());
        connectionProvider = sessionFactoryImplementor.getJdbcServices().getConnectionProvider();
        try {
            performUpdate();
        } catch (LiquibaseException e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    private void performUpdate() throws LiquibaseException {
        Connection c = null;
        Liquibase liquibase = null;
        try {
            // c = dataSource.getConnection();
            c = connectionProvider.getConnection();
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
        System.out.println("LIQUIBASE CHANGELOG: " + config.getChangeLog());

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
