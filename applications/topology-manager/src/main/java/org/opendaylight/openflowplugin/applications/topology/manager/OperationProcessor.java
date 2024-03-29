/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.topology.manager;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.openflowplugin.common.txchain.TransactionChainManager;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Component(service = OperationProcessor.class)
public final class OperationProcessor implements AutoCloseable, Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(OperationProcessor.class);
    private static final int MAX_TRANSACTION_OPERATIONS = 100;
    private static final int OPERATION_QUEUE_DEPTH = 500;
    private static final String TOPOLOGY_MANAGER = "ofp-topo-processor";

    private final BlockingQueue<TopologyOperation> queue = new LinkedBlockingQueue<>(OPERATION_QUEUE_DEPTH);
    private final Thread thread;
    private final TransactionChainManager transactionChainManager;
    private volatile boolean finishing = false;

    @Inject
    @Activate
    public OperationProcessor(@Reference final DataBroker dataBroker) {
        transactionChainManager = new TransactionChainManager(dataBroker, TOPOLOGY_MANAGER);
        transactionChainManager.activateTransactionManager();
        transactionChainManager.initialSubmitWriteTransaction();

        thread = new Thread(this);
        thread.setDaemon(true);
        thread.setName("ofp-topo-expo-" + FlowCapableTopologyProvider.TOPOLOGY_ID);
        thread.start();
        LOG.debug("OperationProcessor started");
    }

    void enqueueOperation(final TopologyOperation task) {
        try {
            queue.put(task);
        } catch (InterruptedException e) {
            LOG.warn("Interrupted while submitting task {}", task, e);
        }
    }

    @Override
    public void run() {
        while (!finishing) {
            try {
                TopologyOperation op = queue.take();

                LOG.debug("New {} operation available, starting transaction", op);

                int ops = 0;
                do {
                    op.applyOperation(transactionChainManager);

                    ops++;
                    if (ops < MAX_TRANSACTION_OPERATIONS) {
                        op = queue.poll();
                    } else {
                        op = null;
                    }

                    LOG.debug("Next operation {}", op);
                } while (op != null);

                LOG.debug("Processed {} operations, submitting transaction", ops);
                if (!transactionChainManager.submitTransaction()) {
                    cleanDataStoreOperQueue();
                }
            } catch (final InterruptedException e) {
                // This should mean we're shutting down.
                LOG.debug("Stat Manager DS Operation thread interrupted!", e);
                finishing = true;
            }
        }
        // Drain all events, making sure any blocked threads are unblocked
        cleanDataStoreOperQueue();
    }

    private void cleanDataStoreOperQueue() {
        while (!queue.isEmpty()) {
            queue.poll();
        }
    }

    @PreDestroy
    @Deactivate
    @Override
    public void close() {
        thread.interrupt();
        try {
            thread.join();
        } catch (InterruptedException e) {
            LOG.debug("Join of thread {} was interrupted", thread.getName(), e);
        }

        transactionChainManager.close();
        LOG.debug("OperationProcessor stopped");
    }
}
