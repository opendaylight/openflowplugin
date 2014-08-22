/*
 * (C) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import org.slf4j.Logger;

/**
 * Error handler implementation that logs the error.
 * 
 * @param <S> type of the source where the error was generated
 * @author Fabiel Zuniga
 */
public class LoggerErrorHandler<S> implements ErrorHandler<S, Exception> {

    private Logger logger;

    /**
     * Creates a new error handler.
     *
     * @param logger logger to send the error to
     */
    public LoggerErrorHandler(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void errorOccurred(S source, Exception error) {
        this.logger.error("Error occurred in: " + source, error);
    }
}
