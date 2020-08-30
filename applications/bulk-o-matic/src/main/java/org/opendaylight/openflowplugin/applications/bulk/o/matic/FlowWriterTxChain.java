/*
 * Copyright (c) 2016, 2017 Ericsson Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.bulk.o.matic;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.Transaction;
import org.opendaylight.mdsal.binding.api.TransactionChain;
import org.opendaylight.mdsal.binding.api.TransactionChainListener;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlowWriterTxChain implements FlowCounterMBean {
    private static final Logger LOG = LoggerFactory.getLogger(FlowWriterTxChain.class);
    private final DataBroker dataBroker;
    private final ExecutorService flowPusher;
    private long startTime;
    private final AtomicInteger writeOpStatus = new AtomicInteger(FlowCounter.OperationStatus.INIT.status());
    private final AtomicInteger countDpnWriteCompletion = new AtomicInteger();
    private final AtomicLong taskCompletionTime = new AtomicLong();

    public FlowWriterTxChain(final DataBroker dataBroker, ExecutorService flowPusher) {
        this.dataBroker = dataBroker;
        this.flowPusher = flowPusher;
        LOG.info("Using Ping Pong Flow Tester Impl");
    }

    public void addFlows(Integer dpnCount, Integer flowsPerDPN, int batchSize, int sleepMillis, int sleepAfter,
            short startTableId, short endTableId, boolean isCreateParents) {
        LOG.info("Using Transaction Chain Flow Writer Impl");
        countDpnWriteCompletion.set(dpnCount);
        startTime = System.nanoTime();
        for (int i = 1; i <= dpnCount; i++) {
            FlowHandlerTask task = new FlowHandlerTask(Integer.toString(i), flowsPerDPN, true, batchSize, sleepMillis,
                    sleepAfter, startTableId, endTableId, isCreateParents);
            flowPusher.execute(task);
        }
    }

    public void deleteFlows(Integer dpnCount, Integer flowsPerDPN, int batchSize, short startTableId,
            short endTableId) {
        LOG.info("Using Transaction Chain Flow Writer Impl");
        countDpnWriteCompletion.set(dpnCount);
        for (int i = 1; i <= dpnCount; i++) {
            FlowHandlerTask task = new FlowHandlerTask(Integer.toString(i), flowsPerDPN, false, batchSize, 0, 1,
                    startTableId, endTableId, false);
            flowPusher.execute(task);
        }
    }

    @Override
    public int getWriteOpStatus() {
        return writeOpStatus.get();
    }

    @Override
    public long getTaskCompletionTime() {
        return taskCompletionTime.get();
    }

    private class FlowHandlerTask implements Runnable, TransactionChainListener {
        private final String dpId;
        private final boolean add;
        private final int flowsPerDpn;
        private final int batchSize;
        private final int sleepAfter;
        private final int sleepMillis;
        private final short startTableId;
        private final short endTableId;
        private final boolean isCreateParents;
        private final AtomicInteger remainingTxReturn = new AtomicInteger(0);

        private TransactionChain txChain;

        FlowHandlerTask(final String dpId,
                        final int flowsPerDpn,
                        final boolean add,
                        final int batchSize,
                        final int sleepMillis,
                        final int sleepAfter,
                        final short startTableId,
                        final short endTableId,
                        final boolean isCreateParents) {
            this.dpId = BulkOMaticUtils.DEVICE_TYPE_PREFIX + dpId;
            this.add = add;
            this.flowsPerDpn = flowsPerDpn;
            this.batchSize = batchSize;
            this.sleepMillis = sleepMillis;
            this.sleepAfter = sleepAfter;
            this.startTableId = startTableId;
            this.endTableId = endTableId;
            this.isCreateParents = isCreateParents;
            remainingTxReturn.set(flowsPerDpn / batchSize);
        }

        @Override
        public void run() {
            writeOpStatus.set(FlowCounter.OperationStatus.IN_PROGRESS.status());
            short tableId = startTableId;
            int numSubmits = flowsPerDpn / batchSize;
            int sourceIp = 1;
            int newBatchSize = batchSize;
            LOG.info("Number of Txn for dpId: {} is: {}", dpId, numSubmits);

            txChain = dataBroker.createMergingTransactionChain(this);
            LOG.info("Creating new txChain: {} for dpid: {}", txChain, dpId);

            for (int i = 1; i <= numSubmits; i++) {
                WriteTransaction writeTransaction = txChain.newWriteOnlyTransaction();
                short calculatedTableId = tableId;
                for (; sourceIp <= newBatchSize; sourceIp++) {
                    String flowId = "Flow-" + dpId + "." + calculatedTableId + "." + sourceIp;
                    Flow flow = null;
                    if (add) {
                        Match match = BulkOMaticUtils.getMatch(sourceIp);
                        flow = BulkOMaticUtils.buildFlow(calculatedTableId, flowId, match);
                    }

                    writeTxToDs(writeTransaction, flowId,
                            BulkOMaticUtils.getFlowInstanceIdentifier(calculatedTableId, flowId, dpId),
                            flow, sourceIp, calculatedTableId);

                    if (sourceIp < newBatchSize) {
                        short numberA = 1;
                        short numberB = (short) (endTableId - startTableId + 1);
                        calculatedTableId = (short) ((calculatedTableId + numberA) % numberB + startTableId);
                    }
                }
                LOG.debug("Submitting Txn for dpId: {}, begin tableId: {}, end tableId: {}, sourceIp: {}", dpId,
                        tableId, calculatedTableId, sourceIp - 1);
                writeTransaction.commit().addCallback(
                        new DsCallBack(dpId, tableId, calculatedTableId, sourceIp, txChain),
                        MoreExecutors.directExecutor());
                // Wrap around
                tableId = (short) ((calculatedTableId + 1) % (short) (endTableId - startTableId + 1) + startTableId);
                newBatchSize += batchSize;
                if (i % sleepAfter == 0 && sleepMillis > 0) {
                    try {
                        Thread.sleep(sleepMillis);
                    } catch (InterruptedException e) {
                        LOG.error("Writer Thread Interrupted: {}", e.getMessage());
                    }
                }
            }
            LOG.info("Completed FlowHandlerTask thread for dpid: {}", dpId);
        }

        @Override
        public void onTransactionChainFailed(TransactionChain transactionChain,
                Transaction asyncTransaction, Throwable throwable) {
            LOG.error("Transaction chain: {} FAILED at asyncTransaction: {} due to: ", transactionChain,
                    asyncTransaction.getIdentifier(), throwable);
            transactionChain.close();
        }

        @Override
        public void onTransactionChainSuccessful(TransactionChain transactionChain) {
            LOG.info("Transaction chain: {} closed successfully.", transactionChain);
        }

        private void writeTxToDs(WriteTransaction writeTransaction, String flowId, InstanceIdentifier<Flow> flowIid,
                Flow flow, Integer sourceIp, Short tableId) {
            if (add) {
                LOG.trace("Adding flow for flowId: {}, flowIid: {}", flowId, flowIid);
                if (isCreateParents) {
                    writeTransaction.mergeParentStructurePut(LogicalDatastoreType.CONFIGURATION, flowIid, flow);
                } else {
                    writeTransaction.put(LogicalDatastoreType.CONFIGURATION, flowIid, flow);
                }
            } else {
                LOG.trace("Deleting flow for flowId: {}, flowIid: {}", flowId, flowIid);
                writeTransaction.delete(LogicalDatastoreType.CONFIGURATION, flowIid);
            }
        }

        private class DsCallBack implements FutureCallback<Object> {
            private final String dpId;
            private final int sourceIp;
            private final short endTableId;
            private final short beginTableId;
            private final TransactionChain txChain;

            DsCallBack(String dpId, short beginTableId, short endTableId, int sourceIp,
                    TransactionChain txChain) {
                this.dpId = dpId;
                this.sourceIp = sourceIp;
                this.endTableId = endTableId;
                this.beginTableId = beginTableId;
                this.txChain = txChain;
            }

            @Override
            public void onSuccess(Object notUsed) {
                if (remainingTxReturn.decrementAndGet() <= 0) {
                    long dur = System.nanoTime() - startTime;
                    LOG.info("Completed all flows installation for: dpid: {} in {}ns", dpId, dur);
                    if (0 == countDpnWriteCompletion.decrementAndGet()
                            && writeOpStatus.get() != FlowCounter.OperationStatus.FAILURE.status()) {
                        writeOpStatus.set(FlowCounter.OperationStatus.SUCCESS.status());
                        taskCompletionTime.set(dur);
                    }
                    txChain.close();
                }
            }

            @Override
            public void onFailure(Throwable error) {
                if (remainingTxReturn.decrementAndGet() <= 0) {
                    long dur = System.nanoTime() - startTime;
                    LOG.info("Completed all flows installation for: dpid: {} in {}ns", dpId, dur);
                }
                LOG.error("Error: {} in Datastore write operation: dpid: {}, begin tableId: {}, "
                        + "end tableId: {}, sourceIp: {} ", error, dpId, beginTableId, endTableId, sourceIp);
                writeOpStatus.set(FlowCounter.OperationStatus.FAILURE.status());
            }
        }
    }
}
