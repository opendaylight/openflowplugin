/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.rpc.listener;

import com.google.common.base.Function;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.rpc.listener.ItemLifecycleListener;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * General implementation of {@link ItemLifecycleListener} - keeping of DS/operational reflection up-to-date
 */
public class ItemLifecycleListenerImpl implements ItemLifecycleListener {

    private static final Integer MAX_WRITE_ATTEMPTS = 3;
    private final DeviceContext deviceContext;

    final Map<ItemToStore, Integer> unconfirmedWritingDSToCount = new HashMap<>();

    public ItemLifecycleListenerImpl(DeviceContext deviceContext) {
        this.deviceContext = deviceContext;
    }

    @Override
    public <I extends Identifiable<K> & DataObject, K extends Identifier<I>> void onAdded(final KeyedInstanceIdentifier<I, K> itemPath, final I itemBody) {
        deviceContext.writeToTransaction(LogicalDatastoreType.OPERATIONAL, itemPath, itemBody);
        final CheckedFuture<Void, TransactionCommitFailedException> futureTx = deviceContext.submitTransactionWithFuture();

        Futures.addCallback(futureTx, new FutureCallbackImpl(itemPath,itemBody, new Function<ItemToStore, Void>() {
            @Override
            public Void apply(ItemToStore itemToStore) {
                onAdded((KeyedInstanceIdentifier<I, K>) itemToStore.getItemPath(), (I) itemToStore.getItemBody());
                return null;
            }
        }));
    }

    @Override
    public <I extends Identifiable<K> & DataObject, K extends Identifier<I>> void onRemoved(KeyedInstanceIdentifier<I, K> itemPath) {
        deviceContext.addDeleteToTxChain(LogicalDatastoreType.OPERATIONAL, itemPath);
        final CheckedFuture<Void, TransactionCommitFailedException> futureTx = deviceContext.submitTransactionWithFuture();

        Futures.addCallback(futureTx, new FutureCallbackImpl(itemPath, null, new Function<ItemToStore, Void>() {
            @Override
            public Void apply(ItemToStore itemToStore) {
                onRemoved((KeyedInstanceIdentifier<I, K>) itemToStore.getItemPath());
                return null;
            }
        }));
    }

    private class FutureCallbackImpl <I extends Identifiable<K> & DataObject, K extends Identifier<I>> implements FutureCallback<Void>  {

        private final ItemToStore<I, K> itemToStore;
        private final Function<ItemToStore,Void> function;

        private FutureCallbackImpl(final KeyedInstanceIdentifier<I, K> itemPath, final I itemBody, final Function<ItemToStore,Void> function) {
            itemToStore = new ItemToStore<>(itemPath, itemBody);
            this.function = function;
        }

        @Override
        public void onSuccess(Void result) {
            unconfirmedWritingDSToCount.remove(itemToStore);
        }

        @Override
        public void onFailure(Throwable t) {
            Integer itemToStoreWritingAttempts = unconfirmedWritingDSToCount.get(itemToStore);
            itemToStoreWritingAttempts = itemToStoreWritingAttempts == null ? 1 : itemToStoreWritingAttempts++;
            if (itemToStoreWritingAttempts <= MAX_WRITE_ATTEMPTS) {
                unconfirmedWritingDSToCount.put(itemToStore, itemToStoreWritingAttempts);
                function.apply(itemToStore);
            } else {
                unconfirmedWritingDSToCount.remove(itemToStore);
            }
        }
    }

    private static class ItemToStore <I extends Identifiable<K> & DataObject, K extends Identifier<I>> {

        final KeyedInstanceIdentifier<I, K> itemPath;
        final I itemBody;

        public KeyedInstanceIdentifier<I, K> getItemPath() {
            return itemPath;
        }

        public I getItemBody() {
            return itemBody;
        }

        private ItemToStore(final KeyedInstanceIdentifier<I, K> itemPath, final I itemBody) {
            this.itemBody = itemBody;
            this.itemPath = itemPath;
        }
    }
}
