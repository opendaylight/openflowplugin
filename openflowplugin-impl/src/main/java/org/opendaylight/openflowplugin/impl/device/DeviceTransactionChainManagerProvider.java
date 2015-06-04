/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.device;

import java.util.HashMap;
import java.util.Map;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yangtools.concepts.Registration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 2.6.2015.
 */
public class DeviceTransactionChainManagerProvider {


    private static final Logger LOG = LoggerFactory.getLogger(DeviceTransactionChainManagerProvider.class);
    private static final Map<NodeId, TransactionChainManager> txChManagers = new HashMap<>();

    public TransactionChainManager provideTransactionChainManagerOrWaitForNotification(final ConnectionContext connectionContext,
                                                                                       final DataBroker dataBroker,
                                                                                       final ReadyForNewTransactionChainHandler readyForNewTransactionChainHandler) {
        final NodeId nodeId = connectionContext.getNodeId();
        synchronized (this) {
            TransactionChainManager transactionChainManager = txChManagers.get(nodeId);
            if (null == transactionChainManager) {
                LOG.info("Creating new transaction chain for device {}", nodeId.toString());
                Registration registration = new Registration() {
                    @Override
                    public void close() throws Exception {
                        txChManagers.remove(nodeId);
                    }
                };
                transactionChainManager = new TransactionChainManager(dataBroker, connectionContext, registration);
                txChManagers.put(nodeId, transactionChainManager);
                return transactionChainManager;
            } else {
                try {
                    if (!transactionChainManager.attemptToRegisterHandler(readyForNewTransactionChainHandler)) {
                        if (TransactionChainManager.TransactionChainManagerStatus.WORKING.equals(transactionChainManager.getTransactionChainManagerStatus())) {
                            LOG.info("There already exists one handler for connection described as {}. Connection is working will not try again.", nodeId);
                            connectionContext.closeConnection();
                        } else {
                            LOG.info("There already exists one handler for connection described as {}. Transaction chain manager is in state {}. Will try again.",
                                    nodeId,
                                    transactionChainManager.getTransactionChainManagerStatus());
                            readyForNewTransactionChainHandler.onReadyForNewTransactionChain(connectionContext);
                        }
                    }
                } catch (Exception e) {
                    LOG.info("Transaction closed handler registration for node {} failed because we most probably hit previous transaction chain  manager's close process. Will try again.", nodeId);
                    readyForNewTransactionChainHandler.onReadyForNewTransactionChain(connectionContext);
                }
            }
        }
        return null;
    }


}
