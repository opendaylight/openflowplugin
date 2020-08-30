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
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
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
    private final AtomicInteger writeOpStatus = new AtomicInteger(FlowCounter.OperationStatus.INIT.status());
    private final AtomicInteger countDpnWriteCompletion = new AtomicInteger();
    private final AtomicLong taskCompletionTime = new AtomicLong();

    public FlowWriterSequential(final DataBroker dataBroker, ExecutorService flowPusher) {
        this.dataBroker = dataBroker;
        this.flowPusher = flowPusher;
        LOG.info("Using Sequential implementation of Flow Writer.");
    }

    public void addFlows(Integer count, Integer flowsPerDPN, int batchSize, int sleepMillis, short startTableId,
            short endTableId, boolean isCreateParents) {
        LOG.info("Using Sequential implementation of Flow Writer.");
        this.dpnCount = count;
        countDpnWriteCompletion.set(count);
        startTime = System.nanoTime();
        for (int i = 1; i <= count; i++) {
            FlowHandlerTask task = new FlowHandlerTask(Integer.toString(i), flowsPerDPN, true, batchSize, sleepMillis,
                    startTableId, endTableId, isCreateParents);
            flowPusher.execute(task);
        }
    }

    public void deleteFlows(Integer count, Integer flowsPerDPN, int batchSize, short startTableId,
            short endTableId) {
        LOG.info("Using Sequential implementation of Flow Writer.");
        countDpnWriteCompletion.set(count);
        for (int i = 1; i <= count; i++) {
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

        FlowHandlerTask(final String dpId,
                        final int flowsPerDpn,
                        final boolean add,
                        final int batchSize,
                        int sleepMillis,
                        final short startTableId,
                        final short endTableId,
                        final boolean isCreateParents) {
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
            LOG.info("Starting flow writer task for dpid: {}. Number of transactions: {}", dpId,
                    flowsPerDpn / batchSize);
            writeOpStatus.set(FlowCounter.OperationStatus.IN_PROGRESS.status());

            Short tableId = startTableId;

            WriteTransaction writeTransaction = dataBroker.newWriteOnlyTransaction();
            short calculatedTableId = tableId;

            int sourceIp = 1;
            for (; sourceIp <= batchSize; sourceIp++) {
                String flowId = "Flow-" + dpId + "." + calculatedTableId + "." + sourceIp;
                LOG.debug("Adding flow with id: {}", flowId);
                Flow flow = null;
                if (add) {
                    Match match = BulkOMaticUtils.getMatch(sourceIp);
                    flow = BulkOMaticUtils.buildFlow(calculatedTableId, flowId, match);
                }
                addFlowToTx(writeTransaction, flowId,
                        BulkOMaticUtils.getFlowInstanceIdentifier(calculatedTableId, flowId, dpId), flow);

                if (sourceIp < batchSize) {
                    short numberA = 1;
                    short numberB = (short) (endTableId - startTableId + 1);
                    calculatedTableId = (short) ((calculatedTableId + numberA) % numberB + startTableId);
                }
            }

            LOG.debug("Submitting Txn for dpId: {}, begin tableId: {}, end tableId: {}, sourceIp: {}", dpId, tableId,
                    calculatedTableId, sourceIp);

            writeTransaction.commit().addCallback(new DsCallBack(dpId, sourceIp, calculatedTableId),
                    MoreExecutors.directExecutor());
        }

        private void addFlowToTx(WriteTransaction writeTransaction, String flowId, InstanceIdentifier<Flow> flowIid,
                Flow flow) {
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
            private final short tableId;

            private int sourceIp;

            DsCallBack(String dpId, int sourceIp, short tableId) {
                this.dpId = dpId;
                this.sourceIp = sourceIp;
                short numberA = 1;
                short numberB = (short) (endTableId - startTableId + 1);
                this.tableId = (short) ((tableId + numberA) % numberB + startTableId);
            }

            @Override
            public void onSuccess(Object notUsed) {
                if (sourceIp > flowsPerDpn) {
                    long dur = System.nanoTime() - startTime;
                    LOG.info("Completed all flows installation for: dpid: {}, tableId: {}, sourceIp: {} in {}ns", dpId,
                            tableId, sourceIp, dur);
                    if (0 == countDpnWriteCompletion.decrementAndGet()
                            && writeOpStatus.get() != FlowCounter.OperationStatus.FAILURE.status()) {
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
                short calculatedTableId = tableId;
                for (; sourceIp <= newBatchSize; sourceIp++) {
                    String flowId = "Flow-" + dpId + "." + calculatedTableId + "." + sourceIp;
                    Flow flow = null;
                    if (add) {
                        Match match = BulkOMaticUtils.getMatch(sourceIp);
                        flow = BulkOMaticUtils.buildFlow(calculatedTableId, flowId, match);
                    }
                    LOG.debug("Adding flow with id: {}", flowId);
                    addFlowToTx(writeTransaction, flowId,
                            BulkOMaticUtils.getFlowInstanceIdentifier(calculatedTableId, flowId, dpId),
                            flow);

                    if (sourceIp < newBatchSize) {
                        short numberA = 1;
                        short numberB = (short) (endTableId - startTableId + 1);
                        calculatedTableId = (short) ((calculatedTableId + numberA) % numberB + startTableId);
                    }
                }
                LOG.debug("OnSuccess: Submitting Txn for dpId: {}, begin tableId: {}, end tableId: {}, sourceIp: {}",
                        dpId, tableId, calculatedTableId, sourceIp);
                writeTransaction.commit().addCallback(new DsCallBack(dpId, sourceIp, calculatedTableId),
                        MoreExecutors.directExecutor());
            }

            @Override
            public void onFailure(Throwable error) {
                LOG.error("Error: {} in Datastore write operation: dpid: {}, tableId: {}, sourceIp: {}", error, dpId,
                        tableId, sourceIp);
                writeOpStatus.set(FlowCounter.OperationStatus.FAILURE.status());
            }
        }
    }
}
