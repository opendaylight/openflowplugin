/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core;

import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;

import org.opendaylight.openflowplugin.openflow.md.core.session.SessionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * dumping all exceptions to log
 * @author mirehak
 */
public class ErrorHandlerQueueImpl implements ErrorHandler {

    private static final Logger LOG = LoggerFactory
            .getLogger(ErrorHandlerQueueImpl.class);

    private LinkedBlockingQueue<Exception> errorQueue;

    /**
     * default ctor
     */
    public ErrorHandlerQueueImpl() {
        this.errorQueue = new LinkedBlockingQueue<>();
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
    
    @Override
    public void handleException(Throwable e, SessionContext sessionContext) {
        String sessionKeyId = null;
        if (sessionContext != null) {
            sessionKeyId = Arrays.toString(sessionContext.getSessionKey().getId());
        }
        
        Exception causeAndThread = new Exception(
                "IN THREAD: "+Thread.currentThread().getName() +
                "; session:"+sessionKeyId, e);
        try {
            errorQueue.put(causeAndThread);
        } catch (InterruptedException e1) {
            LOG.error(e1.getMessage(), e1);
        }
    }

    @Override
    public void close() throws Exception {
        //TODO: add special exception to queue and recognize it in run method
    }
}
