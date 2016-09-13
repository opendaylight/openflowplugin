/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.notification.supplier;

import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Default definition for every Notification Supplier. Migration from notification
 * to {@link org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener} has one
 * keyed component - WildCarded Path which represent a changes checker in DataStoreTreeNode
 *
 * @param <O> - {@link DataObject} represent Data Tree Item from DataStore
 */
public interface NotificationSupplierDefinition<O extends DataObject> extends AutoCloseable, DataTreeChangeListener<O> {

    /**
     * Method return wildCardPath for Listener registration and for identify
     * the correct KeyInstanceIdentifier from Data Tree Item in DataStore;
     *
     * @return {@link InstanceIdentifier}
     */
    InstanceIdentifier<O> getWildCardPath();

}

