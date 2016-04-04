/*
 * Copyright (c) 2016 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.bulk.o.matic;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import org.opendaylight.controller.md.sal.binding.api.BindingTransactionChain;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionChain;
import org.opendaylight.controller.md.sal.common.api.data.TransactionChainListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public class FlowWriterTxChain implements FlowCounterMBean {
    private static final Logger LOG = LoggerFactory.getLogger(FlowWriterTxChain.class);
    private final DataBroker dataBroker;
    private final ExecutorService flowPusher;
    private long startTime;
    private AtomicInteger writeOpStatus = new AtomicInteger(FlowCounter.OperationStatus.INIT.ordinal());
    private AtomicInteger countDpnWriteCompletion = new AtomicInteger(0);

    public FlowWriterTxChain(final DataBroker dataBroker, ExecutorService flowPusher){
        this.dataBroker = dataBroker;
        this.flowPusher = flowPusher;
        LOG.info("Using Ping Pong Flow Tester Impl");
    }

    public void addFlows(Integer dpnCount, Integer flowsPerDPN, int batchSize,
                         int sleepMillis, int sleepAfter, short startTableId, short endTableId) {
        countDpnWriteCompletion.set(dpnCount);
        startTime = System.nanoTime();
        for (int i = 1; i <= dpnCount; i++) {
            FlowHandlerTask task = new FlowHandlerTask(Integer.toString(i),
                    flowsPerDPN, true, batchSize, sleepMillis, sleepAfter, startTableId, endTableId);
            flowPusher.execute(task);
        }
    }

    public void deleteFlows(Integer dpnCount, Integer flowsPerDPN, int batchSize,
                            short startTableId, short endTableId){
        countDpnWriteCompletion.set(dpnCount);
        for (int i = 1; i <= dpnCount; i++) {
            FlowHandlerTask task = new FlowHandlerTask(Integer.toString(i), flowsPerDPN, false, batchSize,
                    0, 1, startTableId, endTableId);
            flowPusher.execute(task);
        }
    }

    public long getFlowCount() {
        return 0;
    }
    public int getReadOpStatus() {
        return 0;
    }
    public int getWriteOpStatus() {
        return writeOpStatus.get();
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
        private AtomicInteger remainingTxReturn = new AtomicInteger(0);

        BindingTransactionChain txChain;

        public FlowHandlerTask(final String dpId,
                               final int flowsPerDpn,
                               final boolean add,
                               final int batchSize,
                               final int sleepMillis,
                               final int sleepAfter,
                               final short startTableId,
                               final short endTableId){
            this.dpId = "openflow:" + dpId;
            this.add = add;
            this.flowsPerDpn = flowsPerDpn;
            this.batchSize = batchSize;
            this.sleepMillis = sleepMillis;
            this.sleepAfter = sleepAfter;
            this.startTableId = startTableId;
            this.endTableId = endTableId;
            remainingTxReturn.set(flowsPerDpn/batchSize);
        }

        @Override
        public void run() {
            writeOpStatus.set(FlowCounter.OperationStatus.INPROGRESS.ordinal());
            short tableId = startTableId;
            int numSubmits = flowsPerDpn/batchSize;
            int sourceIp = 1;
            int newBatchSize = batchSize;
            LOG.info("Number of Txn for dpId: {} is: {}", dpId, numSubmits);

            txChain = dataBroker.createTransactionChain(this);
            LOG.info("Creating new txChain: {} for dpid: {}", txChain, dpId);

            for (int i = 1; i <= numSubmits; i++) {
                WriteTransaction writeTransaction;
                try {
                    writeTransaction = txChain.newWriteOnlyTransaction();
                } catch (Exception e) {
                    LOG.error("Transaction creation failed in txChain: {}, due to: {}", txChain, e);
                    break;
                }
                short k = tableId;
                for (; sourceIp <= newBatchSize; sourceIp++) {
                    String flowId = "Flow-" + dpId + "." + k + "." + sourceIp;
                    Flow flow = null;
                    if (add) {
                        Match match = BulkOMaticUtils.getMatch(sourceIp);
                        flow = BulkOMaticUtils.buildFlow(k, flowId, match);
                    }

                    writeTxToDs(writeTransaction, flowId,
                            BulkOMaticUtils.getFlowInstanceIdentifier(k, flowId, dpId), flow, sourceIp, k);

                    if (sourceIp < newBatchSize) {
                        short a = 1;
                        short b = (short) (endTableId - startTableId + 1);
                        k = (short) (((k + a) % b) + startTableId);
                    }
                }
                LOG.debug("Submitting Txn for dpId: {}, begin tableId: {}, end tableId: {}, sourceIp: {}", dpId, tableId, k, sourceIp - 1);
                Futures.addCallback(writeTransaction.submit(), new DsCallBack(dpId, tableId, k, sourceIp));
                // Wrap around
                tableId = (short) (((k + 1) % ((short) (endTableId - startTableId + 1))) + startTableId);
                newBatchSize += batchSize;
                if (((i % sleepAfter) == 0) && (sleepMillis > 0)) {
                    try {
                        Thread.sleep(sleepMillis);
                    } catch (InterruptedException e) {
                        LOG.info("Writer Thread Interrupted: {}", e.getMessage());
                    }
                }
            }
            LOG.info("Completed FlowHandlerTask thread for dpid: {}", dpId);
        }

        @Override
        public void onTransactionChainFailed(TransactionChain<?, ?> transactionChain, AsyncTransaction<?, ?> asyncTransaction, Throwable throwable) {
            LOG.error("Transaction chain: {} FAILED at asyncTransaction: {} due to: {}", transactionChain,
                    asyncTransaction.getIdentifier(), throwable);
            transactionChain.close();
        }

        @Override
        public void onTransactionChainSuccessful(TransactionChain<?, ?> transactionChain) {
            LOG.info("Transaction chain: {} closed successfully.", transactionChain);
        }

        private void writeTxToDs(WriteTransaction writeTransaction, String flowId, InstanceIdentifier<Flow> flowIid, Flow flow, Integer sourceIp, Short tableId){
            if (add) {
                LOG.trace("Adding flow for flowId: {}, flowIid: {}", flowId, flowIid);
                writeTransaction.put(LogicalDatastoreType.CONFIGURATION, flowIid, flow, true);
            } else {
                LOG.trace("Deleting flow for flowId: {}, flowIid: {}", flowId, flowIid);
                writeTransaction.delete(LogicalDatastoreType.CONFIGURATION, flowIid);
            }
        }

        private class DsCallBack implements FutureCallback {
            private String dpId;
            private int sourceIp;
            private short endTableId;
            private short beginTableId;

            public DsCallBack(String dpId, Short beginTableId, Short endTableId, Integer sourceIp) {
                this.dpId = dpId;
                this.sourceIp = sourceIp;
                this.endTableId = endTableId;
                this.beginTableId = beginTableId;
            }

            @Override
            public void onSuccess(Object o) {
                if (remainingTxReturn.decrementAndGet() <= 0) {
                    long dur = System.nanoTime() - startTime;
                    LOG.info("Completed all flows installation for: dpid: {} in {}ns", dpId,
                            dur);
                    if(0 == countDpnWriteCompletion.decrementAndGet() &&
                            writeOpStatus.get() != FlowCounter.OperationStatus.FAILURE.ordinal()) {
                       writeOpStatus.set(FlowCounter.OperationStatus.SUCCESS.ordinal());
                    }
                    txChain.close();
                }
            }

            public void onFailure(Throwable error) {
                if (remainingTxReturn.decrementAndGet() <= 0) {
                    long dur = System.nanoTime() - startTime;
                    LOG.info("Completed all flows installation for: dpid: {} in {}ns", dpId,
                            dur);
                }
                LOG.error("Error: {} in Datastore write operation: dpid: {}, begin tableId: {}, " +
                        "end tableId: {}, sourceIp: {} ", error, dpId, beginTableId, endTableId, sourceIp);
                writeOpStatus.set(FlowCounter.OperationStatus.FAILURE.ordinal());
            }
        }
    }
}