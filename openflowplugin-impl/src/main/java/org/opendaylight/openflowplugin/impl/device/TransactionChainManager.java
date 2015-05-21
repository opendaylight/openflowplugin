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
import javax.annotation.Nonnull;
import org.opendaylight.controller.md.sal.binding.api.BindingTransactionChain;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionChain;
import org.opendaylight.controller.md.sal.common.api.data.TransactionChainListener;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
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
class TransactionChainManager implements TransactionChainListener, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(TransactionChainManager.class);

    private final Object txLock = new Object();

    private final DeviceState deviceState;
    private final DataBroker dataBroker;
    private WriteTransaction wTx;
    private BindingTransactionChain txChainFactory;
    private boolean submitIsEnabled;

    TransactionChainManager(@Nonnull final DataBroker dataBroker, @Nonnull final DeviceState deviceState) {
        this.dataBroker = Preconditions.checkNotNull(dataBroker);
        this.deviceState = Preconditions.checkNotNull(deviceState);
        txChainFactory = dataBroker.createTransactionChain(TransactionChainManager.this);
        LOG.debug("created txChainManager");
    }

    void initialSubmitWriteTransaction() {
        enableSubmit();
        submitWriteTransaction();
    }

    boolean submitWriteTransaction() {
        if ( ! submitIsEnabled) {
            LOG.trace("transaction not committed - submit block issued");
            return false;
        }
        if (wTx == null) {
            LOG.trace("nothing to commit - submit returns true");
            return true;
        }
        if ( ! deviceState.isValid()) {
            LOG.info("DeviceState is not valid will not submit transaction");
            return false;
        }
        synchronized (txLock) {
            wTx.submit();
            wTx = null;
        }
        return true;
    }

    <T extends DataObject> void addDeleteOperationTotTxChain(final LogicalDatastoreType store,
            final InstanceIdentifier<T> path) {
        try {
            final WriteTransaction writeTx = getTransactionSafely();
            writeTx.delete(store, path);
        } catch (final Exception e) {
            LOG.warn("failed to put into writeOnlyTransaction : {}", e.getMessage());
            LOG.trace("failed to put into writeOnlyTransaction.. ", e);
        }
    }

    <T extends DataObject> void writeToTransaction(final LogicalDatastoreType store,
            final InstanceIdentifier<T> path, final T data) {
        try {
            final WriteTransaction writeTx = getTransactionSafely();
            writeTx.put(store, path, data);
        } catch (final Exception e) {
            LOG.warn("failed to put into writeOnlyTransaction: {}", e.getMessage());
            LOG.trace("failed to put into writeOnlyTransaction.. ", e);
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

    private void recreateTxChain() {
        txChainFactory.close();
        txChainFactory = dataBroker.createTransactionChain(TransactionChainManager.this);
        synchronized (txLock) {
            wTx = null;
        }
    }

    private WriteTransaction getTransactionSafely() {
        if (wTx == null) {
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
    public void close() throws Exception {
        LOG.debug("Removing node {} from operational DS.", deviceState.getNodeId());
        synchronized (txLock) {
            final WriteTransaction writeTx = getTransactionSafely();
            writeTx.delete(LogicalDatastoreType.OPERATIONAL, deviceState.getNodeInstanceIdentifier());
            writeTx.submit();
            wTx = null;
            txChainFactory.close();
        }
    }
}
