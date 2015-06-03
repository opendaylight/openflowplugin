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
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.impl.util.DeviceStateUtil;
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
 *         <p/>
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
    private final ConnectionContext connectionContext;
    private Registration managerRegistration;

    TransactionChainManager(@Nonnull final DataBroker dataBroker,
                            @Nonnull final ConnectionContext connectionContext,
                            @Nonnull final Registration managerRegistration) {
        this.dataBroker = Preconditions.checkNotNull(dataBroker);
        this.nodeII = Preconditions.checkNotNull(DeviceStateUtil.createNodeInstanceIdentifier(connectionContext.getNodeId()));
        this.connectionContext = Preconditions.checkNotNull(connectionContext);
        this.managerRegistration = Preconditions.checkNotNull(managerRegistration);
        createTxChain(dataBroker);
        LOG.debug("created txChainManager");
    }

    private void createTxChain(final DataBroker dataBroker) {
        txChainFactory = dataBroker.createTransactionChain(TransactionChainManager.this);
        this.transactionChainManagerStatus = TransactionChainManagerStatus.WORKING;
    }

    void initialSubmitWriteTransaction() {
        enableSubmit();
        submitWriteTransaction();
    }

    public boolean attemptToRegisterHandler(final ReadyForNewTransactionChainHandler readyForNewTransactionChainHandler) {
        if (null == this.readyForNewTransactionChainHandler) {
            synchronized (this) {
                Preconditions.checkState(null != this.managerRegistration);
                this.readyForNewTransactionChainHandler = readyForNewTransactionChainHandler;
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

        if (wTx == null) {
            LOG.trace("nothing to commit - submit returns true");
            return true;
        }
        synchronized (txLock) {
            if (wTx == null) {
                LOG.trace("nothing to commit - submit returns true");
                return true;
            }
            wTx.submit();
            wTx = null;
        }
        return true;
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
        createTxChain(dataBroker);
        synchronized (txLock) {
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

    @Override
    public void close() {
        LOG.debug("Removing node {} from operational DS.", nodeII);
        synchronized (txLock) {
            final WriteTransaction writeTx = getTransactionSafely();
            this.transactionChainManagerStatus = TransactionChainManagerStatus.SHUTTING_DOWN;
            writeTx.delete(LogicalDatastoreType.OPERATIONAL, nodeII);
            LOG.debug("Delete node {} from operational DS put to write transaction.", nodeII);
            CheckedFuture<Void, TransactionCommitFailedException> submitsFuture = writeTx.submit();
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

    private void notifyReadyForNewTransactionChainAndCloseFactory() {
        synchronized (this) {
            if (null != readyForNewTransactionChainHandler) {
                readyForNewTransactionChainHandler.onReadyForNewTransactionChain(connectionContext);
            }
            try {
                LOG.debug("Closing registration in manager.");
                managerRegistration.close();
            } catch (Exception e) {
                LOG.warn("Failed to close transaction chain manager's registration.", e);
            }
            managerRegistration = null;
        }
        txChainFactory.close();
        LOG.debug("Transaction chain factory closed.");
    }

    public enum TransactionChainManagerStatus {
        WORKING, SHUTTING_DOWN;
    }

}
