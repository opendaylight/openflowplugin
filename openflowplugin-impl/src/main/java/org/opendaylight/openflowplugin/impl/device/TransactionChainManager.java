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
import org.opendaylight.controller.md.sal.binding.api.BindingTransactionChain;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
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
    private BindingTransactionChain txChainFactory;
    private boolean submitIsEnabled;

    public TransactionChainManagerStatus getTransactionChainManagerStatus() {
        return transactionChainManagerStatus;
    }

    private TransactionChainManagerStatus transactionChainManagerStatus;
    private ReadyForNewTransactionChainHandler readyForNewTransactionChainHandler;
    private final KeyedInstanceIdentifier<Node, NodeKey> nodeII;
    private volatile Registration managerRegistration;

    TransactionChainManager(@Nonnull final DataBroker dataBroker,
                            @Nonnull final KeyedInstanceIdentifier<Node, NodeKey> nodeII,
                            @Nonnull final Registration managerRegistration) {
        this.dataBroker = Preconditions.checkNotNull(dataBroker);
        this.nodeII = Preconditions.checkNotNull(nodeII);
        this.managerRegistration = Preconditions.checkNotNull(managerRegistration);
        this.transactionChainManagerStatus = TransactionChainManagerStatus.WORKING;
        createTxChain(dataBroker);
        LOG.debug("created txChainManager");
    }

    private void createTxChain(final DataBroker dataBroker) {
        txChainFactory = dataBroker.createTransactionChain(TransactionChainManager.this);
    }

    void initialSubmitWriteTransaction() {
        enableSubmit();
        submitWriteTransaction();
    }

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

    boolean submitWriteTransaction() {
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

    public void cancelWriteTransaction() {
        // there is no cancel txn in ping-pong broker. So we need to drop the chain and recreate it.
        // since the chain is created per device, there won't be any other txns other than ones we created.
        recreateTxChain();
    }

    <T extends DataObject> void addDeleteOperationTotTxChain(final LogicalDatastoreType store,
                                                             final InstanceIdentifier<T> path) {
        final WriteTransaction writeTx = getTransactionSafely();
        writeTx.delete(store, path);
    }

    <T extends DataObject> void writeToTransaction(final LogicalDatastoreType store,
                                                   final InstanceIdentifier<T> path, final T data) {
        final WriteTransaction writeTx = getTransactionSafely();
        writeTx.put(store, path, data);
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

    private void recreateTxChain() {
        txChainFactory.close();
        synchronized (txLock) {
            createTxChain(dataBroker);
            wTx = null;
        }
    }


    private WriteTransaction getTransactionSafely() {
        if (wTx == null && !TransactionChainManagerStatus.SHUTTING_DOWN.equals(transactionChainManagerStatus)) {
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
     * Closing the txChainManager without cleaning the operational
     */
    private void closeTransactionChainManager(){
        LOG.info("closing txChainManager");

        synchronized (this) {

            this.transactionChainManagerStatus = TransactionChainManagerStatus.SHUTTING_DOWN;

            if (null == txChainFactory) {
                LOG.warn("txChainManager already closed for node {}", nodeII);
                return;
            }

            if(managerRegistration == null){
                LOG.warn("managerRegistration is null");
                return;
            }

            try {
                LOG.debug("Closing registration in manager.");
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

        }
        LOG.debug("Transaction chain factory closed.");

    }

    /**
     * Remove node from operational
     * First we close the chain manager so it can be recreated
     */
    public void removeNodeFromOperationalAndClose(){
        LOG.info("removing node from operational");

        synchronized (txLock){
            //Close the transaction chain manager
            closeTransactionChainManager();

            //Can this be ?
            if (txChainFactory != null) {
                LOG.warn("transaction chain factory is not null");
                recreateTxChain();
            }

            createTxChain(dataBroker);

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

    @Override
    public void close() {
        this.closeTransactionChainManager();
    }

    public enum TransactionChainManagerStatus {
        //txChainManager is working
        WORKING,
        //txChainManager is trying to be closed
        SHUTTING_DOWN;
    }

}
