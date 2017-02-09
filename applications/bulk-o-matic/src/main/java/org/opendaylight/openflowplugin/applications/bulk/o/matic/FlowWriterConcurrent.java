/*
 * Copyright (c) 2016 Ericsson Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.bulk.o.matic;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlowWriterConcurrent implements FlowCounterMBean {
    private static final Logger LOG = LoggerFactory.getLogger(FlowWriterConcurrent.class);
    public static final String USING_CONCURRENT_IMPLEMENTATION_OF_FLOW_WRITER = "Using Concurrent implementation of Flow Writer.";
    private final DataBroker dataBroker;
    private final ExecutorService flowPusher;
    private long startTime;
    private AtomicInteger writeOpStatus = new AtomicInteger(FlowCounter.OperationStatus.INIT.status());
    private AtomicInteger countDpnWriteCompletion = new AtomicInteger();
    private AtomicLong taskCompletionTime = new AtomicLong();

    public FlowWriterConcurrent(final DataBroker dataBroker, ExecutorService flowPusher) {
        this.dataBroker = dataBroker;
        this.flowPusher = flowPusher;
        LOG.info(USING_CONCURRENT_IMPLEMENTATION_OF_FLOW_WRITER);
    }

    public void addFlows(Integer dpnCount, Integer flowsPerDPN, int batchSize,
                         int sleepMillis, int sleepAfter, short startTableId, short endTableId,
                         boolean isCreateParents) {
        LOG.info(USING_CONCURRENT_IMPLEMENTATION_OF_FLOW_WRITER);
        countDpnWriteCompletion.set(dpnCount);
        startTime = System.nanoTime();
        for (int i = 1; i <= dpnCount; i++) {
            FlowHandlerTask task = new FlowHandlerTask(Integer.toString(i),
                    flowsPerDPN, true, batchSize, sleepMillis, sleepAfter, startTableId, endTableId, isCreateParents);
            flowPusher.execute(task);
        }
    }

    public void deleteFlows(Integer dpnCount, Integer flowsPerDPN, int batchSize,
                            short startTableId, short endTableId) {
        LOG.info(USING_CONCURRENT_IMPLEMENTATION_OF_FLOW_WRITER);
        countDpnWriteCompletion.set(dpnCount);
        for (int i = 1; i <= dpnCount; i++) {
            FlowHandlerTask task = new FlowHandlerTask(Integer.toString(i), flowsPerDPN, false, batchSize,
                    0, 1, startTableId, endTableId, false);
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

    private class FlowHandlerTask implements Runnable {
        private final String dpId;
        private final boolean add;
        private final int flowsPerDpn;
        private final int batchSize;
        private final int sleepAfter;
        private final int sleepMillis;
        private final short startTableId;
        private final short endTableId;
        private AtomicInteger remainingTxReturn = new AtomicInteger(0);
        private final boolean isCreateParents;

        public FlowHandlerTask(final String dpId,
                               final int flowsPerDpn,
                               final boolean add,
                               final int batchSize,
                               final int sleepMillis,
                               final int sleepAfter,
                               final short startTableId,
                               final short endTableId,
                               final boolean isCreateParents){
            this.dpId = BulkOMaticUtils.DEVICE_TYPE_PREFIX + dpId;
            this.add = add;
            this.flowsPerDpn = flowsPerDpn;
            this.batchSize = batchSize;
            this.sleepMillis = sleepMillis;
            this.sleepAfter = sleepAfter;
            this.startTableId = startTableId;
            this.endTableId = endTableId;
            this.isCreateParents = isCreateParents;
            remainingTxReturn.set(flowsPerDpn/batchSize);
        }

        @Override
        public void run() {
            LOG.info("Starting flow writer task for dpid: {}. Number of transactions: {}", dpId, flowsPerDpn/batchSize);
            writeOpStatus.set(FlowCounter.OperationStatus.IN_PROGRESS.status());
            short tableId = startTableId;
            int numSubmits = flowsPerDpn/batchSize;
            int sourceIp = 1;
            int newBatchSize = batchSize;

            for (int i = 1; i <= numSubmits; i++) {
                WriteTransaction writeTransaction = dataBroker.newWriteOnlyTransaction();
                short k = tableId;
                for (; sourceIp <= newBatchSize; sourceIp++) {
                    String flowId = "Flow-" + dpId + "." + k + "." + sourceIp;
                    Flow flow = null;
                    if (add) {
                        Match match = BulkOMaticUtils.getMatch(sourceIp);
                        flow = BulkOMaticUtils.buildFlow(k, flowId, match);
                    }

                    addFlowToTx(writeTransaction, flowId,
                            BulkOMaticUtils.getFlowInstanceIdentifier(k, flowId, dpId), flow, sourceIp, k);

                    if (sourceIp < newBatchSize) {
                        short a = 1;
                        short b = (short)(endTableId - startTableId + 1);
                        k = (short) (((k + a) % b) + startTableId);
                    }
                }
                Futures.addCallback(writeTransaction.submit(), new DsCallBack(dpId, tableId, k, sourceIp));
                // Wrap around
                tableId = (short)(((k + 1)%((short)(endTableId - startTableId + 1))) + startTableId);
                newBatchSize += batchSize;
                if (((i%sleepAfter) == 0) && (sleepMillis > 0)) {
                    try {
                        Thread.sleep(sleepMillis);
                    } catch (InterruptedException e) {
                        LOG.error("Writer Thread Interrupted: {}", e.getMessage());
                    }
                }
            }
        }

        private void addFlowToTx(WriteTransaction writeTransaction, String flowId, InstanceIdentifier<Flow> flowIid, Flow flow, Integer sourceIp, Short tableId){
            if (add) {
                LOG.trace("Adding flow for flowId: {}, flowIid: {}", flowId, flowIid);
                writeTransaction.put(LogicalDatastoreType.CONFIGURATION, flowIid, flow, isCreateParents);
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
                            writeOpStatus.get() != FlowCounter.OperationStatus.FAILURE.status()) {
                        writeOpStatus.set(FlowCounter.OperationStatus.SUCCESS.status());
                        taskCompletionTime.set(dur);
                    }
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
                writeOpStatus.set(FlowCounter.OperationStatus.FAILURE.status());
            }
        }
    }
}