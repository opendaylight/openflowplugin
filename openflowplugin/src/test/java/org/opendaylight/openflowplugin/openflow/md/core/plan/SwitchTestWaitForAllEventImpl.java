/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.plan;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author mirehak
 *
 */
public class SwitchTestWaitForAllEventImpl implements SwitchTestWaitForAllEvent {
    
    private Set<SwitchTestWaitForRpcEvent> eventBag;

    /**
     * @param eventBag the eventBag to set
     */
    public void setEventBag(Set<SwitchTestWaitForRpcEvent> eventBag) {
        this.eventBag = eventBag;
    }

    @Override
    public Set<SwitchTestWaitForRpcEvent> getWaitEventBag() {
        return eventBag;
    }
    
    @Override
    public String toString() {
        List<String> rpcNames = null;
        if (eventBag != null) {
            rpcNames = new ArrayList<>();
            for (SwitchTestWaitForRpcEvent waitEvent : eventBag) {
                rpcNames.add(waitEvent.getRpcName()+"["+waitEvent.getXid()+"]");
            }
        }
        return "SwitchTestWaitForAllEventImpl "+rpcNames;
    }

}
