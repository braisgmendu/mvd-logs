package org.eclipse.edc.monitor;

import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.system.MonitorExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Esta clase extiende la funcionalidad del sistema de monitoreo utilizando una base de datos.
 */
public class DatabaseMonitorExtension implements MonitorExtension {
    private DatabaseMonitor newMonitor;

    @Override
    public String name() {
        return "Database Monitor";
    }


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
            originalMonitor.info("<<<<DatabaseMonitor Creado>>>>");
        } catch (SQLException e) {
            originalMonitor.severe("NO SE PUDO CONECTAR CON LA DATABASE", e);
            throw new RuntimeException(e);

        }

    }

    @Override
    public Monitor getMonitor() {

        return this.newMonitor;
    }
}