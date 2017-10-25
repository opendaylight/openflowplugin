/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.core.connection;

final class SimpleRpcListener extends AbstractRpcListener<Void> {
    public SimpleRpcListener(final Object message, final String failureInfo) {
        super(message, failureInfo);
    }

    @Override
    protected void operationSuccessful() {
        successfulRpc(null);
    }
}
