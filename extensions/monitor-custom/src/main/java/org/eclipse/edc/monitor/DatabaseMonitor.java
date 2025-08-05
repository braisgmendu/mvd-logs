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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.eclipse.edc.spi.monitor.Monitor;

import java.util.function.Supplier;

public class DatabaseMonitor implements Monitor {

    private static final Logger LOGGER = LogManager.getRootLogger();

    public DatabaseMonitor(String runtimeId) {
        ThreadContext.put("runtimeId", runtimeId);
    }

    @Override
    public void severe(Supplier<String> supplier, Throwable... errors) {
        LOGGER.fatal(supplier.get(), errors.length > 0 ? errors[0] : null);
    }

    @Override
    public void warning(Supplier<String> supplier, Throwable... errors) {
        LOGGER.warn(supplier.get(), errors.length > 0 ? errors[0] : null);
    }

    @Override
    public void info(Supplier<String> supplier, Throwable... errors) {
        LOGGER.info(supplier.get(), errors.length > 0 ? errors[0] : null);
    }

    @Override
    public void debug(Supplier<String> supplier, Throwable... errors) {
        LOGGER.debug(supplier.get(), errors.length > 0 ? errors[0] : null);
    }
}