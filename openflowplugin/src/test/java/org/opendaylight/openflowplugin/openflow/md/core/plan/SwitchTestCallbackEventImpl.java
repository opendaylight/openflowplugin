/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.plan;

import java.util.concurrent.Callable;

/**
 * @author mirehak
 *
 */
public class SwitchTestCallbackEventImpl implements SwitchTestCallbackEvent {

    private Callable<Void> callable;
    
    /**
     * @param callable the callable to set
     */
    public void setCallable(Callable<Void> callable) {
        this.callable = callable;
    }

    @Override
    public Callable<Void> getCallback() {
        return callable;
    }
    
    @Override
    public String toString() {
        String callableDesc = null;
        if (callable != null) {
            callableDesc = "Callable<Void>:"+System.identityHashCode(callable);
        }
        return "SwitchTestCallbackEventImpl [" + callableDesc + "]";
    }

}
