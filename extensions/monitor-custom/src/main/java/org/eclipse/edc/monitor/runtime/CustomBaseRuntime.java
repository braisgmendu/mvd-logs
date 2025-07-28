package org.eclipse.edc.monitor.runtime;

import org.eclipse.edc.boot.system.runtime.BaseRuntime;
import org.eclipse.edc.monitor.FileAndConsoleMonitor;
import org.eclipse.edc.spi.monitor.ConsoleMonitor;
import org.eclipse.edc.spi.monitor.Monitor;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class CustomBaseRuntime extends BaseRuntime {

    private final String[] capturedArgs; // Campo para guardar los argumentos

    /**
     * Constructor que captura los argumentos del programa.
     */
    private CustomBaseRuntime(String[] args) {
        super(); // Llama al constructor de la clase padre
        this.capturedArgs = args;
    }

    /**
     * Nuestro propio método main.
     */
    public static void main(String[] args) {
        // Creamos una instancia de nuestra clase, pasándole los argumentos
        var runtime = new CustomBaseRuntime(args);
        runtime.boot(true);
    }

    @NotNull
    @Override
    protected Monitor createMonitor() {
        // Ahora usamos nuestro campo 'capturedArgs' en lugar del 'programArgs' privado
        var runtimeId = Arrays.stream(capturedArgs)
                .filter(s -> s.startsWith("--runtime-id="))
                .map(s -> s.substring("--runtime-id=".length()))
                .findFirst()
                .orElse("unknown-runtime");

        System.out.println("\n<<<<< USANDO CustomBaseRuntime PARA CREAR EL MONITOR PARA: " + runtimeId + " >>>>>\n");
        return new FileAndConsoleMonitor(runtimeId, ConsoleMonitor.Level.DEBUG, true);
    }
}