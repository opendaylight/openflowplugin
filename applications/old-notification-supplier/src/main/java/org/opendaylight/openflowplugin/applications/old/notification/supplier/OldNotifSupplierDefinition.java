/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.old.notification.supplier;

import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.Notification;

/**
 *
 */
public interface OldNotifSupplierDefinition<DATA_TREE_OBJECT extends DataObject,
                                            CREATE_NOTIF extends Notification,
                                            UPDATE_NOTIF extends Notification,
                                            DELETE_NOTIF extends Notification>
                                    extends DataChangeListener, AutoCloseable {

    /**
     * Method return wildCardPath for Listener registration
     * and for identify the correct KeyInstanceIdentifier from data;
     *
     * @return
     */
    InstanceIdentifier<DATA_TREE_OBJECT> getWildCardPath();

    /**
     * @param o
     * @return
     */
    CREATE_NOTIF createNotification(DATA_TREE_OBJECT o, InstanceIdentifier<DATA_TREE_OBJECT> path);

    /**
     * @param o
     * @return
     */
    UPDATE_NOTIF updateNotification(DATA_TREE_OBJECT o, InstanceIdentifier<DATA_TREE_OBJECT> path);

    /**
     * @param path
     * @return
     */
    DELETE_NOTIF deleteNotification(InstanceIdentifier<DATA_TREE_OBJECT> path);
}

