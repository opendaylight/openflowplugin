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
import java.util.Collections;
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
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.OfpRole;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * openflowplugin-impl
 * org.opendaylight.openflowplugin.impl.device
 * <p>
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

    private boolean lastNode;
    private final DeviceState deviceState;
    @GuardedBy("txLock")
    private WriteTransaction wTx;
    @GuardedBy("txLock")
    private BindingTransactionChain txChainFactory;
    @Deprecated
    private boolean submitIsEnabled = false;

    private volatile TransactionChainManagerStatus transactionChainManagerStatus;
    @Deprecated
    private ReadyForNewTransactionChainHandler readyForNewTransactionChainHandler;

    @Deprecated
    private volatile Registration managerRegistration;

    TransactionChainManager(@Nonnull final DataBroker dataBroker, @Nonnull final DeviceState deviceState) {
        LOG.debug("TxChainManager initialization");
        this.dataBroker = Preconditions.checkNotNull(dataBroker);
        this.deviceState = Preconditions.checkNotNull(deviceState);
        this.transactionChainManagerStatus = TransactionChainManagerStatus.SLEEPING;
        this.lastNode = false;
        LOG.trace("Initialization of txChainManager done");
    }

    /**
     * Method make initial empty node merge for new connection
     */
    private void initMergeEmptyNode() {
        LOG.trace("mergeEmptyNode for Node {}", deviceState.getNodeId());
        if (TransactionChainManagerStatus.WORKING.equals(transactionChainManagerStatus) && (!submitIsEnabled)) {
            final NodeBuilder nodeBuilder = new NodeBuilder().setId(deviceState.getNodeId()).setNodeConnector(Collections.<NodeConnector>emptyList());
            final WriteTransaction writeTx = getTransactionSafely();
            Preconditions.checkState(writeTx != null);
            writeTx.merge(LogicalDatastoreType.OPERATIONAL, deviceState.getNodeInstanceIdentifier(), nodeBuilder.build());
            LOG.debug("New node {} was added to OPERTATIONAL DataStore.", deviceState.getNodeId());
        }
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
     * @param oldRole - old role identifier
     * @param newRole - new role identifier
     */
    public void activateTransactionManager(final OfpRole oldRole, final OfpRole newRole) {
        LOG.trace("Changing txChain manager status to WORKING for Node {}", deviceState.getNodeId());
        Preconditions.checkArgument(newRole != null);
        Preconditions.checkState(OfpRole.BECOMEMASTER.equals(newRole));
        if (TransactionChainManagerStatus.WORKING.equals(transactionChainManagerStatus)) {
            LOG.debug("We are in WORKING mode for {} node. So we don't do anything");
            return;
        }
        if (!(newRole.equals(oldRole))) {
            synchronized (txLock) {
                LOG.debug("Transaction Factory create");
                Preconditions.checkState(txChainFactory == null, "TxChainFactory survive last close.");
                Preconditions.checkState(wTx == null, "We have some unexpected WriteTransaction.");
                this.transactionChainManagerStatus = TransactionChainManagerStatus.WORKING;
                this.txChainFactory = dataBroker.createTransactionChain(TransactionChainManager.this);
                initMergeEmptyNode();
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
        LOG.trace("Changing txChain manager status to SLEEPING for Node {}", deviceState.getNodeId());
        Preconditions.checkArgument(newRole != null);
        Preconditions.checkState(OfpRole.BECOMESLAVE.equals(newRole));
        if (TransactionChainManagerStatus.SLEEPING.equals(transactionChainManagerStatus)) {
            LOG.debug("We are in SLEEPING mode for {} node. So we don't do anything");
            return;
        }
        if (!(newRole.equals(oldRole)) && TransactionChainManagerStatus.WORKING.equals(transactionChainManagerStatus)) {
            synchronized (txLock) {
                if (TransactionChainManagerStatus.WORKING.equals(transactionChainManagerStatus)) {
                    LOG.debug("Submitting all transactions if we were in status WORKING");
                    // make sure that even initial data (already added to tx) will be processed in order to free tx
                    submitIsEnabled = true;
                    submitWriteTransaction();
                    Preconditions.checkState(wTx == null, "We have some unexpected WriteTransaction.");
                    LOG.debug("Transaction Factory delete");
                    transactionChainManagerStatus = TransactionChainManagerStatus.SLEEPING;
                    txChainFactory.close();
                    txChainFactory = null;
                    wTx = null;
                }
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
        LOG.trace("SubmitWriteTransaction Method for Node {}", deviceState.getNodeId());
        if (!submitIsEnabled) { // this check will not need anymore (Status has to be working before call this method)
            LOG.trace("transaction not committed - submit block issued");
            return false;
        }
        synchronized (txLock) {
            Preconditions.checkState(TransactionChainManagerStatus.WORKING.equals(transactionChainManagerStatus),
                    "we have here Uncompleted Transaction for node {} and we are not MASTER", deviceState.getNodeId());
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
                        LOG.error("Transaction for node {} commit failed. {}", deviceState.getNodeId(), t);
                    } else {
                        LOG.error("Exception during transaction submitting for node {}. {}", deviceState.getNodeId(), t);
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
        LOG.trace("addDeleteOperationToTxChain method call for Node {}", deviceState.getNodeId());
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


    @VisibleForTesting
    Boolean getLastNode() {
        return this.lastNode;
    }

    /**
     * this method provide posibbilyti to set the flag of last node, so when the
     * txChainManager is closing, it will remove node from operational and its safe to close entity
     * @param ownershipChange
     */
    public void setMarkLastNode(final EntityOwnershipChange ownershipChange) {
        LOG.trace("setMarkLastNode method call");
        Preconditions.checkArgument(ownershipChange != null);
        Preconditions.checkState((!ownershipChange.isOwner()), "Node is still master");
        Preconditions.checkState((!ownershipChange.hasOwner()), "Node has still an owner");
        this.lastNode = true;
    }


    /**
     * Method writes data identified by PATH from DS
     * @param store Operational or Configuration
     * @param path the new data identifier
     * @param data dataObject to add
     * Method for testing purpose
     * @return
     */
    <T extends DataObject> void writeToTransaction(final LogicalDatastoreType store,
                                                   final InstanceIdentifier<T> path, final T data) {
        LOG.trace("writeToTransaction method call for Node {}", deviceState.getNodeId());
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

    /**
     * Set the status of txChainManager to {@link TransactionChainManagerStatus#SHUTTING_DOWN}
     * and flush transaction if in working state and transactiona are waiting
     * if the flag last node is set, it will clear the node from operational
     */
    private void closeTransactionChainManagerSafely() {
        LOG.trace("closing transaction chain manager safely");
        if (TransactionChainManagerStatus.WORKING.equals(transactionChainManagerStatus) && wTx != null) {
            LOG.debug("We have still transaction, trying to submit");
            submitWriteTransaction();
        }
        synchronized (txLock) {
            transactionChainManagerStatus = TransactionChainManagerStatus.SHUTTING_DOWN;
            this.txChainFactory.close();
            this.txChainFactory = null;
            this.wTx = null;
            if (lastNode) {
                LOG.debug("I am the last node, removing me ({}) from operational", deviceState.getNodeId());

                //Making local variable, we don't need to care about releasing from memory
                final BindingTransactionChain localTxChainFactory = dataBroker.createTransactionChain(TransactionChainManager.this);
                final WriteTransaction tx = localTxChainFactory.newWriteOnlyTransaction();

                tx.delete(LogicalDatastoreType.OPERATIONAL, deviceState.getNodeInstanceIdentifier());
                final CheckedFuture<Void, TransactionCommitFailedException> submitsFuture = tx.submit();

                Futures.addCallback(submitsFuture, new FutureCallback<Void>() {
                    @Override
                    public void onSuccess(final Void aVoid) {
                        LOG.debug("Removing node {} from operational DS successful .", deviceState.getNodeId());
                    }

                    @Override
                    public void onFailure(final Throwable throwable) {
                        LOG.info("Attempt to close transaction chain factory failed.", throwable);
                    }
                });
            } else {
                LOG.debug("Not the last node, not touching the operational");
            }

        }

    }

    @Override
    public void onTransactionChainFailed(final TransactionChain<?, ?> chain,
                                         final AsyncTransaction<?, ?> transaction, final Throwable cause) {
        LOG.warn("Submited TxChain for node {} Failed -> recreating", deviceState.getNodeId(), cause);
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
        LOG.trace("Submited TxChain for node {} finish Successful", deviceState.getNodeId());
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
        LOG.trace("getTransactionSafely method for Node {}", deviceState.getNodeId());
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
        LOG.debug("Removing node {} from operational DS.", deviceState.getNodeId());
        synchronized (txLock) {
            final WriteTransaction writeTx;

            //TODO(Kamal): Fix this. This might cause two txChain Manager working on the same node.
            if (txChainFactory == null) {
                LOG.info("Creating new Txn Chain Factory for cleanup purposes - Race Condition Hazard, " +
                        "Concurrent Modification Hazard, node:{}", deviceState.getNodeId());
                this.txChainFactory = dataBroker.createTransactionChain(TransactionChainManager.this);
            }

            if (TransactionChainManagerStatus.SHUTTING_DOWN.equals(transactionChainManagerStatus)) {
                // status is already shutdown. so get the tx directly
                writeTx = txChainFactory.newWriteOnlyTransaction();
            } else {
                writeTx = getTransactionSafely();
            }

            this.transactionChainManagerStatus = TransactionChainManagerStatus.SHUTTING_DOWN;
            writeTx.delete(LogicalDatastoreType.OPERATIONAL, deviceState.getNodeInstanceIdentifier());
            LOG.debug("Delete node {} from operational DS put to write transaction.", deviceState.getNodeId());

            final CheckedFuture<Void, TransactionCommitFailedException> submitsFuture = writeTx.submit();
            LOG.debug("Delete node {} from operational DS write transaction submitted.", deviceState.getNodeId());

            Futures.addCallback(submitsFuture, new FutureCallback<Void>() {
                @Override
                public void onSuccess(final Void aVoid) {
                    LOG.debug("Removing node {} from operational DS successful .", deviceState.getNodeId());
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
        LOG.debug("closing txChainManager");
        closeTransactionChainManagerSafely();
        Preconditions.checkState(wTx == null);
        Preconditions.checkState(txChainFactory == null);
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
