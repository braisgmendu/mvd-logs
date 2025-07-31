package org.eclipse.edc.monitor.runtime;


import org.eclipse.edc.boot.config.ConfigurationLoader;
import org.eclipse.edc.boot.config.EnvironmentVariables;
import org.eclipse.edc.boot.config.SystemProperties;
import org.eclipse.edc.boot.system.ServiceLocatorImpl;
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
    private DatabaseMonitor databaseMonitor;
    private final ConfigurationLoader configLoader;
    private final Monitor bootstrapMonitor;

    /**
     *  Constructor que inicializa los componentes necesarios para el arranque
     */
    public CustomBaseRuntime() {
        super();
        this.bootstrapMonitor = new ConsoleMonitor(ConsoleMonitor.Level.DEBUG, true);
        var serviceLocator = new ServiceLocatorImpl();
        this.configLoader = new ConfigurationLoader(serviceLocator, EnvironmentVariables.ofDefault(), SystemProperties.ofDefault());
    }

    /**
     * Nuestro propio método main.
     *
     * @param args  argumentos que se le puede pasar al ejecutar
     */
    public static void main(String[] args) {
        // Creamos una instancia de nuestra clase
        var runtime = new CustomBaseRuntime();
        runtime.boot(true);
    }

    @NotNull
    @Override
    protected Monitor createMonitor() {
        var config = this.configLoader.loadConfiguration(this.bootstrapMonitor);
        var runtimeId = config.getString("edc.runtime.id", "unknown-runtime");
        var dbConfig = config.getConfig("edc.datasource.log");

        try {
            Connection connection = createConnection(dbConfig);
            this.bootstrapMonitor.info("Database Monitor initial for: " + runtimeId);
            this.databaseMonitor = new DatabaseMonitor(runtimeId, this.bootstrapMonitor, connection);
            return this.databaseMonitor;
        } catch (SQLException e) {
            this.bootstrapMonitor.severe("No se pudo conectar la base de datos");
            return this.bootstrapMonitor;
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