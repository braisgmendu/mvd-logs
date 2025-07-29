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

import org.eclipse.edc.spi.monitor.Monitor;

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

    private final Monitor consoleMonitor;
    private final PrintWriter fileWriter;
    private final String prefix;


    public FileAndConsoleMonitor(String runtimeId, Monitor originalMonitor) {
        // 1. Instanciamos ConsoleMonitor para reutilizar su lógica de consola
        this.consoleMonitor = originalMonitor;
        this.prefix = "[%s]".formatted(runtimeId);

        try {
            new java.io.File("logs").mkdirs();
            String logFileName = runtimeId.isEmpty() ? "edc-default" : runtimeId;
            FileWriter fw = new FileWriter(String.format("logs/%s.logs", logFileName), true);
            this.fileWriter = new PrintWriter(fw, true);
        } catch (IOException e) {
            originalMonitor.severe(() -> "Error al inicializar FileAndConsoleMonitor: no se pudo abrir el fichero de log.", e);
            throw new RuntimeException("No se pudo inicializar el fichero de log", e);
        }

        //Runtime.getRuntime().addShutdownHook(new Thread(this::close));
    }

    // Los métodos de la interfaz simplemente delegan al método 'output'
    @Override
    public void severe(Supplier<String> supplier, Throwable... errors) {
        consoleMonitor.severe(supplier, errors);
        writeToFile("SEVERE", supplier, errors);
    }

    @Override
    public void warning(Supplier<String> supplier, Throwable... errors) {
        consoleMonitor.warning(supplier, errors);
        writeToFile("WARNING", supplier, errors);
    }

    @Override
    public void info(Supplier<String> supplier, Throwable... errors) {
        consoleMonitor.info(supplier, errors);
        writeToFile("INFO", supplier, errors);
    }

    @Override
    public void debug(Supplier<String> supplier, Throwable... errors) {
        consoleMonitor.debug(supplier, errors);
        writeToFile("DEBUG", supplier, errors);
    }

    private synchronized void writeToFile(String level, Supplier<String> supplier, Throwable... errors) {
        try {
            var time = ZonedDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            fileWriter.println(prefix + level + " " + time + " " + sanitizeMessage(supplier));
            if (errors != null) {
                for (var error : errors) {
                    if (error != null) {
                        error.printStackTrace(fileWriter);
                    }
                }
            }
            fileWriter.flush();
        } catch (Exception e) {
            consoleMonitor.severe("FALLO AL ESCRIBIR EN EL FICHERO DE LOG", e);
        }

    }

    @Override
    public void close() {
        if (fileWriter != null) {
            fileWriter.close();
        }
    }

}