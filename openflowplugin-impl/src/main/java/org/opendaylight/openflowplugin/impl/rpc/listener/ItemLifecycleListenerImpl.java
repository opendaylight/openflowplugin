/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.rpc.listener;

import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.rpc.listener.ItemLifecycleListener;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

/**
 * General implementation of {@link ItemLifecycleListener} - keeping of DS/operational reflection up-to-date
 */
public class ItemLifecycleListenerImpl implements ItemLifecycleListener {

    private final DeviceContext deviceContext;

    public ItemLifecycleListenerImpl(DeviceContext deviceContext) {
        this.deviceContext = deviceContext;
    }

    @Override
    public <I extends Identifiable<K> & DataObject, K extends Identifier<I>> void onAdded(KeyedInstanceIdentifier<I, K> itemPath, I itemBody) {
        deviceContext.writeToTransaction(LogicalDatastoreType.OPERATIONAL, itemPath, itemBody);
        deviceContext.submitTransaction();
    }

    @Override
    public <I extends Identifiable<K> & DataObject, K extends Identifier<I>> void onRemoved(KeyedInstanceIdentifier<I, K> itemPath) {
        deviceContext.addDeleteToTxChain(LogicalDatastoreType.OPERATIONAL, itemPath);
        deviceContext.submitTransaction();
    }
}
