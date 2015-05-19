/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.device;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import org.opendaylight.controller.md.sal.binding.api.BindingTransactionChain;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionChain;
import org.opendaylight.controller.md.sal.common.api.data.TransactionChainListener;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * openflowplugin-impl
 * org.opendaylight.openflowplugin.impl.device
 * <p/>
 * Package protected class for controlling {@link WriteTransaction} life cycle. It is
 * a {@link TransactionChainListener} and provide package protected methods for writeToTransaction
 * method (wrapped {@link WriteTransaction#put(LogicalDatastoreType, InstanceIdentifier, DataObject)})
 * and submitTransaction method (wrapped {@link WriteTransaction#submit()})
 *
 * @author <a href="mailto:vdemcak@cisco.com">Vaclav Demcak</a>
 *         <p/>
 *         Created: Apr 2, 2015
 */
@VisibleForTesting
class TransactionChainManager implements TransactionChainListener {

    private static final Logger LOG = LoggerFactory.getLogger(TransactionChainManager.class);

    private final HashedWheelTimer hashedWheelTimer;
    private final DataBroker dataBroker;
    private final long maxTx;
    private final long timerValue;
    private BindingTransactionChain txChainFactory;
    private WriteTransaction wTx;
    private Timeout submitTaskTime;
    private long nrOfActualTx;
    private boolean submitIsEnabled;

    TransactionChainManager(@Nonnull final DataBroker dataBroker,
                            @Nonnull final HashedWheelTimer hashedWheelTimer,
                            final long maxTx,
                            final long timerValue) {
        this.dataBroker = Preconditions.checkNotNull(dataBroker);
        this.hashedWheelTimer = Preconditions.checkNotNull(hashedWheelTimer);
        this.maxTx = maxTx;
        txChainFactory = dataBroker.createTransactionChain(TransactionChainManager.this);
        nrOfActualTx = 0L;
        this.timerValue = timerValue;
        LOG.debug("created txChainManager with operation limit {}", maxTx);
    }


    public void commitOperationsGatheredInOneTransaction() {
        enableSubmit();
        submitTransaction();
    }

    public void startGatheringOperationsToOneTransaction() {
        submitIsEnabled = false;
    }

    <T extends DataObject> void writeToTransaction(final LogicalDatastoreType store,
                                                   final InstanceIdentifier<T> path, final T data) {
        try {
            WriteTransaction writeTx = getTransactionSafely();
            writeTx.put(store, path, data);
            countTxInAndCommit();
        } catch (Exception e) {
            LOG.warn("failed to put into writeOnlyTransaction: {}", e.getMessage());
            LOG.trace("failed to put into writeOnlyTransaction.. ", e);
        }
    }

    private WriteTransaction getTransactionSafely() {
        if (wTx == null) {
            wTx = txChainFactory.newWriteOnlyTransaction();
        }
        return wTx;
    }

    <T extends DataObject> void addDeleteOperationTotTxChain(final LogicalDatastoreType store,
                                                             final InstanceIdentifier<T> path) {
        try {
            WriteTransaction writeTx = getTransactionSafely();
            writeTx.delete(store, path);
            countTxInAndCommit();
        } catch (Exception e) {
            LOG.warn("failed to put into writeOnlyTransaction : {}", e.getMessage());
            LOG.trace("failed to put into writeOnlyTransaction.. ", e);
        }
    }

    private void countTxInAndCommit() {
        nrOfActualTx += 1L;
        if (nrOfActualTx >= maxTx) {
            submitTransaction();
        }
    }

    void submitScheduledTransaction(final Timeout timeout) {
        if (timeout.isCancelled()) {
            // zombie timer executed
            return;
        }

        if (submitIsEnabled) {
            submitTransaction();
        } else {
            LOG.info("transaction submit task will not be scheduled - submit block issued.");
        }
    }

    void submitTransaction() {
        if (submitIsEnabled) {
            if (wTx != null && nrOfActualTx > 0) {
                LOG.trace("submitting transaction, counter: {}", nrOfActualTx);
                CheckedFuture<Void, TransactionCommitFailedException> submitResult = wTx.submit();
                try {
                    submitResult.get();
                } catch (ExecutionException | InterruptedException e) {
                    recreateTxChain();
                }
                hookTimeExpenseCounter(submitResult, String.valueOf(wTx.getIdentifier()) + "::" + nrOfActualTx);
                wTx = null;
                nrOfActualTx = 0L;
            }
            if (submitTaskTime != null) {
                // if possible then cancel current timer (even if being executed via timer)
                submitTaskTime.cancel();
            }
            submitTaskTime = hashedWheelTimer.newTimeout(new TimerTask() {
                @Override
                public void run(final Timeout timeout) throws Exception {
                    submitScheduledTransaction(timeout);
                }
            }, timerValue, TimeUnit.MILLISECONDS);

        } else {
            LOG.trace("transaction not committed - submit block issued");
        }
    }

    private static void hookTimeExpenseCounter(final CheckedFuture<Void, TransactionCommitFailedException> submitResult, final String name) {
        final long submitFiredTime = System.nanoTime();
        LOG.debug("submit of {} fired", name);
        Futures.addCallback(submitResult, new FutureCallback<Void>() {
            @Override
            public void onSuccess(final Void result) {
                LOG.debug("submit of {} finished in {} ms", name, System.nanoTime() - submitFiredTime);
            }

            @Override
            public void onFailure(final Throwable t) {
                LOG.warn("transaction submit failed: {}", t.getMessage());
            }
        });
    }

    void enableSubmit() {
        submitIsEnabled = true;
    }

    @Override
    public void onTransactionChainFailed(final TransactionChain<?, ?> chain,
                                         final AsyncTransaction<?, ?> transaction, final Throwable cause) {
        LOG.warn("txChain failed -> recreating", cause);
        recreateTxChain();
    }

    private void recreateTxChain() {
        txChainFactory.close();
        txChainFactory = dataBroker.createTransactionChain(TransactionChainManager.this);
    }

    @Override
    public void onTransactionChainSuccessful(final TransactionChain<?, ?> chain) {
        // NOOP - only yet, here is probably place for notification to get new WriteTransaction
    }

}
