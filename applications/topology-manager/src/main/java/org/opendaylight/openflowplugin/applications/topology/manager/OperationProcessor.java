/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.topology.manager;

import com.google.common.base.Preconditions;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.opendaylight.controller.md.sal.binding.api.BindingTransactionChain;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncTransaction;
import org.opendaylight.controller.md.sal.common.api.data.TransactionChain;
import org.opendaylight.controller.md.sal.common.api.data.TransactionChainListener;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class OperationProcessor implements AutoCloseable, Runnable, TransactionChainListener {
    private static final Logger LOG = LoggerFactory.getLogger(OperationProcessor.class);
    private static final int MAX_TRANSACTION_OPERATIONS = 100;
    private static final int OPERATION_QUEUE_DEPTH = 500;

    private final BlockingQueue<TopologyOperation> queue = new LinkedBlockingQueue<>(OPERATION_QUEUE_DEPTH);
    private final DataBroker dataBroker;
    private final Thread thread;
    private BindingTransactionChain transactionChain;
    private volatile boolean finishing = false;

    public OperationProcessor(final DataBroker dataBroker) {
        this.dataBroker = Preconditions.checkNotNull(dataBroker);
        transactionChain = this.dataBroker.createTransactionChain(this);

        thread = new Thread(this);
        thread.setDaemon(true);
        thread.setName("FlowCapableTopologyExporter-" + FlowCapableTopologyProvider.TOPOLOGY_ID);
    }

    void enqueueOperation(final TopologyOperation task) {
        try {
            queue.put(task);
        } catch (InterruptedException e) {
            LOG.warn("Interrupted while submitting task {}", task, e);
        }
    }

    public void start() {
        thread.start();
    }

    @Override
    public void run() {
            while (!finishing) {
                try {
                    TopologyOperation op = queue.take();

                    LOG.debug("New {} operation available, starting transaction", op);

                    final ReadWriteTransaction tx = transactionChain.newReadWriteTransaction();

                    int ops = 0;
                    do {
                        op.applyOperation(tx);

                        ops++;
                        if (ops < MAX_TRANSACTION_OPERATIONS) {
                            op = queue.poll();
                        } else {
                            op = null;
                        }

                        LOG.debug("Next operation {}", op);
                    } while (op != null);

                    LOG.debug("Processed {} operations, submitting transaction", ops);
                    submitTransaction(tx);
                } catch (final IllegalStateException e) {
                    LOG.warn("Stat DataStoreOperation unexpected State!", e);
                    transactionChain.close();
                    transactionChain = dataBroker.createTransactionChain(this);
                    cleanDataStoreOperQueue();
                } catch (final InterruptedException e) {
                    // This should mean we're shutting down.
                    LOG.debug("Stat Manager DS Operation thread interrupted!", e);
                    finishing = true;
                } catch (final Exception e) {
                    LOG.warn("Stat DataStore Operation executor fail!", e);
                }
            }
        // Drain all events, making sure any blocked threads are unblocked
        cleanDataStoreOperQueue();
    }

    private void submitTransaction(ReadWriteTransaction tx) {
        try {
            tx.submit().checkedGet();
        } catch (final TransactionCommitFailedException e) {
            LOG.warn("Stat DataStoreOperation unexpected State!", e);
            transactionChain.close();
            transactionChain = dataBroker.createTransactionChain(this);
            cleanDataStoreOperQueue();
        }
    }

    private void cleanDataStoreOperQueue() {
        while (!queue.isEmpty()) {
            queue.poll();
        }
    }

    @Override
    public void onTransactionChainFailed(TransactionChain<?, ?> chain, AsyncTransaction<?, ?> transaction, Throwable cause) {
        LOG.warn("Failed to export Topology manager operations, Transaction {} failed: {}", transaction.getIdentifier(), cause.getMessage());
        LOG.debug("Failed to export Topology manager operations.. ", cause);
        transactionChain.close();
        transactionChain = dataBroker.createTransactionChain(this);
        cleanDataStoreOperQueue();
    }

    @Override
    public void onTransactionChainSuccessful(TransactionChain<?, ?> chain) {
        //NOOP
    }

    @Override
    public void close() {
        thread.interrupt();
        try {
            thread.join();
        } catch(InterruptedException e) {
            LOG.debug("Join of thread {} was interrupted", thread.getName(), e);
        }

        if (transactionChain != null) {
            transactionChain.close();
        }

        LOG.debug("OperationProcessor closed");
    }
}
