/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.device;

import java.util.HashMap;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 2.6.2015.
 */
public class DeviceTransactionChainManagerProvider implements ReadyForNewTransactionChainHandler {


    private static final Logger LOG = LoggerFactory.getLogger(DeviceTransactionChainManagerProvider.class);
    private static final HashMap<NodeId, TransactionChainManager> txChManagers = new HashMap<>();

    public TransactionChainManager provideTransactionChainManagerOrWaitForNotification(final ConnectionContext connectionContext,
                                                                                       final DataBroker dataBroker,
                                                                                       final ReadyForNewTransactionChainHandler readyForNewTransactionChainHandler) {

        final NodeId nodeId = connectionContext.getNodeId();
        TransactionChainManager transactionChainManager = txChManagers.get(nodeId);
        if (null == transactionChainManager) {
            LOG.info("Creating new transaction chain for device {}", nodeId.toString());
            transactionChainManager = new TransactionChainManager(dataBroker, connectionContext);
            transactionChainManager.addDeviceTxChainClosedHandler(this);
            txChManagers.put(nodeId, transactionChainManager);
            return transactionChainManager;
        } else {
            LOG.info("Device {} waits for previous connection's transaction chain to be closed.", nodeId.toString());
            transactionChainManager.addDeviceTxChainClosedHandler(readyForNewTransactionChainHandler);
        }

        return null;
    }


    @Override
    public void onReadyForNewTransactionChain(final ConnectionContext connectionContext) {
        txChManagers.remove(connectionContext.getNodeId());
    }
}
