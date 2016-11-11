/*
 * Copyright (c) 2016 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.southboundmanager.openflowservice.api;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Users are suppose to extend this callable whenever calling the openflow facade APIs.
 * It is called in the following cases:
 * 1. Openflow RPC error
 * 2. OFPT Error
 * 3. Unknown/Unhandled error
 */
public abstract class ErrorCallable implements Callable {
    private AtomicBoolean isCalled = new AtomicBoolean(false);
    private OpenflowErrorCause cause;
    public void setIsCalled(boolean isCalled) {
        this.isCalled.set(isCalled);
    }
    public boolean getIsCalled() {
        return this.isCalled.get();
    }
    public void setCause(OpenflowErrorCause cause) {
        this.cause = cause;
    }
    public OpenflowErrorCause getCause() {
        return this.cause;
    }
}
