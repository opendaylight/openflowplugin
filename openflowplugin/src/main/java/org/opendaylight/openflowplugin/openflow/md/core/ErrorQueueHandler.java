/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core;

import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * dumping all exceptions to log
 * @author mirehak
 */
public class ErrorQueueHandler implements Runnable {

    private static final Logger LOG = LoggerFactory
            .getLogger(ErrorQueueHandler.class);

    private LinkedBlockingQueue<Exception> errorQueue;

    /**
     * @param errorQueue
     */
    public ErrorQueueHandler(LinkedBlockingQueue<Exception> errorQueue) {
        this.errorQueue = errorQueue;
    }

    @Override
    public void run() {
        while (true) {
            Exception error;
            try {
                error = errorQueue.take();
                Throwable cause = error.getCause();
                LOG.error(error.getMessage()+" -> "+cause.getMessage(), cause);
            } catch (InterruptedException e) {
                LOG.warn(e.getMessage());
            }
        }
    }
}
