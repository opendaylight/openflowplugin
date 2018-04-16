/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.common.txchain;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;
import org.opendaylight.controller.md.sal.binding.api.BindingTransactionChain;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionChain;
import org.opendaylight.controller.md.sal.common.api.data.TransactionChainClosedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionChainListener;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.openflowplugin.common.wait.SimpleTaskRetryLooper;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * openflowplugin-impl
 * org.opendaylight.openflowplugin.impl.device
 * Package protected class for controlling {@link WriteTransaction} life cycle. It is
 * a {@link TransactionChainListener} and provide package protected methods for writeToTransaction
 * method (wrapped {@link WriteTransaction#put(LogicalDatastoreType, InstanceIdentifier, DataObject)})
 * and submitTransaction method (wrapped {@link WriteTransaction#submit()})
 */
public class TransactionChainManager implements TransactionChainListener, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(TransactionChainManager.class);
    private static final String CANNOT_WRITE_INTO_TRANSACTION = "Cannot write into transaction.";

    private final Object txLock = new Object();
    private final DataBroker dataBroker;
    private final String nodeId;

    @GuardedBy("txLock")
    private ReadWriteTransaction wTx;
    @GuardedBy("txLock")
    private BindingTransactionChain transactionChain;
    @GuardedBy("txLock")
    private boolean submitIsEnabled;
    @GuardedBy("txLock")
    private ListenableFuture<Void> lastSubmittedFuture;

    private volatile boolean initCommit;

    @GuardedBy("txLock")
    private TransactionChainManagerStatus transactionChainManagerStatus = TransactionChainManagerStatus.SLEEPING;

    public TransactionChainManager(@Nonnull final DataBroker dataBroker,
                                   @Nonnull final String deviceIdentifier) {
        this.dataBroker = dataBroker;
        this.nodeId = deviceIdentifier;
        this.lastSubmittedFuture = Futures.immediateFuture(null);
    }

    @GuardedBy("txLock")
    private void createTxChain() {
        this.transactionChain = dataBroker.createTransactionChain(TransactionChainManager.this);
    }

    public boolean initialSubmitWriteTransaction() {
        enableSubmit();
        return submitTransaction();
    }

    /**
     * Method change status for TxChainManager to {@link TransactionChainManagerStatus#WORKING} and it has to make
     * registration for this class instance as {@link TransactionChainListener} to provide possibility a make DS
     * transactions. Call this method for MASTER role only.
     */
    public void activateTransactionManager() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("activateTransactionManager for node {} transaction submit is set to {}",
                    this.nodeId, submitIsEnabled);
        }
        synchronized (txLock) {
            if (TransactionChainManagerStatus.SLEEPING == transactionChainManagerStatus) {
                Preconditions.checkState(transactionChain == null,
                        "TxChainFactory survive last close.");
                Preconditions.checkState(wTx == null,
                        "We have some unexpected WriteTransaction.");
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
    public ListenableFuture<Void> deactivateTransactionManager() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deactivateTransactionManager for node {}", this.nodeId);
        }
        final ListenableFuture<Void> future;
        synchronized (txLock) {
            if (TransactionChainManagerStatus.WORKING == transactionChainManagerStatus) {
                transactionChainManagerStatus = TransactionChainManagerStatus.SLEEPING;
                future = txChainShuttingDown();
                Preconditions.checkState(wTx == null,
                        "We have some unexpected WriteTransaction.");
                Futures.addCallback(future, new FutureCallback<Void>() {
                    @Override
                    public void onSuccess(final Void result) {
                        closeTransactionChain();
                    }

                    @Override
                    public void onFailure(@Nonnull final Throwable t) {
                        closeTransactionChain();
                    }
                });
            } else {
                // TODO : ignoring redundant deactivate invocation
                future = Futures.immediateCheckedFuture(null);
            }
        }
        return future;
    }

    private void closeTransactionChain() {
        if (wTx != null) {
            wTx.cancel();
            wTx = null;
        }
        Optional.ofNullable(transactionChain).ifPresent(TransactionChain::close);
        transactionChain = null;
    }

    public boolean submitTransaction() {
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
            Preconditions.checkState(TransactionChainManagerStatus.WORKING == transactionChainManagerStatus,
                    "we have here Uncompleted Transaction for node {} and we are not MASTER",
                    this.nodeId);
            final CheckedFuture<Void, TransactionCommitFailedException> submitFuture = wTx.submit();
            lastSubmittedFuture = submitFuture;
            wTx = null;
            if (initCommit) {
                try {
                    SimpleTaskRetryLooper looper = new SimpleTaskRetryLooper(500, 4);
                    looper.loopUntilNoException(() -> submitFuture.get(5L, TimeUnit.SECONDS));
                } catch (Exception ex) {
                    return false;
                }
                initCommit = false;
                return true;
            }
            Futures.addCallback(submitFuture, new FutureCallback<Void>() {
                @Override
                public void onSuccess(final Void result) {
                    //NOOP
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
                }
            });
        }
        return true;
    }

    public <T extends DataObject> void addDeleteOperationToTxChain(final LogicalDatastoreType store,
                                                                   final InstanceIdentifier<T> path){
        synchronized (txLock) {
            ensureTransaction();
            if (wTx == null) {
                LOG.debug("WriteTx is null for node {}. Delete {} was not realized.", this.nodeId, path);
                throw new TransactionChainClosedException(CANNOT_WRITE_INTO_TRANSACTION);
            }

            wTx.delete(store, path);
        }
    }

    public <T extends DataObject> void writeToTransaction(final LogicalDatastoreType store,
                                                          final InstanceIdentifier<T> path,
                                                          final T data,
                                                          final boolean createParents){
        synchronized (txLock) {
            ensureTransaction();
            if (wTx == null) {
                LOG.debug("WriteTx is null for node {}. Write data for {} was not realized.", this.nodeId, path);
                throw new TransactionChainClosedException(CANNOT_WRITE_INTO_TRANSACTION);
            }

            wTx.put(store, path, data, createParents);
        }
    }

    public <T extends DataObject> void mergeToTransaction(final LogicalDatastoreType store,
                                                          final InstanceIdentifier<T> path,
                                                          final T data,
                                                          final boolean createParents){
        synchronized (txLock) {
            ensureTransaction();
            if (wTx == null) {
                LOG.debug("WriteTx is null for node {}. Merge data for {} was not realized.", this.nodeId, path);
                throw new TransactionChainClosedException(CANNOT_WRITE_INTO_TRANSACTION);
            }

            wTx.merge(store, path, data, createParents);
        }
    }

    public <T extends DataObject> CheckedFuture<com.google.common.base.Optional<T>, ReadFailedException>
    readFromTransaction(final LogicalDatastoreType store,
                        final InstanceIdentifier<T> path){
        synchronized (txLock) {
            ensureTransaction();
            if (wTx == null) {
                LOG.debug("WriteTx is null for node {}. Read data for {} was not realized.", this.nodeId, path);
                throw new TransactionChainClosedException(CANNOT_WRITE_INTO_TRANSACTION);
            }

            return wTx.read(store, path);
        }
    }

    @Override
    public void onTransactionChainFailed(final TransactionChain<?, ?> chain,
                                         final AsyncTransaction<?, ?> transaction, final Throwable cause) {
        synchronized (txLock) {
            if (TransactionChainManagerStatus.WORKING == transactionChainManagerStatus &&
                    chain.equals(this.transactionChain)) {
                LOG.warn("Transaction chain failed, recreating chain due to ", cause);
                closeTransactionChain();
                createTxChain();
                wTx = null;
            }
        }
    }

    @Override
    public void onTransactionChainSuccessful(final TransactionChain<?, ?> chain) {
        // NOOP
    }

    @GuardedBy("txLock")
   private void ensureTransaction() {
        if (wTx == null && TransactionChainManagerStatus.WORKING == transactionChainManagerStatus
            && transactionChain != null) {
                wTx = transactionChain.newReadWriteTransaction();
        }
    }

    private void enableSubmit() {
        synchronized (txLock) {
            LOG.debug("Transaction submit is enabled for node {}", this.nodeId);
            /* !!!IMPORTANT: never set true without transactionChain */
            submitIsEnabled = transactionChain != null;
        }
    }

    public ListenableFuture<Void> shuttingDown() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("TxManager is going SHUTTING_DOWN for node {}", this.nodeId);
        }
        synchronized (txLock) {
            this.transactionChainManagerStatus = TransactionChainManagerStatus.SHUTTING_DOWN;
            return txChainShuttingDown();
        }
    }

    @GuardedBy("txLock")
    private ListenableFuture<Void> txChainShuttingDown() {
        boolean wasSubmitEnabled = submitIsEnabled;
        submitIsEnabled = false;
        ListenableFuture<Void> future;

        if (!wasSubmitEnabled || transactionChain == null) {
            // stay with actual thread
            future = Futures.immediateCheckedFuture(null);

            if (wTx != null) {
                wTx.cancel();
                wTx = null;
            }
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
            closeTransactionChain();
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
