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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.function.Supplier;

/**
 * Un Monitor que escribe tanto a la consola (usando ConsoleMonitor)
 * como a un fichero de log.
 */
public class DatabaseMonitor implements Monitor, AutoCloseable {

    private final Monitor consoleMonitor;
    private final Connection connection;
    private final String runtimeId;
    private final String insertSql = "INSERT INTO logs (log_timestamp, runtime_id, log_level, message, exception) VALUES (?, ?, ?, ?, ?)";

    /**
     * Crea una nueva instancia de DatabasMonitor con los parámetros dados.
     *
     * @param runtimeId         el identificador del entorno de ejecución
     * @param originalMonitor   el monitor original que se va a envolver o extender
     * @param connection        la conexión a la base de datos para registrar los eventos
     */
    public DatabaseMonitor(String runtimeId, Monitor originalMonitor, Connection connection) {
        this.runtimeId = runtimeId;
        this.consoleMonitor = originalMonitor;
        this.connection = connection;
    }

    // Los métodos de la interfaz simplemente delegan al método 'output'
    @Override
    public void severe(Supplier<String> supplier, Throwable... errors) {
        consoleMonitor.severe(supplier, errors);
        writeToDatabase("SEVERE", supplier, errors);
    }

    @Override
    public void warning(Supplier<String> supplier, Throwable... errors) {
        consoleMonitor.warning(supplier, errors);
        writeToDatabase("WARNING", supplier, errors);
    }

    @Override
    public void info(Supplier<String> supplier, Throwable... errors) {
        consoleMonitor.info(supplier, errors);
        writeToDatabase("INFO", supplier, errors);
    }

    @Override
    public void debug(Supplier<String> supplier, Throwable... errors) {
        consoleMonitor.debug(supplier, errors);
        writeToDatabase("DEBUG", supplier, errors);
    }

    private synchronized void writeToDatabase(String level, Supplier<String> supplier, Throwable... errors) {
        try (PreparedStatement stmt = connection.prepareStatement(insertSql)) {
            stmt.setTimestamp(1, Timestamp.from(Instant.now()));
            stmt.setString(2, runtimeId);
            stmt.setString(3, level);
            stmt.setString(4, sanitizeMessage(supplier));
            if (errors != null && errors.length > 0 && errors[0] != null) {
                StringWriter sw = new StringWriter();
                errors[0].printStackTrace(new PrintWriter(sw));
                stmt.setString(5, sw.toString());
            } else {
                stmt.setNull(5, java.sql.Types.VARCHAR);
            }
            stmt.executeUpdate();
        } catch (SQLException e) {
            consoleMonitor.severe("FALLO AL ESCRIBIR LOG EN POSTGRESQL: " + e.getMessage());
        }
    }

    @Override
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error al cerrar la conexión a la BD de logs: " + e.getMessage());
        }
    }
}