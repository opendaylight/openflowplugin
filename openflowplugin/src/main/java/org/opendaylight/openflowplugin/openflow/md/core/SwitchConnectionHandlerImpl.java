/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core;

import java.net.InetAddress;

import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowjava.protocol.api.connection.SwitchConnectionHandler;
import org.opendaylight.openflowplugin.openflow.md.core.session.OFSessionUtil;
import org.opendaylight.openflowplugin.openflow.md.queue.QueueKeeperLightImpl;

/**
 * @author mirehak
 *
 */
public class SwitchConnectionHandlerImpl implements SwitchConnectionHandler {
    
    private QueueKeeperLightImpl<Object> queueKeeper;
    private ErrorHandler errorHandler;

    /**
     * 
     */
    public SwitchConnectionHandlerImpl() {
        queueKeeper = new QueueKeeperLightImpl<>();
        queueKeeper.setListenerMapping(OFSessionUtil.getListenersMap());
        queueKeeper.init();
        // TODO: add pop-listeners consuming object processed via queue
        //queueKeeper.addPopListener(listener);
        
        errorHandler = new ErrorHandlerQueueImpl();
    }

    @Override
    public boolean accept(InetAddress address) {
        // TODO:: add policy derived rules
        return true;
    }

    @Override
    public void onSwitchConnected(ConnectionAdapter connectionAdapter) {
        ConnectionConductor conductor = ConnectionConductorFactory.createConductor(
                connectionAdapter, queueKeeper);
        conductor.setErrorHandler(errorHandler);
    }

}
