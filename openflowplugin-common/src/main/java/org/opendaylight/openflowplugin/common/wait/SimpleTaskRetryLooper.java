/*
 * Copyright (c) 2015, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.common.wait;

import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by mirehak on 4/28/15.
 */
public class SimpleTaskRetryLooper {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleTaskRetryLooper.class);

    private final long tick;
    private final int maxRetries;

    /**
     * It retries the task passed to its method loopUntilNoException a number of
     * times (maxRetries).
     *
     * @param tick
     *            sleep between steps in miliseconds
     * @param maxRetries
     *            retries limit
     */
    public SimpleTaskRetryLooper(long tick, int maxRetries) {
        this.tick = tick;
        this.maxRetries = maxRetries;
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    public <T> T loopUntilNoException(Callable<T> task) throws Exception {
        T output = null;

        Exception taskException = null;
        for (int i = 0; i < maxRetries; i++) {
            taskException = null;
            try {
                output = task.call();
                break;
            } catch (Exception exception) {
                LOG.debug("looper step failed: {}", exception.getMessage());
                taskException = exception;
            }

            try {
                Thread.sleep(tick);
            } catch (InterruptedException e) {
                LOG.debug("interrupted: {}", e.getMessage(), e);
                Thread.currentThread().interrupt();
            }
        }

        if (taskException != null) {
            throw taskException;
        }

        LOG.debug("looper step succeeded: {}", output);
        return output;
    }
}
