/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.connection;

import org.opendaylight.openflowplugin.api.ConnectionException;
import org.opendaylight.openflowplugin.api.openflow.md.core.ErrorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * dumping all exceptions to log.
 */
public class ErrorHandlerSimpleImpl implements ErrorHandler {

    private static final Logger LOG = LoggerFactory
            .getLogger(ErrorHandlerSimpleImpl.class);

    @Override
    public void handleException(Throwable throwable) {
        if (throwable instanceof ConnectionException) {
            LOG.warn("Exception", throwable);
        } else {
            LOG.error("Exception", throwable);
        }
    }
}
