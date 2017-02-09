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

public class FlowWriterSequential implements FlowCounterMBean {
    private static final Logger LOG = LoggerFactory.getLogger(FlowWriterSequential.class);
    private final DataBroker dataBroker;
    private final ExecutorService flowPusher;
    protected int dpnCount;
    private long startTime;
    private AtomicInteger writeOpStatus = new AtomicInteger(FlowCounter.OperationStatus.INIT.status());
    private AtomicInteger countDpnWriteCompletion = new AtomicInteger();
    private AtomicLong taskCompletionTime = new AtomicLong();

    public FlowWriterSequential(final DataBroker dataBroker, ExecutorService flowPusher) {
        this.dataBroker = dataBroker;
        this.flowPusher = flowPusher;
        LOG.info("Using Sequential implementation of Flow Writer.");
    }

    public void addFlows(Integer dpnCount, Integer flowsPerDPN, int batchSize, int sleepMillis,
                         short startTableId, short endTableId, boolean isCreateParents) {
        LOG.info("Using Sequential implementation of Flow Writer.");
        this.dpnCount = dpnCount;
        countDpnWriteCompletion.set(dpnCount);
        startTime = System.nanoTime();
        for (int i = 1; i <= dpnCount; i++) {
            FlowHandlerTask task = new FlowHandlerTask(Integer.toString(i), flowsPerDPN, true, batchSize,
                    sleepMillis, startTableId, endTableId, isCreateParents);
            flowPusher.execute(task);
        }
    }

    public void deleteFlows(Integer dpnCount, Integer flowsPerDPN, int batchSize, short startTableId,
                            short endTableId) {
        LOG.info("Using Sequential implementation of Flow Writer.");
        countDpnWriteCompletion.set(dpnCount);
        for (int i = 1; i <= dpnCount; i++) {
            FlowHandlerTask task = new FlowHandlerTask(Integer.toString(i), flowsPerDPN, false, batchSize, 0,
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

    private class FlowHandlerTask implements Runnable {
        private final String dpId;
        private final int flowsPerDpn;
        private final boolean add;
        private final int batchSize;
        private final int sleepMillis;
        private final short startTableId;
        private final short endTableId;
        private final boolean isCreateParents;

        public FlowHandlerTask(final String dpId,
                               final int flowsPerDpn,
                               final boolean add,
                               final int batchSize,
                               int sleepMillis,
                               final short startTableId,
                               final short endTableId,
                               final boolean isCreateParents){
            this.dpId = BulkOMaticUtils.DEVICE_TYPE_PREFIX + dpId;
            this.add = add;
            this.flowsPerDpn = flowsPerDpn;
            this.batchSize = batchSize;
            this.sleepMillis = sleepMillis;
            this.startTableId = startTableId;
            this.endTableId = endTableId;
            this.isCreateParents = isCreateParents;
        }

        @Override
        public void run() {
            LOG.info("Starting flow writer task for dpid: {}. Number of transactions: {}", dpId, flowsPerDpn/batchSize);
            writeOpStatus.set(FlowCounter.OperationStatus.IN_PROGRESS.status());

            Short tableId = startTableId;
            Integer sourceIp = 1;

            WriteTransaction writeTransaction = dataBroker.newWriteOnlyTransaction();
            short k = tableId;

            for (; sourceIp <= batchSize; sourceIp++) {
                String flowId = "Flow-" + dpId + "." + k + "." + sourceIp;
                LOG.debug("Adding flow with id: {}", flowId);
                Flow flow = null;
                if (add) {
                    Match match = BulkOMaticUtils.getMatch(sourceIp);
                    flow = BulkOMaticUtils.buildFlow(k, flowId, match);
                }
                addFlowToTx(writeTransaction, flowId,
                        BulkOMaticUtils.getFlowInstanceIdentifier(k, flowId, dpId), flow);

                if (sourceIp < batchSize) {
                    short a = 1;
                    short b = (short)(endTableId - startTableId + 1);
                    k = (short) (((k + a) % b) + startTableId);
                }
            }

            LOG.debug("Submitting Txn for dpId: {}, begin tableId: {}, end tableId: {}, sourceIp: {}", dpId, tableId, k, sourceIp);

            Futures.addCallback(writeTransaction.submit(), new DsCallBack(dpId, sourceIp, k));
        }

        private void addFlowToTx(WriteTransaction writeTransaction, String flowId, InstanceIdentifier<Flow> flowIid,
                                 Flow flow) {
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
            private Integer sourceIp;
            private Short tableId;

            public DsCallBack(String dpId, Integer sourceIp, Short tableId) {
                this.dpId = dpId;
                this.sourceIp = sourceIp;
                short a = 1;
                short b = (short)(endTableId - startTableId + 1);
                this.tableId = (short) (((tableId + a) % b) + startTableId);
            }

            @Override
            public void onSuccess(Object o) {
                if (sourceIp > flowsPerDpn) {
                    long dur = System.nanoTime() - startTime;
                    LOG.info("Completed all flows installation for: dpid: {}, tableId: {}, sourceIp: {} in {}ns", dpId,
                            tableId, sourceIp, dur);
                    if(0 == countDpnWriteCompletion.decrementAndGet() &&
                            writeOpStatus.get() != FlowCounter.OperationStatus.FAILURE.status()) {
                        writeOpStatus.set(FlowCounter.OperationStatus.SUCCESS.status());
                        taskCompletionTime.set(dur);
                    }
                    return;
                }
                try {
                    if (sleepMillis > 0) {
                        Thread.sleep(sleepMillis);
                    }
                } catch (InterruptedException e) {
                    LOG.error("Writer Thread Interrupted while sleeping: {}", e.getMessage());
                }

                WriteTransaction writeTransaction = dataBroker.newWriteOnlyTransaction();
                int newBatchSize = sourceIp + batchSize - 1;
                short k = tableId;
                for (; sourceIp <= newBatchSize; sourceIp++) {
                    String flowId = "Flow-" + dpId + "." + k + "." + sourceIp;
                    Flow flow = null;
                    if (add) {
                        Match match = BulkOMaticUtils.getMatch(sourceIp);
                        flow = BulkOMaticUtils.buildFlow(k, flowId, match);
                    }
                    LOG.debug("Adding flow with id: {}", flowId);
                    addFlowToTx(writeTransaction, flowId,
                            BulkOMaticUtils.getFlowInstanceIdentifier(k, flowId, dpId), flow);

                    if (sourceIp < newBatchSize) {
                        short a = 1;
                        short b = (short)(endTableId - startTableId + 1);
                        k = (short) (((k + a) % b) + startTableId);
                    }
                }
                LOG.debug("OnSuccess: Submitting Txn for dpId: {}, begin tableId: {}, end tableId: {}, sourceIp: {}",
                        dpId, tableId, k, sourceIp);
                Futures.addCallback(writeTransaction.submit(), new DsCallBack(dpId, sourceIp, k));
            }

            public void onFailure(Throwable error) {
                LOG.error("Error: {} in Datastore write operation: dpid: {}, tableId: {}, sourceIp: {}",
                        error, dpId, tableId, sourceIp);
                writeOpStatus.set(FlowCounter.OperationStatus.FAILURE.status());
            }
        }
    }
}