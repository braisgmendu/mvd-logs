package org.eclipse.edc.monitor;

import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.system.MonitorExtension;

/**
 * Esta clase extiende la funcionalidad del sistema de monitoreo utilizando una base de datos.
 */
public class DatabaseMonitorExtension implements MonitorExtension {
    private DatabaseMonitor monitor;

    @Override
    public String name() {
        return "Database Monitor";
    }

    @Override
    public Monitor getMonitor() {
        if (monitor == null) {
            // Leemos la propiedad del sistema y llamamos al nuevo constructor
            String runtimeId = System.getProperty("edc.runtime.id", "unknown-runtime");
            monitor = new DatabaseMonitor(runtimeId);
        }
        return monitor;
    }

}