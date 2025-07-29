package org.eclipse.edc.monitor;

import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;

public class FileAndConsoleMonitorExtension implements ServiceExtension {
    private FileAndConsoleMonitor newMonitor;

    @Override
    public String name() {
        return "File and Console Monitor";
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        // Obtenemos el monitor que se ha cargado por defecto
        var originalMonitor = context.getMonitor();

        // Obtenemos el ID de este runtime desde la configuración
        var runtimeId = context.getConfig().getString("edc.runtime.id", "unknown-runtime");

        // Creamos nuestro monitor, pasándole el ID y el monitor original para no perder los logs de arranque
        this.newMonitor = new FileAndConsoleMonitor(runtimeId, originalMonitor);
        context.registerService(Monitor.class, newMonitor);

        originalMonitor.info("File and Console Monitor registered for runtime: " + runtimeId);
    }

    @Override
    public void shutdown() {
        if (newMonitor != null) {
            newMonitor.close();
            System.out.println("FilwAndConsoleMonitor for " + name() + "shut down correctly");
        }
    }
}