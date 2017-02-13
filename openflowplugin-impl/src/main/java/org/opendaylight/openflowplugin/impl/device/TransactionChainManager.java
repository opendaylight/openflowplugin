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
import com.google.common.util.concurrent.ListenableFuture;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import org.opendaylight.controller.md.sal.binding.api.BindingTransactionChain;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionChain;
import org.opendaylight.controller.md.sal.common.api.data.TransactionChainClosedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionChainListener;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.LifecycleService;
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
 */
class TransactionChainManager implements TransactionChainListener, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(TransactionChainManager.class);
    private static final String CANNOT_WRITE_INTO_TRANSACTION = "Cannot write into transaction.";

    private final Object txLock = new Object();
    private final DataBroker dataBroker;
    private final String nodeId;
    private LifecycleService lifecycleService;

    @GuardedBy("txLock")
    private WriteTransaction wTx;
    @GuardedBy("txLock")
    private BindingTransactionChain txChainFactory;
    @GuardedBy("txLock")
    private boolean submitIsEnabled;
    @GuardedBy("txLock")
    private ListenableFuture<Void> lastSubmittedFuture;

    private boolean initCommit;

    @GuardedBy("txLock")
    private TransactionChainManagerStatus transactionChainManagerStatus = TransactionChainManagerStatus.SLEEPING;

    TransactionChainManager(@Nonnull final DataBroker dataBroker,
                            @Nonnull final DeviceInfo deviceInfo) {
        this.dataBroker = dataBroker;
        this.nodeId = deviceInfo.getNodeInstanceIdentifier().getKey().getId().getValue();
        this.transactionChainManagerStatus = TransactionChainManagerStatus.SLEEPING;
        this.lastSubmittedFuture = Futures.immediateFuture(null);
    }

    @GuardedBy("txLock")
    private void createTxChain() {
        BindingTransactionChain txChainFactoryTemp = txChainFactory;
        txChainFactory = dataBroker.createTransactionChain(TransactionChainManager.this);
        Optional.ofNullable(txChainFactoryTemp).ifPresent(TransactionChain::close);
    }

    public void setLifecycleService(final LifecycleService lifecycleService) {
        this.lifecycleService = lifecycleService;
    }

    void initialSubmitWriteTransaction() {
        enableSubmit();
        submitWriteTransaction();
    }

    /**
     * Method change status for TxChainManager to {@link TransactionChainManagerStatus#WORKING} and it has to make
     * registration for this class instance as {@link TransactionChainListener} to provide possibility a make DS
     * transactions. Call this method for MASTER role only.
     */
    void activateTransactionManager() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("activateTransactionManager for node {} transaction submit is set to {}", this.nodeId, submitIsEnabled);
        }
        synchronized (txLock) {
            if (TransactionChainManagerStatus.SLEEPING.equals(transactionChainManagerStatus)) {
                Preconditions.checkState(txChainFactory == null, "TxChainFactory survive last close.");
                Preconditions.checkState(wTx == null, "We have some unexpected WriteTransaction.");
                this.transactionChainManagerStatus = TransactionChainManagerStatus.WORKING;
                this.submitIsEnabled = false;
                this.initCommit = true;
                createTxChain();
            }
        }
    }

    /**
     * Method change status for TxChainManger to {@link TransactionChainManagerStatus#SLEEPING} and it unregisters
     * this class instance as {@link TransactionChainListener} so it broke a possibility to write something to DS.
     * Call this method for SLAVE only.
     * @return Future
     */
    ListenableFuture<Void> deactivateTransactionManager() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deactivateTransactionManager for node {}", this.nodeId);
        }
        final ListenableFuture<Void> future;
        synchronized (txLock) {
            if (TransactionChainManagerStatus.WORKING.equals(transactionChainManagerStatus)) {
                transactionChainManagerStatus = TransactionChainManagerStatus.SLEEPING;
                future = txChainShuttingDown();
                Preconditions.checkState(wTx == null, "We have some unexpected WriteTransaction.");
                Futures.addCallback(future, new FutureCallback<Void>() {
                    @Override
                    public void onSuccess(final Void result) {
                        removeTxChainFactory();
                    }

                    @Override
                    public void onFailure(final Throwable t) {
                        removeTxChainFactory();
                    }
                });
            } else {
                // TODO : ignoring redundant deactivate invocation
                future = Futures.immediateCheckedFuture(null);
            }
        }
        return future;
    }

    private void removeTxChainFactory() {
        Optional.ofNullable(txChainFactory).ifPresent(TransactionChain::close);
        txChainFactory = null;
    }

    boolean submitWriteTransaction() {
        synchronized (txLock) {
            if (!submitIsEnabled) {
                if (LOG.isTraceEnabled()) {
                    LOG.trace("transaction not committed - submit block issued");
                }
                return false;
            }
            if (Objects.isNull(wTx)) {
                if (LOG.isTraceEnabled()) {
                    LOG.trace("nothing to commit - submit returns true");
                }
                return true;
            }
            Preconditions.checkState(TransactionChainManagerStatus.WORKING.equals(transactionChainManagerStatus),
                    "we have here Uncompleted Transaction for node {} and we are not MASTER", this.nodeId);
            final CheckedFuture<Void, TransactionCommitFailedException> submitFuture = wTx.submit();
            Futures.addCallback(submitFuture, new FutureCallback<Void>() {
                @Override
                public void onSuccess(final Void result) {
                    if (initCommit) {
                        initCommit = false;
                    }
                }

                @Override
                public void onFailure(final Throwable t) {
                    if (t instanceof TransactionCommitFailedException) {
                        LOG.error("Transaction commit failed. ", t);
                    } else {
                        if (t instanceof CancellationException) {
                            LOG.warn("Submit task was canceled");
                            LOG.trace("Submit exception: ", t);
                        } else {
                            LOG.error("Exception during transaction submitting. ", t);
                        }
                    }
                    if (initCommit) {
                        wTx = null;
                        Optional.ofNullable(lifecycleService).ifPresent(LifecycleService::closeConnection);
                    }
                }
            });
            lastSubmittedFuture = submitFuture;
            wTx = null;
        }
        return true;
    }

    <T extends DataObject> void addDeleteOperationTotTxChain(final LogicalDatastoreType store,
                                                             final InstanceIdentifier<T> path){
        final WriteTransaction writeTx = getTransactionSafely();
        if (Objects.nonNull(writeTx)) {
            writeTx.delete(store, path);
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("WriteTx is null for node {}. Delete {} was not realized.", this.nodeId, path);
            }
            throw new TransactionChainClosedException(CANNOT_WRITE_INTO_TRANSACTION);
        }
    }

    <T extends DataObject> void writeToTransaction(final LogicalDatastoreType store,
                                                   final InstanceIdentifier<T> path,
                                                   final T data,
                                                   final boolean createParents){
        final WriteTransaction writeTx = getTransactionSafely();
        if (Objects.nonNull(writeTx)) {
            writeTx.put(store, path, data, createParents);
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("WriteTx is null for node {}. Write data for {} was not realized.", this.nodeId, path);
            }
            throw new TransactionChainClosedException(CANNOT_WRITE_INTO_TRANSACTION);
        }
    }

    @Override
    public void onTransactionChainFailed(final TransactionChain<?, ?> chain,
                                         final AsyncTransaction<?, ?> transaction, final Throwable cause) {
        if (transactionChainManagerStatus.equals(TransactionChainManagerStatus.WORKING)) {
            LOG.warn("Transaction chain failed, recreating chain due to ", cause);
            recreateTxChain();
        }
    }

    @Override
    public void onTransactionChainSuccessful(final TransactionChain<?, ?> chain) {
        // NOOP
    }

    private void recreateTxChain() {
        synchronized (txLock) {
            createTxChain();
            wTx = null;
        }
    }

    @Nullable
    private WriteTransaction getTransactionSafely() {
            synchronized (txLock) {
                if (wTx == null && TransactionChainManagerStatus.WORKING.equals(transactionChainManagerStatus)) {
                    Optional.ofNullable(txChainFactory).ifPresent(bindingTransactionChain -> wTx = txChainFactory.newWriteOnlyTransaction());
                }
            }
        return wTx;
    }

    @VisibleForTesting
    void enableSubmit() {
        synchronized (txLock) {
            /* !!!IMPORTANT: never set true without txChainFactory */
            submitIsEnabled = txChainFactory != null;
        }
    }

    ListenableFuture<Void> shuttingDown() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("TxManager is going SHUTTING_DOWN for node {}", this.nodeId);
        }
        ListenableFuture<Void> future;
        synchronized (txLock) {
            this.transactionChainManagerStatus = TransactionChainManagerStatus.SHUTTING_DOWN;
            future = txChainShuttingDown();
        }
        return future;
    }

    @GuardedBy("txLock")
    private ListenableFuture<Void> txChainShuttingDown() {
        submitIsEnabled = false;
        ListenableFuture<Void> future;
        if (txChainFactory == null) {
            // stay with actual thread
            future = Futures.immediateCheckedFuture(null);
        } else if (wTx == null) {
            // hijack md-sal thread
            future = lastSubmittedFuture;
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Submitting all transactions for Node {}", this.nodeId);
            }
            // hijack md-sal thread
            future = wTx.submit();
            wTx = null;
        }
        return future;
    }

    @Override
    public void close() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Setting transactionChainManagerStatus to SHUTTING_DOWN for {}", this.nodeId);
        }
        synchronized (txLock) {
            removeTxChainFactory();
        }
    }

    private enum TransactionChainManagerStatus {
        /** txChainManager is sleeping - is not active (SLAVE or default init value) */
        WORKING,
        /** txChainManager is working - is active (MASTER) */
        SLEEPING,
        /** txChainManager is trying to be closed - device disconnecting */
        SHUTTING_DOWN;
    }
}
