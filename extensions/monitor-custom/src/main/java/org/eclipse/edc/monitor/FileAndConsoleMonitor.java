/*
 *  Copyright (c) 2023 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG) - initial API and implementation
 *
 */


package org.eclipse.edc.monitor;

import org.eclipse.edc.spi.monitor.ConsoleMonitor;
import org.eclipse.edc.spi.monitor.ConsoleMonitor.Level;
import org.eclipse.edc.spi.monitor.Monitor;
import org.jetbrains.annotations.Nullable;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Supplier;

/**
 * Un Monitor que escribe tanto a la consola (usando ConsoleMonitor)
 * como a un fichero de log.
 */
public class FileAndConsoleMonitor implements Monitor, AutoCloseable {

    private final ConsoleMonitor consoleMonitor;
    private final PrintWriter fileWriter;
    private final String prefix;


    public FileAndConsoleMonitor(@Nullable String runtimeName, Level level, boolean useColor) {
        // 1. Instanciamos ConsoleMonitor para reutilizar su lógica de consola
        this.consoleMonitor = new ConsoleMonitor(runtimeName, level, useColor);
        this.prefix = runtimeName == null ? "" : "[%s] ".formatted(runtimeName);

        // 2. Preparamos el escritor para el fichero
        try {
            // 1. Crear un objeto File para el directorio
            java.io.File logDir = new java.io.File("logs");

            // 2. Si el directorio no existe, crearlo
            if (!logDir.exists()) {
                logDir.mkdirs(); // mkdirs() crea también los directorios padres si es necesario
            }

            // CAMBIO CLAVE: El nombre del fichero ahora es dinámico
            String logFileName = runtimeName == null ? "edc-default" : runtimeName;
            FileWriter fw = new FileWriter(String.format("logs/%s.log", logFileName), true);

            this.fileWriter = new PrintWriter(fw, true);

        } catch (IOException e) {
            consoleMonitor.severe(() -> "Error al inicializar FileAndConsoleMonitor: no se pudo abrir el fichero de log.", e);
            throw new RuntimeException("No se pudo inicializar el fichero de log", e);
        }
        // 3. Añadimos un hook para cerrar el fichero al apagar la JVM
        Runtime.getRuntime().addShutdownHook(new Thread(this::close));
    }

    // Constructor por defecto que se usará si EDC lo instancia automáticamente
    public FileAndConsoleMonitor() {
        this(null, Level.getDefaultLevel(), true);
    }

    // Los métodos de la interfaz simplemente delegan al método 'output'
    @Override
    public void severe(Supplier<String> supplier, Throwable... errors) {
        consoleMonitor.severe(supplier, errors);
        output("SEVERE", supplier, errors);
    }

    @Override
    public void warning(Supplier<String> supplier, Throwable... errors) {
        consoleMonitor.warning(supplier, errors);
        output("WARNING", supplier, errors);
    }

    @Override
    public void info(Supplier<String> supplier, Throwable... errors) {
        consoleMonitor.info(supplier, errors);
        output("INFO", supplier, errors);
    }

    @Override
    public void debug(Supplier<String> supplier, Throwable... errors) {
        consoleMonitor.debug(supplier, errors);
        output("DEBUG", supplier, errors);
    }

    private synchronized void output(String level, Supplier<String> supplier, Throwable... errors) {

        // Y ahora, escribimos en el fichero
        var time = ZonedDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        fileWriter.println(prefix + level + " " + time + " " + sanitizeMessage(supplier));
        if (errors != null) {
            for (var error : errors) {
                if (error != null) {
                    error.printStackTrace(fileWriter);
                }
            }
        }
    }

    @Override
    public void close() {
        if (fileWriter != null) {
            fileWriter.close();
        }
    }
}