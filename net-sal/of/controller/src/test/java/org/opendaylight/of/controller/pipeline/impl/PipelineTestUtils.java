/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.pipeline.impl;

import org.slf4j.Logger;

/**
 * Provides basic utilities for testing the {@link PipelineManager}.
 *
 * @author Scott Simes
 */
public class PipelineTestUtils {

    /**
     * Sets the logger implementation to be used by the pipeline manager.
     *
     * @param testLogger the desired logger to. use
     */
    public static void setLogger(Logger testLogger) {
       PipelineManager.setLogger(testLogger);
    }

    /**
     * Restores the logger to the default implementation.
     */
    public static void restoreLogger() {
        PipelineManager.restoreLogger();
    }
}
