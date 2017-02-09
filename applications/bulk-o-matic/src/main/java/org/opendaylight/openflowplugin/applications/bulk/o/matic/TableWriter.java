package org.opendaylight.openflowplugin.applications.bulk.o.matic;

import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class TableWriter implements FlowCounterMBean {

    private final Logger LOG = LoggerFactory.getLogger(TableWriter.class);

    private final AtomicInteger writeOpStatus = new AtomicInteger(FlowCounter.OperationStatus.INIT.status());
    private final AtomicLong taskCompletionTime = new AtomicLong(BulkOMaticUtils.DEFAULT_COMPLETION_TIME);
    private final AtomicInteger successfulWrites = new AtomicInteger(0);
    private final AtomicInteger failedWrites = new AtomicInteger(0);

    private final DataBroker dataBroker;
    private final ExecutorService tablePusher;

    public TableWriter(DataBroker dataBroker, ExecutorService tablePusher) {
        this.dataBroker = dataBroker;
        this.tablePusher = tablePusher;
    }

    public void addTables(int dpnCount, short startTableId, short endTableId) {
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
    public long getFlowCount() {
        return BulkOMaticUtils.DEFAULT_FLOW_COUNT;
    }

    @Override
    public int getReadOpStatus() {
        return BulkOMaticUtils.DEFUALT_STATUS;
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
    public String getUnits() {
        return BulkOMaticUtils.DEFAULT_UNITS;
    }

    @Override
    public long getTableCount() {
        return successfulWrites.get();
    }

    private class TableHandlerTask implements Runnable {

        private short startTableId;
        private short endTableId;
        private int dpnCount;
        private boolean isAdd;

        public TableHandlerTask(int dpnCount, short startTableId, short endTableId, boolean isAdd) {
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
                    Table table = new TableBuilder().setKey(new TableKey(tableId))
                            .setId(tableId)
                            .build();
                    InstanceIdentifier<Table> tableIId = BulkOMaticUtils.getTableId(tableId, dpId);

                    if (isAdd) {
                        wtx.put(LogicalDatastoreType.CONFIGURATION, tableIId, table, true);
                    } else {
                        wtx.delete(LogicalDatastoreType.CONFIGURATION, tableIId);
                    }

                    CheckedFuture<Void, TransactionCommitFailedException> future = wtx.submit();

                    Futures.addCallback(future, new FutureCallback<Void>() {
                        @Override
                        public void onSuccess(@Nullable Void v) {
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
                            LOG.error("Table addition Failed. Error: {}", throwable);
                            if (failedWrites.incrementAndGet() == totalTables) {
                                writeOpStatus.set(FlowCounter.OperationStatus.FAILURE.status());
                            }
                        }
                    });
                }
            }
        }
    }
}