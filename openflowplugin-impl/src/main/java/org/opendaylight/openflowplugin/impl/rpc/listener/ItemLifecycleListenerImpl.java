/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.rpc.listener;

import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.openflow.device.TxFacade;
import org.opendaylight.openflowplugin.api.openflow.rpc.listener.ItemLifecycleListener;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * General implementation of {@link ItemLifecycleListener} - keeping of DS/operational reflection up-to-date.
 */
public class ItemLifecycleListenerImpl implements ItemLifecycleListener {

    private static final Logger LOG = LoggerFactory.getLogger(ItemLifecycleListenerImpl.class);
    private static final String NOT_ABLE_TO_WRITE_TO_TRANSACTION = "Not able to write to transaction: ";

    private final TxFacade txFacade;

    public ItemLifecycleListenerImpl(final TxFacade txFacade) {
        this.txFacade = txFacade;
    }

    @Override
    @SuppressWarnings("checkstyle:IllegalCatch")
    public <I extends Identifiable<K> & DataObject, K extends Identifier<I>>
            void onAdded(KeyedInstanceIdentifier<I, K> itemPath, I itemBody) {
        try {
            txFacade.writeToTransaction(LogicalDatastoreType.OPERATIONAL, itemPath, itemBody);
            txFacade.submitTransaction();
        } catch (Exception e) {
            LOG.warn(NOT_ABLE_TO_WRITE_TO_TRANSACTION, e);
        }
    }

    @Override
    @SuppressWarnings("checkstyle:IllegalCatch")
    public <I extends Identifiable<K> & DataObject, K extends Identifier<I>>
            void onRemoved(KeyedInstanceIdentifier<I, K> itemPath) {
        try {
            txFacade.addDeleteToTxChain(LogicalDatastoreType.OPERATIONAL, itemPath);
            txFacade.submitTransaction();
        } catch (Exception e) {
            LOG.warn(NOT_ABLE_TO_WRITE_TO_TRANSACTION, e);
        }
    }

    @Override
    @SuppressWarnings("checkstyle:IllegalCatch")
    public <I extends Identifiable<K> & DataObject, K extends Identifier<I>>
            void onUpdated(KeyedInstanceIdentifier<I, K> itemPath, I itemBody) {
        try {
            txFacade.addDeleteToTxChain(LogicalDatastoreType.OPERATIONAL, itemPath);
            txFacade.writeToTransaction(LogicalDatastoreType.OPERATIONAL, itemPath, itemBody);
            txFacade.submitTransaction();
        } catch (Exception e) {
            LOG.warn(NOT_ABLE_TO_WRITE_TO_TRANSACTION, e);
        }
    }
}
