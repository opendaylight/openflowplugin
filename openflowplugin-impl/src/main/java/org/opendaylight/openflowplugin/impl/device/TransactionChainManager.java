/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.device;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import org.opendaylight.controller.md.sal.binding.api.BindingTransactionChain;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionChain;
import org.opendaylight.controller.md.sal.common.api.data.TransactionChainListener;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
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
    private final DeviceState deviceState;
    @GuardedBy("txLock")
    private WriteTransaction wTx;
    @GuardedBy("txLock")
    private BindingTransactionChain txChainFactory;
    private boolean submitIsEnabled;

    public TransactionChainManagerStatus getTransactionChainManagerStatus() {
        return transactionChainManagerStatus;
    }

    @GuardedBy("txLock")
    private TransactionChainManagerStatus transactionChainManagerStatus;
    private final KeyedInstanceIdentifier<Node, NodeKey> nodeII;

    TransactionChainManager(@Nonnull final DataBroker dataBroker,
                            @Nonnull final DeviceState deviceState) {
        this.dataBroker = Preconditions.checkNotNull(dataBroker);
        this.deviceState = Preconditions.checkNotNull(deviceState);
        this.nodeII = Preconditions.checkNotNull(deviceState.getNodeInstanceIdentifier());
        this.transactionChainManagerStatus = TransactionChainManagerStatus.SLEEPING;
        LOG.trace("created txChainManager");
    }

    @VisibleForTesting
    @GuardedBy("txLock")
    void createTxChain() {
        if (txChainFactory != null) {
            txChainFactory.close();
        }
        txChainFactory = dataBroker.createTransactionChain(TransactionChainManager.this);
    }

    void initialSubmitWriteTransaction() {
        setSubmit(true);
        submitWriteTransaction();
    }

    /**
     * Method change status for TxChainManager to {@link TransactionChainManagerStatus#WORKING} and it has to make
     * registration for this class instance as {@link TransactionChainListener} to provide possibility a make DS
     * transactions. Call this method for MASTER role only.
     */
    public void activateTransactionManager() {
        LOG.trace("activeTransactionManager for node {} transaction submit is set to {}", deviceState.getNodeId());
        synchronized (txLock) {
            if (TransactionChainManagerStatus.SLEEPING.equals(transactionChainManagerStatus)) {
                LOG.debug("Transaction Factory create {}", deviceState.getNodeId());
                Preconditions.checkState(txChainFactory == null, "TxChainFactory survive last close.");
                Preconditions.checkState(wTx == null, "We have some unexpected WriteTransaction.");
                this.transactionChainManagerStatus = TransactionChainManagerStatus.WORKING;
                createTxChain();
            } else {
                LOG.debug("Transaction is active {}", deviceState.getNodeId());
            }
        }
    }

    /**
     * Method change status for TxChainManger to {@link TransactionChainManagerStatus#SLEEPING} and it unregisters
     * this class instance as {@link TransactionChainListener} so it broke a possibility to write something to DS.
     * Call this method for SLAVE only.
     */
    public void deactivateTransactionManager() {
        synchronized (txLock) {
            if (TransactionChainManagerStatus.WORKING.equals(transactionChainManagerStatus)) {
                LOG.debug("Submitting all transactions if we were in status WORKING for Node", deviceState.getNodeId());
                submitWriteTransaction();
                Preconditions.checkState(wTx == null, "We have some unexpected WriteTransaction.");
                LOG.debug("Transaction Factory delete for Node {}", deviceState.getNodeId());
                transactionChainManagerStatus = TransactionChainManagerStatus.SLEEPING;
                txChainFactory.close();
                txChainFactory = null;
            }
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
            Preconditions.checkState(TransactionChainManagerStatus.WORKING.equals(transactionChainManagerStatus),
                    "we have here Uncompleted Transaction for node {} and we are not MASTER", nodeII);	    
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
            LOG.debug("WriteTx is null for node {}. Delete {} was not realized.", nodeII, path);
        }
    }

    <T extends DataObject> void writeToTransaction(final LogicalDatastoreType store,
                                                   final InstanceIdentifier<T> path, final T data) {
        final WriteTransaction writeTx = getTransactionSafely();
        if (writeTx != null) {
            writeTx.put(store, path, data);
        } else {
            LOG.debug("WriteTx is null for node {}. Write data for {} was not realized.", nodeII, path);
        }
    }

    @Override
    public void onTransactionChainFailed(final TransactionChain<?, ?> chain,
                                         final AsyncTransaction<?, ?> transaction, final Throwable cause) {
        if (transactionChainManagerStatus.equals(TransactionChainManagerStatus.WORKING)) {
            LOG.warn("txChain failed -> recreating", cause);
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
        if (wTx == null && TransactionChainManagerStatus.WORKING.equals(transactionChainManagerStatus)) {
            synchronized (txLock) {
                if (wTx == null && TransactionChainManagerStatus.WORKING.equals(transactionChainManagerStatus)) {
                    if (wTx == null && txChainFactory != null) {
                        wTx = txChainFactory.newWriteOnlyTransaction();
                    }
                }
            }
        }
        return wTx;
    }

    @VisibleForTesting
    void setSubmit(Boolean value) {
        submitIsEnabled = value;
    }

    @Override
    public void close() {
        LOG.info("Setting transactionChainManagerStatus to SHUTTING_DOWN, will wait for ownership service to notify", nodeII);
        // we can finish in initial phase
        initialSubmitWriteTransaction();
        synchronized (txLock) {
            // we can finish in initial phase
            initialSubmitWriteTransaction();
            this.transactionChainManagerStatus = TransactionChainManagerStatus.SHUTTING_DOWN;
            if (txChainFactory != null) {
                txChainFactory.close();
                txChainFactory = null;
            }
            this.transactionChainManagerStatus = TransactionChainManagerStatus.SHUTTING_DOWN;
        }
        Preconditions.checkState(wTx == null);
        Preconditions.checkState(txChainFactory == null);
    }

    public enum TransactionChainManagerStatus {
        WORKING, SLEEPING, SHUTTING_DOWN;
    }



}
