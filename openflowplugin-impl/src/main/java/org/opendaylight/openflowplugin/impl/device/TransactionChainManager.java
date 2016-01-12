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
import org.opendaylight.controller.md.sal.common.api.data.AsyncTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionChain;
import org.opendaylight.controller.md.sal.common.api.data.TransactionChainListener;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.OfpRole;
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
public class TransactionChainManager implements TransactionChainListener, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(TransactionChainManager.class);

    private final Object txLock = new Object();

    private final DataBroker dataBroker;
    @GuardedBy("txLock")
    private WriteTransaction wTx;
    @GuardedBy("txLock")
    private BindingTransactionChain txChainFactory;
    @Deprecated
    private boolean submitIsEnabled = true;

    private volatile TransactionChainManagerStatus transactionChainManagerStatus;
    @Deprecated
    private ReadyForNewTransactionChainHandler readyForNewTransactionChainHandler;

    private final KeyedInstanceIdentifier<Node, NodeKey> nodeII;
    @Deprecated
    private volatile Registration managerRegistration;

    TransactionChainManager(@Nonnull final DataBroker dataBroker,
            @Nonnull final KeyedInstanceIdentifier<Node, NodeKey> nodeII,
            @Nonnull final Registration managerRegistration) {
        LOG.debug("TxChainManager initialization");
        this.dataBroker = Preconditions.checkNotNull(dataBroker);
        this.nodeII = Preconditions.checkNotNull(nodeII);
        this.managerRegistration = Preconditions.checkNotNull(managerRegistration);
        this.transactionChainManagerStatus = TransactionChainManagerStatus.SLEEPING;
        LOG.trace("Initialization of txChainManager done");
    }

    /**
     * Method return actual transactionChainManagerStatus. Method is used in DeviceManager to check
     * how it could continue in postHanshake process. {@link TransactionChainManagerStatus#SLEEPING} means
     * "we are SLAVE, so we can finish registration now" {@link TransactionChainManagerStatus#WORKING} means
     * "we are MASTER, so we have to finish whole registration"
     * @return actual {@link TransactionChainManagerStatus}
     */
    TransactionChainManagerStatus getTransactionChainManagerStatus() {
        return transactionChainManagerStatus;
    }

    /**
     * Method change status for TxChainManager to {@link TransactionChainManagerStatus#WORKING} and it has to make
     * registration for this class instance as {@link TransactionChainListener} to provide possibility a make DS
     * transactions.
     * @param ownershipChange - marker to be sure it is used only for MASTER
     */
    public void activateTransactionManager(final OfpRole oldRole, final OfpRole newRole) {
        LOG.trace("Changing txChain manager status to WORKING");
        Preconditions.checkArgument(newRole != null);
        Preconditions.checkState(OfpRole.BECOMEMASTER.equals(newRole));
        if (!(newRole.equals(oldRole))) {
            synchronized (txLock) {
                LOG.debug("Transaction Factory create");
                Preconditions.checkState(txChainFactory == null, "TxChainFactory survive last close.");
                Preconditions.checkState(wTx == null, "We have some unexpected WriteTransaction.");
                this.transactionChainManagerStatus = TransactionChainManagerStatus.WORKING;
                this.txChainFactory = dataBroker.createTransactionChain(TransactionChainManager.this);
            }
        } else {
            LOG.debug("Ownership was not realy changed!");
        }
    }

    /**
     * Method change status for TxChainManger to {@link TransactionChainManagerStatus#SLEEPING} and it unregisters
     * this class instance as {@link TransactionChainListener} so it broke a possibility to write something to DS.
     * Parameters are are used as markers to be sure it is used only for SLAVE
     * @param oldRole - old role identifier
     * @param newRole - new role identifier
     */
    public void deactivateTransactionManager(final OfpRole oldRole, final OfpRole newRole) {
        LOG.trace("Changing txChain manager status to SLEEPING");
        Preconditions.checkArgument(newRole != null);
        Preconditions.checkState(OfpRole.BECOMESLAVE.equals(newRole));
        if (!(newRole.equals(oldRole))) {
            synchronized (txLock) {
                if (TransactionChainManagerStatus.WORKING.equals(transactionChainManagerStatus)) {
                    LOG.debug("Submitting all transactions if we were in status WORKING");
                    submitWriteTransaction();
                }
                Preconditions.checkState(wTx == null, "We have some unexpected WriteTransaction.");
                LOG.debug("Transaction Factory delete");
                transactionChainManagerStatus = TransactionChainManagerStatus.SLEEPING;
                txChainFactory.close();
                txChainFactory = null;
                wTx = null;
            }
        } else {
            LOG.debug("Ownership was not realy changed!");
        }
    }

    @Deprecated
    void initialSubmitWriteTransaction() {
        enableSubmit();
        submitWriteTransaction();
    }

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
     * Method submits actual {@link WriteTransaction} to DataStore if some is available
     *
     * @return information about run submit process
     */
    boolean submitWriteTransaction() {
        LOG.trace("SubmitWriteTransaction Method");
        if (!submitIsEnabled) { // this check will not need anymore (Status has to be working before call this method)
            LOG.trace("transaction not committed - submit block issued");
            return false;
        }
        synchronized (txLock) {
            Preconditions.checkState(TransactionChainManagerStatus.WORKING.equals(transactionChainManagerStatus),
                    "we have here Uncompleted Transaction for node {} and we are not MASTER", nodeII);
            if (wTx == null) {
                LOG.trace("nothing to commit - submit returns true");
                return true;
            }
            final CheckedFuture<Void, TransactionCommitFailedException> submitFuture = wTx.submit();
            Futures.addCallback(submitFuture, new FutureCallback<Void>() {
                @Override
                public void onSuccess(final Void result) {
                    //no action required
                }

                @Override
                public void onFailure(final Throwable t) {
                    if (t instanceof TransactionCommitFailedException) {
                        LOG.error("Transaction for node {} commit failed. {}", nodeII, t);
                    } else {
                        LOG.error("Exception during transaction submitting for node {}. {}", nodeII, t);
                    }
                }
            });
            wTx = null;
        }
        return true;
    }

    @Deprecated
    public void cancelWriteTransaction() {
        // there is no cancel txn in ping-pong broker. So we need to drop the chain and recreate it.
        // since the chain is created per device, there won't be any other txns other than ones we created.
        recreateTxChain();
    }

    /**
     * Method deletes object identified by PATH from DS
     * @param store Operational or Configuration
     * @param path the Deleted object identifier
     */
    <T extends DataObject> void addDeleteOperationTotTxChain(final LogicalDatastoreType store,
                                                             final InstanceIdentifier<T> path) {
        LOG.trace("addDeleteOperationToTxChain method call");
        Preconditions.checkArgument(store != null);
        Preconditions.checkArgument(path != null);
        if (TransactionChainManagerStatus.WORKING.equals(transactionChainManagerStatus)) {
            LOG.debug("Deleting {} from {} DataStore.", path, store);
            final WriteTransaction writeTx = getTransactionSafely();
            if (writeTx != null) {
                writeTx.delete(store, path);
            } else {
                LOG.info("You are not MASTER and Delete {} was not writen to {} DS", path, store);
            }
        } else {
            LOG.warn("You try to delete {} from {} DataStore, but you are not MASTER", path, store);
        }
    }

    /**
     * Method writes data identified by PATH from DS
     * @param store Operational or Configuration
     * @param path the new data identifier
     * @param data dataObject to add
     */
    <T extends DataObject> void writeToTransaction(final LogicalDatastoreType store,
                                                   final InstanceIdentifier<T> path, final T data) {
        LOG.trace("writeToTransaction method call");
        Preconditions.checkArgument(store != null);
        Preconditions.checkArgument(path != null);
        Preconditions.checkArgument(data != null);
        if (TransactionChainManagerStatus.WORKING.equals(transactionChainManagerStatus)) {
            LOG.debug("Writing data {} to {} DataStore.", path, store);
            final WriteTransaction writeTx = getTransactionSafely();
            if (writeTx != null) {
                writeTx.put(store, path, data);
            } else {
                LOG.info("You are not MASTER and Write data to {} was not writen to {} DS", path, store);
            }
        } else {
            LOG.warn("You try to write data {} to {} DataStore, but you are not MASTER", path, store);
        }
    }

    @Override
    public void onTransactionChainFailed(final TransactionChain<?, ?> chain,
                                         final AsyncTransaction<?, ?> transaction, final Throwable cause) {
        LOG.warn("Submited TxChain for node {} Failed -> recreating", nodeII, cause);
        synchronized (txLock) {
            if (TransactionChainManagerStatus.WORKING.equals(transactionChainManagerStatus)) {
                this.txChainFactory.close();
                this.txChainFactory = dataBroker.createTransactionChain(TransactionChainManager.this);
                this.wTx = null;
            }
        }
    }

    @Override
    public void onTransactionChainSuccessful(final TransactionChain<?, ?> chain) {
        LOG.trace("Submited TxChain for node {} finish Successful", nodeII);
        // NOOP - only yet, here is probably place for notification to get new WriteTransaction
    }

    @Deprecated
    private void recreateTxChain() {
        synchronized (txLock) {
            this.txChainFactory.close();
            this.txChainFactory = dataBroker.createTransactionChain(TransactionChainManager.this);
            this.wTx = null;
        }
    }

    /**
     * Method provide {@link WriteTransaction} to write or delete data to DataStore
     *
     * @return actual or new transaction
     */
    private WriteTransaction getTransactionSafely() {
        LOG.trace("getTransactionSafely method");
        if (wTx == null && TransactionChainManagerStatus.WORKING.equals(transactionChainManagerStatus)) {
            synchronized (txLock) {
                if (wTx == null && TransactionChainManagerStatus.WORKING.equals(transactionChainManagerStatus)) {
                    Preconditions.checkState(txChainFactory != null);
                    wTx = txChainFactory.newWriteOnlyTransaction();
                }
            }
        }
        return wTx;
    }

    @VisibleForTesting
    @Deprecated
    void enableSubmit() {
        submitIsEnabled = true;
    }

    /**
     * When a device disconnects from a node of the cluster, the device context gets closed. With that the txChainMgr
     * status is set to SHUTTING_DOWN and is closed.
     * When the EntityOwnershipService notifies and is derived that this was indeed the last node from which the device
     * had disconnected, then we clean the inventory.
     * Called from DeviceContext
     */
    @Deprecated
    public void cleanupPostClosure() {
        LOG.debug("Removing node {} from operational DS.", nodeII);
        synchronized (txLock) {
            final WriteTransaction writeTx;

            //TODO(Kamal): Fix this. This might cause two txChain Manager working on the same node.
            if (txChainFactory == null) {
                LOG.info("Creating new Txn Chain Factory for cleanup purposes - Race Condition Hazard, " +
                        "Concurrent Modification Hazard, node:{}", nodeII);
                this.txChainFactory = dataBroker.createTransactionChain(TransactionChainManager.this);
            }

            if (TransactionChainManagerStatus.SHUTTING_DOWN.equals(transactionChainManagerStatus)) {
                // status is already shutdown. so get the tx directly
                writeTx = txChainFactory.newWriteOnlyTransaction();
            } else {
                writeTx = getTransactionSafely();
            }

            this.transactionChainManagerStatus = TransactionChainManagerStatus.SHUTTING_DOWN;
            writeTx.delete(LogicalDatastoreType.OPERATIONAL, nodeII);
            LOG.debug("Delete node {} from operational DS put to write transaction.", nodeII);

            final CheckedFuture<Void, TransactionCommitFailedException> submitsFuture = writeTx.submit();
            LOG.debug("Delete node {} from operational DS write transaction submitted.", nodeII);

            Futures.addCallback(submitsFuture, new FutureCallback<Void>() {
                @Override
                public void onSuccess(final Void aVoid) {
                    LOG.debug("Removing node {} from operational DS successful .", nodeII);
                    notifyReadyForNewTransactionChainAndCloseFactory();
                }

                @Override
                public void onFailure(final Throwable throwable) {
                    LOG.info("Attempt to close transaction chain factory failed.", throwable);
                    notifyReadyForNewTransactionChainAndCloseFactory();
                }
            });
            wTx = null;
        }
    }

    @Deprecated
    private void notifyReadyForNewTransactionChainAndCloseFactory() {
        if(managerRegistration == null){
            LOG.warn("managerRegistration is null");
            return;
        }
        synchronized (this) {
            try {
                if (managerRegistration != null) {
                    LOG.debug("Closing registration in manager.");
                    managerRegistration.close();
                }
            } catch (final Exception e) {
                LOG.warn("Failed to close transaction chain manager's registration.", e);
            }
            managerRegistration = null;
            if (null != readyForNewTransactionChainHandler) {
                readyForNewTransactionChainHandler.onReadyForNewTransactionChain();
            }
        }
        if (txChainFactory != null) {
            txChainFactory.close();
            txChainFactory = null;
        }
        LOG.debug("Transaction chain factory closed.");
    }

    @Override
    public void close() {
        LOG.debug("closing txChainManager without cleanup of node {} from operational DS.", nodeII);
        Preconditions.checkState(wTx == null);
        Preconditions.checkState(txChainFactory == null);
        synchronized (txLock) {
            this.transactionChainManagerStatus = TransactionChainManagerStatus.SHUTTING_DOWN;
        }
        notifyReadyForNewTransactionChainAndCloseFactory(); // don't need anymore
    }

    public enum TransactionChainManagerStatus {
        /** txChainManager is sleeping - is not active (SLAVE or default init value) */
        SLEEPING,
        /** txChainManager is working - is active (MASTER) */
        WORKING,
        /** txChainManager is trying to be closed - device disconnecting */
        SHUTTING_DOWN;
    }
}
