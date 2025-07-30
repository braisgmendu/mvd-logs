package org.eclipse.edc.monitor.runtime;


import org.eclipse.edc.boot.config.ConfigurationLoader;
import org.eclipse.edc.boot.config.EnvironmentVariables;
import org.eclipse.edc.boot.config.SystemProperties;
import org.eclipse.edc.boot.system.runtime.BaseRuntime;
import org.eclipse.edc.monitor.DatabaseMonitor;
import org.eclipse.edc.spi.monitor.ConsoleMonitor;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.system.configuration.Config;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Clase custom de baseRuntime para usar el monitor modificado
 */
public class CustomBaseRuntime extends BaseRuntime {
    DatabaseMonitor databaseMonitor;

    /**
     * Nuestro propio método main.
     */
    public static void main(String[] args) {
        // Creamos una instancia de nuestra clase, pasándole los argumentos
        var runtime = new CustomBaseRuntime();
        runtime.boot(true);
    }

    @NotNull
    @Override
    protected Monitor createMonitor() {
        var consoleMonitor = new ConsoleMonitor();
        var serviceLocator = new org.eclipse.edc.boot.system.ServiceLocatorImpl();
        var configLoader = new ConfigurationLoader(serviceLocator, EnvironmentVariables.ofDefault(), SystemProperties.ofDefault());
        var config = configLoader.loadConfiguration(consoleMonitor);
        var runtimeId = config.getString("edc.runtime.id", "unknown-runtime");
        var dbConfig = config.getConfig("edc.datasource.log");

        try {
            Connection connection = createConnection(dbConfig);
            consoleMonitor.info("Database Monitor initial for: " + runtimeId);
            this.databaseMonitor = new DatabaseMonitor(runtimeId, consoleMonitor, connection);
            return this.databaseMonitor;
        } catch (SQLException e) {
            consoleMonitor.severe("No se pudo conectar la base de datos");
            return consoleMonitor;
        }
    }

    private Connection createConnection(Config config) throws SQLException {
        String url = config.getString("url");
        String user = config.getString("user");
        String password = config.getString("password");
        return DriverManager.getConnection(url, user, password);
    }

    @Override
    public void shutdown() {
        if (databaseMonitor != null) {
            databaseMonitor.close();
        }
        super.shutdown();
    }
}