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
import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;
import org.opendaylight.controller.md.sal.binding.api.BindingTransactionChain;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipChange;
import org.opendaylight.controller.md.sal.common.api.data.AsyncTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionChain;
import org.opendaylight.controller.md.sal.common.api.data.TransactionChainListener;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
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
 *         </p>
 *         Created: Apr 2, 2015
 */
class TransactionChainManager implements TransactionChainListener, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(TransactionChainManager.class);

    private final Object txLock = new Object();

    private final DataBroker dataBroker;
    private WriteTransaction wTx;
    @GuardedBy("txLock")
    private BindingTransactionChain txChainFactory;
    private boolean submitIsEnabled;

    public TransactionChainManagerStatus getTransactionChainManagerStatus() {
        return transactionChainManagerStatus;
    }

    private volatile TransactionChainManagerStatus transactionChainManagerStatus;
    private ReadyForNewTransactionChainHandler readyForNewTransactionChainHandler;
    private final KeyedInstanceIdentifier<Node, NodeKey> nodeII;
    private volatile Registration managerRegistration;

    TransactionChainManager(@Nonnull final DataBroker dataBroker,
                            @Nonnull final KeyedInstanceIdentifier<Node, NodeKey> nodeII,
                            @Nonnull final Registration managerRegistration) {
        this.dataBroker = Preconditions.checkNotNull(dataBroker);
        this.nodeII = Preconditions.checkNotNull(nodeII);
        this.managerRegistration = Preconditions.checkNotNull(managerRegistration);
        this.transactionChainManagerStatus = TransactionChainManagerStatus.SLEEPING;
        this.wTx = null;
        LOG.debug("created txChainManager");
    }

    /**
     * Method change status for TxChainManager to {@link TransactionChainManagerStatus#WORKING} and it registrates
     * this class instance as {@link TransactionChainListener} to provide possibility a make DS transactions.
     */
    public void activateTransactionManager() {
        LOG.debug("Changing txChain manager status to WORKING");
        this.transactionChainManagerStatus = TransactionChainManagerStatus.WORKING;
        if (txChainFactory == null) {
            LOG.info("txChainFactory is null");
            createTxChain();
        }
        initialSubmitWriteTransaction();
    }

    /**
     * Method change status for TxChainManger to {@link TransactionChainManagerStatus#SLEEPING} and it unregisters
     * this class instance as {@link TransactionChainListener} so it broke a possibility to write something to DS
     */
    public void unactivateTransactionManager() {
        LOG.trace("Submitting all transactions if we were in status WORKING");
        if (transactionChainManagerStatus.equals(TransactionChainManagerStatus.WORKING)) {
            submitWriteTransaction();
        }
        LOG.debug("Changing txChain manager status to SLEEPING");
        this.transactionChainManagerStatus = TransactionChainManagerStatus.SLEEPING;
        wTx = null;

    }


    /**
     * Method has to flush actual exist transaction (if is not null) - it means close txChain safely and unregistred
     * this class instance as {@link TransactionChainListener}
     * @param ownershipChange - this object represent a restriction for unactivation functionality from OwnershipChangeListener
     * @param lastManStanding - this is definition of
     */
    public void unregistrationTransactionChainManager(final EntityOwnershipChange ownershipChange, Boolean lastManStanding) {
        Preconditions.checkNotNull(ownershipChange);
        LOG.trace("Submitting all transactions if we were in status WORKING");
        if (transactionChainManagerStatus.equals(TransactionChainManagerStatus.WORKING)) {
            submitWriteTransaction();
        }
        LOG.debug("Unregister transaction chain manager");
        transactionChainManagerStatus = TransactionChainManagerStatus.SHUTTING_DOWN;
        synchronized (txLock) {
            closeTransactionChainManager();
            if (lastManStanding) {
                LOG.debug("I am the last node, removing me ({}) from operational", nodeII);

                //Creating factory
                createTxChain();
                final WriteTransaction tx = getTransactionSafely();

                tx.delete(LogicalDatastoreType.OPERATIONAL, nodeII);
                CheckedFuture<Void, TransactionCommitFailedException> submitsFuture = tx.submit();

                Futures.addCallback(submitsFuture, new FutureCallback<Void>() {
                    @Override
                    public void onSuccess(final Void aVoid) {
                        LOG.debug("Removing node {} from operational DS successful .", nodeII);
                    }

                    @Override
                    public void onFailure(final Throwable throwable) {
                        LOG.info("Attempt to close transaction chain factory failed.", throwable);
                    }
                });
            }
        }
    }

    /**
     * @deprecated FIXME: this method has to be removed ASAP (probably in next patch in chain)
     * @param readyForNewTransactionChainHandler
     * @return
     */
    @Deprecated
    public synchronized boolean attemptToRegisterHandler(final ReadyForNewTransactionChainHandler readyForNewTransactionChainHandler) {
        if (TransactionChainManagerStatus.SHUTTING_DOWN.equals(this.transactionChainManagerStatus)
                && null == this.readyForNewTransactionChainHandler) {
            this.readyForNewTransactionChainHandler = readyForNewTransactionChainHandler;
            if (managerRegistration == null) {
                this.readyForNewTransactionChainHandler.onReadyForNewTransactionChain();
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Used from device context to write ?
     */
    public void initialSubmitWriteTransaction() {
        enableSubmit();
        submitWriteTransaction();
    }


    /**
     * Used from device context to write ?
     */
    public boolean submitWriteTransaction() {
        if (!submitIsEnabled) {
            LOG.trace("transaction not committed - submit block issued");
            return false;
        }
        synchronized (txLock) {
            if (wTx == null) {
                LOG.trace("nothing to commit - submit returns true");
                return true;
            }
            final CheckedFuture<Void, TransactionCommitFailedException> submitFuture = wTx.submit();
            Futures.addCallback(submitFuture, new FutureCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    //no action required
                }

                @Override
                public void onFailure(Throwable t) {
                    if (t instanceof TransactionCommitFailedException) {
                        LOG.error("Transaction commit failed. {}", t);
                    } else {
                        LOG.error("Exception during transaction submitting. {}", t);
                    }
                }
            });
            wTx = null;
        }
        return true;
    }


    <T extends DataObject> void addDeleteOperationTotTxChain(final LogicalDatastoreType store,
                                                             final InstanceIdentifier<T> path) {
        final WriteTransaction writeTx = getTransactionSafely();
        if (writeTx != null) {
            writeTx.delete(store, path);
        } else {
            LOG.warn("Actual Cluster Controller instance is not a master so we are not allowed to write any data to DS. {}", path);
        }
    }

    <T extends DataObject> void writeToTransaction(final LogicalDatastoreType store,
                                                   final InstanceIdentifier<T> path, final T data) {
        final WriteTransaction writeTx = getTransactionSafely();
        if (writeTx != null) {
            writeTx.put(store, path, data);
        } else {
            LOG.warn("Actual Cluster Controller instance is not a master so we are not allowed to write any data to DS. {}", path);
        }
    }

    @Override
    public void onTransactionChainFailed(final TransactionChain<?, ?> chain,
                                         final AsyncTransaction<?, ?> transaction, final Throwable cause) {
        LOG.warn("txChain failed -> recreating", cause);
        recreateTxChain();
    }

    @Override
    public void onTransactionChainSuccessful(final TransactionChain<?, ?> chain) {
        // NOOP - only yet, here is probably place for notification to get new WriteTransaction
    }

    /**
     * Method to create txChainFactory from provided dataBroker
     */
    private void createTxChain() {
        txChainFactory = dataBroker.createTransactionChain(TransactionChainManager.this);
    }

    /**
     * Close and reopen txChainFactory
     */
    private void recreateTxChain() {
        txChainFactory.close();
        synchronized (txLock) {
            createTxChain();
            wTx = null;
        }
    }

    /**
     * Return new write only transaction only in case is in state {@Link TransactionChainManagerStatus.WORKING}
     * else returning standing write transaction
     * @return
     */
    private WriteTransaction getTransactionSafely() {
        if (wTx == null && TransactionChainManagerStatus.WORKING.equals(transactionChainManagerStatus)) {
            synchronized (txLock) {
                if (wTx == null) {
                    wTx = txChainFactory.newWriteOnlyTransaction();
                }
            }
        }
        return wTx;
    }

    @VisibleForTesting
    void enableSubmit() {
        submitIsEnabled = true;
    }

    /**
     * Closing the txChainManager
     */
    @GuardedBy("txLock")
    private void closeTransactionChainManager(){

        LOG.trace("closing txChainManager");

        if (null == txChainFactory) {
            LOG.warn("txChainManager already closed for node {}", nodeII);
            return;
        }

        if(managerRegistration == null){
            LOG.warn("managerRegistration is null");
            return;
        }

        try {
            LOG.trace("Closing registration in manager.");
            managerRegistration.close();
        } catch (Exception e) {
            LOG.error("Failed to close transaction chain manager's registration.", e);
        }

        managerRegistration = null;
        if (null != readyForNewTransactionChainHandler) {
            readyForNewTransactionChainHandler.onReadyForNewTransactionChain();
        }

        txChainFactory.close();
        txChainFactory = null;
        wTx = null;

        LOG.debug("Transaction chain factory closed.");

    }

    @Override
    public void close() {
        LOG.debug("Close method invoking");
        synchronized (txLock) {
            transactionChainManagerStatus = TransactionChainManagerStatus.SHUTTING_DOWN;
            this.closeTransactionChainManager();
        }
    }

    public enum TransactionChainManagerStatus {
        //txChainManager is sleeping - is not active
        SLEEPING,
        //txChainManager is working
        WORKING,
        //txChainManager is trying to be closed
        SHUTTING_DOWN;
    }

}
