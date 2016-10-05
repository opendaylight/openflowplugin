/*
 * Copyright (c) 2016 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.reference.app.subscriber;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import org.opendaylight.controller.md.sal.binding.api.BindingTransactionChain;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionChain;
import org.opendaylight.controller.md.sal.common.api.data.TransactionChainListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ref.app.rev160504.SubscriberListEntries;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ref.app.rev160504.subscriber.list.entries.SubscriberListEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ref.app.rev160504.subscriber.list.entries.SubscriberListEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ref.app.rev160504.subscriber.list.entries.SubscriberListEntryKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * subscriber task to add/delete subscriber data in config data store.
 */
public class SubscriberTask implements Runnable, TransactionChainListener {

    private static final Logger LOG = LoggerFactory.getLogger(SubscriberTask.class);
    private final boolean isAdd;
    private final int index;
    private final int count;
    private final String startIp;
    private final int batchSize;
    private AtomicInteger remainingTxReturn = new AtomicInteger(0);
    private BindingTransactionChain txChain;
    private final DataBroker dataBroker;

    public SubscriberTask(final boolean add,
                          final int batchSize,
                          final int index,
                          final int count,
                          final String startIp,
                          final DataBroker dataBroker) {
        this.isAdd = add;
        this.startIp = startIp;
        this.index = index;
        this.count = count;
        this.dataBroker = dataBroker;
        this.batchSize = batchSize;
        remainingTxReturn.set(count / batchSize);
    }

    @Override
    public void run() {
        int numSubmits = count / batchSize;
        numSubmits = (count % batchSize == 0) ? numSubmits : numSubmits + 1;
        int currentIndex = index;
        String currentStartIp = this.startIp;
        int totalCount = index + count;
        int batchCount;
        LOG.info("Number of Txn for start-index {}, start-ip {} and count {} is {}", index, startIp, count, numSubmits);
        txChain = dataBroker.createTransactionChain(this);
        for (int i = 1; i <= numSubmits; i++) {
            batchCount = currentIndex + batchSize;
            if (batchCount > totalCount) {
                batchCount = totalCount;
            }
            WriteTransaction writeTransaction;
            try {
                writeTransaction = txChain.newWriteOnlyTransaction();
            } catch (Exception e) {
                LOG.error("Transaction creation failed in txChain: {}, due to: {}", txChain, e);
                break;
            }
            for (; currentIndex < batchCount; currentIndex++) {

                if (isAdd) {
                    SubscriberListEntryBuilder entryBuilder = new SubscriberListEntryBuilder();
                    entryBuilder.setKey(new SubscriberListEntryKey((long) currentIndex));
                    entryBuilder.setIndex((long) currentIndex);
                    entryBuilder.setSubscriberIp(currentStartIp);
                    currentStartIp = getNextIPV4Address(currentStartIp);
                    writeTransaction.put(LogicalDatastoreType.CONFIGURATION, createSubsEntryIdentifier(currentIndex), entryBuilder.build(), true);
                } else {
                    writeTransaction.delete(LogicalDatastoreType.CONFIGURATION, createSubsEntryIdentifier(currentIndex));
                }
            }
            LOG.debug("Submitting Txn till index {}.", (currentIndex-1));
            Futures.addCallback(writeTransaction.submit(), new DSOperationCallback(isAdd, index, count));
        }
        LOG.info("Completed SubscriberTask thread for index {} and count {}", index, count);
    }

    private String getNextIPV4Address(String ip) {
        String[] nums = ip.split("\\.");
        int i = (Integer.parseInt(nums[0]) << 24 | Integer.parseInt(nums[2]) << 8
                | Integer.parseInt(nums[1]) << 16 | Integer.parseInt(nums[3])) + 1;

        // change address 255.255.255.255 to 0.0.0.0
        if ((byte) i == -1) i++;
        return String.format("%d.%d.%d.%d", i >>> 24 & 0xFF, i >> 16 & 0xFF,
                i >> 8 & 0xFF, i >> 0 & 0xFF);
    }

    private InstanceIdentifier<SubscriberListEntry> createSubsEntryIdentifier(
            int index) {
        return InstanceIdentifier
                .builder(SubscriberListEntries.class)
                .child(SubscriberListEntry.class,
                        new SubscriberListEntryKey((long) index))
                .build();
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

    private class DSOperationCallback implements FutureCallback {
        private int index;
        private int count;
        private boolean operation;

        public DSOperationCallback(boolean operation, int index, int count) {
            this.index = index;
            this.count = count;
            this.operation = operation;
        }

        @Override
        public void onSuccess(Object o) {
            if (remainingTxReturn.decrementAndGet() <= 0) {
                LOG.info("operation completed successfully for referece-app subscriber. isAdd: {}, index: {}, count: {}",
                        operation, index, count);
                txChain.close();
            }
        }

        @Override
        public void onFailure(Throwable error) {
            if (remainingTxReturn.decrementAndGet() <= 0) {
                LOG.info("operation failed for referece-app subscriber. isAdd {}, index: {}, count: {}",
                        operation, index, count);
            }
            LOG.error("Error: {} in Datastore write operation: isAdd {},  index: {}, count: {}",
                    error, operation, index, count);
        }
    }

}