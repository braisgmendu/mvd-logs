package org.eclipse.edc.monitor;


import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Esta clase extiende la funcionalidad del sistema de monitoreo utilizando una base de datos.
 */
public class DatabaseMonitorExtension implements ServiceExtension {
    private DatabaseMonitor newMonitor;

    @Override
    public String name() {
        return "Database Monitor";
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        var originalMonitor = context.getMonitor();
        var config = context.getConfig("edc.datasource.log");



        String url = config.getString("url");
        String user = config.getString("user");
        String password = config.getString("password");

        try {
            Connection connection = DriverManager.getConnection(url, user, password);
            var runtimeId = context.getRuntimeId();

            this.newMonitor = new DatabaseMonitor(runtimeId, originalMonitor, connection);
            context.registerService(Monitor.class, newMonitor);
            originalMonitor.info("Database Monitor registered for runtime: " + runtimeId);

        } catch (SQLException e) {
            originalMonitor.severe("NO SE PUDO CONECTAR CON LA DATABASE", e);
            throw new RuntimeException(e);

        }

    }

    @Override
    public void shutdown() {
        if (newMonitor != null) {
            newMonitor.close();
        }
    }
}