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
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * openflowplugin-impl
 * org.opendaylight.openflowplugin.impl.device
 *
 * Package protected class for controlling {@link WriteTransaction} life cycle. It is
 * a {@link TransactionChainListener} and provide package protected methods for writeToTransaction
 * method (wrapped {@link WriteTransaction#put(LogicalDatastoreType, InstanceIdentifier, DataObject)})
 * and submitTransaction method (wrapped {@link WriteTransaction#submit()})
 *
 * @author <a href="mailto:vdemcak@cisco.com">Vaclav Demcak</a>
 *
 * Created: Apr 2, 2015
 */
@VisibleForTesting
class TransactionChainManager implements TransactionChainListener {

    private final DataBroker dataBroker;
    private final long maxTx;
    private BindingTransactionChain txChainFactory;
    private WriteTransaction wTx;
    private long nrOfActualTx;
    private boolean counterIsEnabled;

    TransactionChainManager(@Nonnull final DataBroker dataBroker, final long maxTx) {
        this.dataBroker = Preconditions.checkNotNull(dataBroker);
        this.maxTx = maxTx;
        txChainFactory = dataBroker.createTransactionChain(TransactionChainManager.this);
        nrOfActualTx = 0L;
    }

    synchronized <T extends DataObject> void writeToTransaction(final LogicalDatastoreType store,
            final InstanceIdentifier<T> path, final T data) {
        if (wTx == null) {
            wTx = txChainFactory.newWriteOnlyTransaction();
        }
        wTx.put(store, path, data, true);
        if ( ! counterIsEnabled) {
            return;
        }
        nrOfActualTx += 1L;
        if (nrOfActualTx == maxTx) {
            submitTransaction();
        }
    }

    synchronized void submitTransaction() {
        if (wTx != null) {
            wTx.submit();
            wTx = null;
            nrOfActualTx = 0L;
        }
    }

    synchronized void enableCounter() {
        counterIsEnabled = true;
    }

    @Override
    public void onTransactionChainFailed(final TransactionChain<?, ?> chain,
            final AsyncTransaction<?, ?> transaction, final Throwable cause) {
        txChainFactory.close();
        txChainFactory = dataBroker.createTransactionChain(TransactionChainManager.this);
    }

    @Override
    public void onTransactionChainSuccessful(final TransactionChain<?, ?> chain) {
        // NOOP - only yet, here is probably place for notification to get new WriteTransaction
    }
}
