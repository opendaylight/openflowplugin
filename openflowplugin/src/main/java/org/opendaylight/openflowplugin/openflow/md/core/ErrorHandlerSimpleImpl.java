/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core;

import java.util.Arrays;

import org.opendaylight.openflowplugin.api.ConnectionException;
import org.opendaylight.openflowplugin.api.openflow.md.core.ErrorHandler;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.SessionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * dumping all exceptions to log
 * @author mirehak
 */
public class ErrorHandlerSimpleImpl implements ErrorHandler {

    private static final Logger LOG = LoggerFactory
            .getLogger(ErrorHandlerSimpleImpl.class);

    @Override
    public void handleException(Throwable e, SessionContext sessionContext) {
        String sessionKeyId = null;
        if (sessionContext != null) {
            sessionKeyId = Arrays.toString(sessionContext.getSessionKey().getId());
        }
        
        if (e instanceof ConnectionException) {
            LOG.warn("exception -> {}, session -> {}", e.getMessage(), sessionKeyId, e);
        } else {
            LOG.error("exception -> {}, session -> {}", e.getMessage(), sessionKeyId, e);
        }
    }
}
