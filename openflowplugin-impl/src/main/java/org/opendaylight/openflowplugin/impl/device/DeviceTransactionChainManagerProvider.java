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
import org.opendaylight.openflowplugin.impl.util.DeviceStateUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yangtools.concepts.Registration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 2.6.2015.
 */
public class DeviceTransactionChainManagerProvider {


    private static final Logger LOG = LoggerFactory.getLogger(DeviceTransactionChainManagerProvider.class);
    private final Map<NodeId, TransactionChainManager> txChManagers = new HashMap<>();
    private final DataBroker dataBroker;

    public DeviceTransactionChainManagerProvider(final DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    public TransactionChainManagerRegistration provideTransactionChainManager(final ConnectionContext connectionContext) {
        final NodeId nodeId = connectionContext.getNodeId();
        TransactionChainManager transactionChainManager;
        boolean ownedByCurrentContext = false;
        synchronized (this) {
            transactionChainManager = txChManagers.get(nodeId);
            if (null == transactionChainManager) {
                LOG.info("Creating new transaction chain for device {}", nodeId.toString());
                Registration registration = new Registration() {
                    @Override
                    public void close() throws Exception {
                        LOG.trace("TransactionChainManagerRegistration Close called for {}", nodeId);
                        txChManagers.remove(nodeId);
                    }
                };
                txChManagers.put(nodeId, transactionChainManager);
                ownedByCurrentContext = true;
            }
        }
        TransactionChainManagerRegistration transactionChainManagerRegistration = new TransactionChainManagerRegistration(ownedByCurrentContext, transactionChainManager);
        return transactionChainManagerRegistration;
    }

    public final class TransactionChainManagerRegistration {
        private final TransactionChainManager transactionChainManager;
        private final boolean ownedByConnectionContext;

        private TransactionChainManagerRegistration(final boolean ownedByConnectionContext, final TransactionChainManager transactionChainManager) {
            this.transactionChainManager = transactionChainManager;
            this.ownedByConnectionContext = ownedByConnectionContext;
        }

        public boolean ownedByInvokingConnectionContext() {
            return ownedByConnectionContext;
        }

        public TransactionChainManager getTransactionChainManager() {
            return transactionChainManager;
        }
    }
}
