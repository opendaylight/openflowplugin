/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.common.txchain;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.checkerframework.checker.lock.qual.GuardedBy;
import org.checkerframework.checker.lock.qual.Holding;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.ReadWriteTransaction;
import org.opendaylight.mdsal.binding.api.TransactionChain;
import org.opendaylight.mdsal.binding.api.TransactionChainClosedException;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.openflowplugin.common.wait.SimpleTaskRetryLooper;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.Empty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The openflowplugin-impl.org.opendaylight.openflowplugin.impl.device
 * package protected class for controlling {@link WriteTransaction} life cycle. It listens on chains and provides
 * package-protected methods for writeToTransaction
 * method (wrapped {@link WriteTransaction#put(LogicalDatastoreType, InstanceIdentifier, DataObject)})
 * and submitTransaction method (wrapped {@link WriteTransaction#commit()}).
 */
public class TransactionChainManager implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(TransactionChainManager.class);
    private static final String CANNOT_WRITE_INTO_TRANSACTION = "Cannot write into transaction.";

    private final ReadWriteLock readWriteTransactionLock = new ReentrantReadWriteLock();
    private final Object txLock = new Object();
    private final DataBroker dataBroker;
    private final String nodeId;

    @GuardedBy("txLock")
    private ReadWriteTransaction writeTx;
    @GuardedBy("txLock")
    private TransactionChain transactionChain;
    @GuardedBy("txLock")
    private boolean submitIsEnabled;
    @GuardedBy("txLock")
    private FluentFuture<? extends CommitInfo> lastSubmittedFuture = CommitInfo.emptyFluentFuture();
    @GuardedBy("txLock")
    private TransactionChainManagerStatus transactionChainManagerStatus = TransactionChainManagerStatus.SLEEPING;

    private volatile boolean initCommit;

    public TransactionChainManager(final DataBroker dataBroker, final String nodeId) {
        this.dataBroker = requireNonNull(dataBroker);
        this.nodeId = requireNonNull(nodeId);
    }

    @Holding("txLock")
    private void createTxChain() {
        final var prev = transactionChain;
        final var next = dataBroker.createTransactionChain();
        transactionChain = next;
        if (prev != null) {
            prev.close();
        }
        next.addCallback(new FutureCallback<Empty>() {
            @Override
            public void onSuccess(final Empty result) {
                // No-op
            }

            @Override
            public void onFailure(final Throwable cause) {
                onTransactionChainFailed(next, cause);
            }
        });
    }

    public boolean initialSubmitWriteTransaction() {
        enableSubmit();
        return submitTransaction();
    }

    /**
     * Method change status for TxChainManager to WORKING and it has to make registration for this instance as
     * a {@link TransactionChain} callback to provide possibility a make DS transactions. Call this method for MASTER
     * role only.
     */
    public void activateTransactionManager() {
        LOG.debug("activateTransactionManager for node {} transaction submit is set to {}", nodeId, submitIsEnabled);
        synchronized (txLock) {
            if (TransactionChainManagerStatus.SLEEPING == transactionChainManagerStatus) {
                Preconditions.checkState(transactionChain == null, "TxChainFactory survive last close.");
                Preconditions.checkState(writeTx == null, "We have some unexpected WriteTransaction.");
                transactionChainManagerStatus = TransactionChainManagerStatus.WORKING;
                submitIsEnabled = false;
                initCommit = true;
                createTxChain();
            }
        }
    }

    /**
     * Method change status for TxChainManger to SLEEPING and it unregisters this instance so it broke a possibility to
     * write something to DS. Call this method for SLAVE only.
     *
     * @return Future competing when deactivation completes
     */
    public FluentFuture<?> deactivateTransactionManager() {
        LOG.debug("deactivateTransactionManager for node {}", nodeId);
        final FluentFuture<? extends CommitInfo> future;
        synchronized (txLock) {
            if (TransactionChainManagerStatus.WORKING == transactionChainManagerStatus) {
                transactionChainManagerStatus = TransactionChainManagerStatus.SLEEPING;
                future = txChainShuttingDown();
                Preconditions.checkState(writeTx == null, "We have some unexpected WriteTransaction.");
                future.addCallback(new FutureCallback<CommitInfo>() {
                    @Override
                    public void onSuccess(final CommitInfo result) {
                        closeTransactionChain();
                    }

                    @Override
                    public void onFailure(final Throwable throwable) {
                        closeTransactionChain();
                    }
                }, MoreExecutors.directExecutor());
            } else {
                // ignoring redundant deactivate invocation
                future = CommitInfo.emptyFluentFuture();
            }
        }
        return future;
    }

    private void closeTransactionChain() {
        if (writeTx != null) {
            writeTx.cancel();
            writeTx = null;
        }
        if (transactionChain != null) {
            transactionChain.close();
            transactionChain = null;
        }
    }

    @GuardedBy("txLock")
    public boolean submitTransaction() {
        return submitTransaction(false);
    }

    @GuardedBy("txLock")
    @SuppressWarnings("checkstyle:IllegalCatch")
    public boolean submitTransaction(final boolean doSync) {
        synchronized (txLock) {
            if (!submitIsEnabled) {
                LOG.trace("transaction not committed - submit block issued");
                return false;
            }
            if (writeTx == null) {
                LOG.trace("nothing to commit - submit returns true");
                return true;
            }
            Preconditions.checkState(TransactionChainManagerStatus.WORKING == transactionChainManagerStatus,
                    "we have here Uncompleted Transaction for node {} and we are not MASTER",
                    nodeId);
            final FluentFuture<? extends CommitInfo> submitFuture = writeTx.commit();
            lastSubmittedFuture = submitFuture;
            writeTx = null;

            if (initCommit || doSync) {
                try {
                    SimpleTaskRetryLooper looper = new SimpleTaskRetryLooper(500, 6);
                    looper.loopUntilNoException(() -> submitFuture.get(5L, TimeUnit.SECONDS));
                } catch (Exception ex) {
                    LOG.error("Exception during INITIAL({}) || doSync({}) transaction submitting for device {}",
                            initCommit, doSync, nodeId, ex);
                    return false;
                }
                initCommit = false;
                return true;
            }

            submitFuture.addCallback(new FutureCallback<CommitInfo>() {
                @Override
                public void onSuccess(final CommitInfo result) {
                    //NOOP
                }

                @Override
                public void onFailure(final Throwable throwable) {
                    if (throwable instanceof InterruptedException || throwable instanceof ExecutionException) {
                        LOG.error("Transaction commit failed. ", throwable);
                    } else if (throwable instanceof CancellationException) {
                        LOG.warn("Submit task was canceled");
                        LOG.trace("Submit exception: ", throwable);
                    } else {
                        LOG.error("Exception during transaction submitting. ", throwable);
                    }
                }
            }, MoreExecutors.directExecutor());
        }
        return true;
    }

    public <T extends DataObject> void addDeleteOperationToTxChain(final LogicalDatastoreType store,
                                                                    final InstanceIdentifier<T> path) {
        synchronized (txLock) {
            ensureTransaction();
            if (writeTx == null) {
                LOG.debug("WriteTx is null for node {}. Delete {} was not realized.", nodeId, path);
                throw new TransactionChainClosedException(CANNOT_WRITE_INTO_TRANSACTION);
            }

            writeTx.delete(store, path);
        }
    }

    public <T extends DataObject> void writeToTransaction(final LogicalDatastoreType store,
                                                          final InstanceIdentifier<T> path,
                                                          final T data,
                                                          final boolean createParents) {
        synchronized (txLock) {
            ensureTransaction();
            if (writeTx == null) {
                LOG.debug("WriteTx is null for node {}. Write data for {} was not realized.", nodeId, path);
                throw new TransactionChainClosedException(CANNOT_WRITE_INTO_TRANSACTION);
            }

            if (createParents) {
                writeTx.mergeParentStructurePut(store, path, data);
            } else {
                writeTx.put(store, path, data);
            }
        }
    }

    public <T extends DataObject> void mergeToTransaction(final LogicalDatastoreType store,
                                                          final InstanceIdentifier<T> path,
                                                          final T data,
                                                          final boolean createParents) {
        synchronized (txLock) {
            ensureTransaction();
            if (writeTx == null) {
                LOG.debug("WriteTx is null for node {}. Merge data for {} was not realized.", nodeId, path);
                throw new TransactionChainClosedException(CANNOT_WRITE_INTO_TRANSACTION);
            }

            if (createParents) {
                writeTx.mergeParentStructureMerge(store, path, data);
            } else {
                writeTx.merge(store, path, data);
            }
        }
    }

    public <T extends DataObject> ListenableFuture<Optional<T>>
        readFromTransaction(final LogicalDatastoreType store, final InstanceIdentifier<T> path) {
        synchronized (txLock) {
            ensureTransaction();
            if (writeTx == null) {
                LOG.debug("WriteTx is null for node {}. Read data for {} was not realized.", nodeId, path);
                throw new TransactionChainClosedException(CANNOT_WRITE_INTO_TRANSACTION);
            }

            return writeTx.read(store, path);
        }
    }

    @VisibleForTesting
    void onTransactionChainFailed(final TransactionChain chain, final Throwable cause) {
        synchronized (txLock) {
            if (TransactionChainManagerStatus.WORKING == transactionChainManagerStatus
                    && chain.equals(transactionChain)) {
                LOG.warn("Transaction chain failed, recreating chain due to ", cause);
                closeTransactionChain();
                createTxChain();
                writeTx = null;
            }
        }
    }

    @Holding("txLock")
    private void ensureTransaction() {
        if (writeTx == null && TransactionChainManagerStatus.WORKING == transactionChainManagerStatus
                && transactionChain != null) {
            writeTx = transactionChain.newReadWriteTransaction();
        }
    }

    private void enableSubmit() {
        synchronized (txLock) {
            /* !!!IMPORTANT: never set true without transactionChain */
            submitIsEnabled = transactionChain != null;
        }
    }

    public FluentFuture<?> shuttingDown() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("TxManager is going SHUTTING_DOWN for node {}", nodeId);
        }
        synchronized (txLock) {
            transactionChainManagerStatus = TransactionChainManagerStatus.SHUTTING_DOWN;
            return txChainShuttingDown();
        }
    }

    @GuardedBy("txLock")
    private FluentFuture<? extends CommitInfo> txChainShuttingDown() {
        boolean wasSubmitEnabled = submitIsEnabled;
        submitIsEnabled = false;
        FluentFuture<? extends CommitInfo> future;

        if (!wasSubmitEnabled || transactionChain == null) {
            // stay with actual thread
            future = CommitInfo.emptyFluentFuture();

            if (writeTx != null) {
                writeTx.cancel();
                writeTx = null;
            }
        } else if (writeTx == null) {
            // hijack md-sal thread
            future = lastSubmittedFuture;
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Submitting all transactions for Node {}", nodeId);
            }
            // hijack md-sal thread
            future = writeTx.commit();
            writeTx = null;
        }

        return future;
    }

    @Override
    public void close() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Setting transactionChainManagerStatus to SHUTTING_DOWN for {}", nodeId);
        }
        synchronized (txLock) {
            closeTransactionChain();
        }
    }

    private enum TransactionChainManagerStatus {
        /**
         * txChainManager is working - is active (MASTER).
         */
        WORKING,
        /**
         * txChainManager is sleeping - is not active (SLAVE or default init value).
         */
        SLEEPING,
        /**
         * txChainManager is trying to be closed - device disconnecting.
         */
        SHUTTING_DOWN
    }

    public void acquireWriteTransactionLock() {
        readWriteTransactionLock.writeLock().lock();
    }

    public void releaseWriteTransactionLock() {
        readWriteTransactionLock.writeLock().unlock();
    }

}
