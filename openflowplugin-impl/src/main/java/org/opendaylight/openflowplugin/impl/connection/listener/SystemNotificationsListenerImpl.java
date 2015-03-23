/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.connection.listener;

import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.system.rev130927.DisconnectEvent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.system.rev130927.SwitchIdleEvent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.system.rev130927.SystemNotificationsListener;

/**
 * 
 */
public class SystemNotificationsListenerImpl implements SystemNotificationsListener {
    
    private ConnectionContext connectionContext;


    /**
     * @param connectionContext
     */
    public SystemNotificationsListenerImpl(ConnectionContext connectionContext) {
        this.connectionContext = connectionContext;
    }

    @Override
    public void onDisconnectEvent(DisconnectEvent notification) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSwitchIdleEvent(SwitchIdleEvent notification) {
        // TODO Auto-generated method stub

    }

}
