/*
 * Copyright (c) 2017 Ericsson Systems, Inc. and others.  All rights reserved.
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TableWriter implements FlowCounterMBean {
    private static final Logger LOG = LoggerFactory.getLogger(TableWriter.class);

    private final AtomicInteger writeOpStatus = new AtomicInteger(FlowCounter.OperationStatus.INIT.status());
    private final AtomicLong taskCompletionTime = new AtomicLong(BulkOMaticUtils.DEFAULT_COMPLETION_TIME);
    private final AtomicInteger successfulWrites = new AtomicInteger();
    private final AtomicInteger failedWrites = new AtomicInteger();
    private final DataBroker dataBroker;
    private final ExecutorService tablePusher;

    public TableWriter(final DataBroker dataBroker, final ExecutorService tablePusher) {
        this.dataBroker = dataBroker;
        this.tablePusher = tablePusher;
    }

    public void addTables(final int dpnCount, final short startTableId, final short endTableId) {
        LOG.info("Starting to add tables: {} to {} on each of {}", startTableId, endTableId, dpnCount);
        TableHandlerTask task = new TableHandlerTask(dpnCount, startTableId, endTableId, true);
        tablePusher.execute(task);
    }

    public void deleteTables(int dpnCount, short startTableId, short endTableId) {
        LOG.info("Starting to delete tables: {} to {} on each of {}", startTableId, endTableId, dpnCount);
        TableHandlerTask task = new TableHandlerTask(dpnCount, startTableId, endTableId, false);
        tablePusher.execute(task);
    }

    @Override
    public int getWriteOpStatus() {
        return writeOpStatus.get();
    }

    @Override
    public long getTaskCompletionTime() {
        return taskCompletionTime.get();
    }

    @Override
    public long getTableCount() {
        return successfulWrites.get();
    }

    private class TableHandlerTask implements Runnable {

        private final short startTableId;
        private final short endTableId;
        private final int dpnCount;
        private final boolean isAdd;

        TableHandlerTask(int dpnCount, short startTableId, short endTableId, boolean isAdd) {
            this.dpnCount = dpnCount;
            this.startTableId = startTableId;
            this.endTableId = endTableId;
            this.isAdd = isAdd;
        }

        @Override
        public void run() {
            writeOpStatus.set(FlowCounter.OperationStatus.IN_PROGRESS.status());
            int totalTables = dpnCount * (endTableId - startTableId + 1);

            for (int dpn = 1; dpn <= dpnCount; dpn++) {
                String dpId = BulkOMaticUtils.DEVICE_TYPE_PREFIX + String.valueOf(dpn);
                for (short tableId = startTableId; tableId <= endTableId; tableId++) {
                    WriteTransaction wtx = dataBroker.newWriteOnlyTransaction();
                    Table table = new TableBuilder().withKey(new TableKey(Uint8.valueOf(tableId))).build();
                    InstanceIdentifier<Table> tableIId = BulkOMaticUtils.getTableId(tableId, dpId);

                    if (isAdd) {
                        wtx.mergeParentStructurePut(LogicalDatastoreType.CONFIGURATION, tableIId, table);
                    } else {
                        wtx.delete(LogicalDatastoreType.CONFIGURATION, tableIId);
                    }

                    wtx.commit().addCallback(new FutureCallback<Object>() {
                        @Override
                        public void onSuccess(Object voidParameter) {
                            if (successfulWrites.incrementAndGet() == totalTables) {
                                if (failedWrites.get() > 0) {
                                    writeOpStatus.set(FlowCounter.OperationStatus.FAILURE.status());
                                } else {
                                    writeOpStatus.set(FlowCounter.OperationStatus.SUCCESS.status());
                                }
                            }
                        }

                        @Override
                        public void onFailure(Throwable throwable) {
                            LOG.error("Table addition Failed.", throwable);
                            if (failedWrites.incrementAndGet() == totalTables) {
                                writeOpStatus.set(FlowCounter.OperationStatus.FAILURE.status());
                            }
                        }
                    }, MoreExecutors.directExecutor());
                }
            }
        }
    }
}
