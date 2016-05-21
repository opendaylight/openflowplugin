/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.notification.supplier;

import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.Notification;

/**
 * Supplier Item contracts definition for every Notification. All items are described
 * by three notifications. Notification for Create, Update and Delete. So interface
 * has to contain three methods for every Notification.
 *
 * @param <O> - data tree item Object
 * @param <C> - Create notification
 * @param <U> - Update notification
 * @param <D> - Delete notification
 */
public interface NotificationSupplierForItem<O extends DataObject,
                                         C extends Notification,
                                         U extends Notification,
                                         D extends Notification>
        extends NotificationSupplierDefinition<O> {

    /**
     * Method produces relevant addItem kind of {@link Notification} from
     * data tree item identified by {@link InstanceIdentifier} path.
     *
     * @param o - Data Tree Item object
     * @param path - Identifier of Data Tree Item
     * @return {@link Notification} - relevant API contract Notification
     */
     C createNotification(O o, InstanceIdentifier<O> path);

    /**
     * Method produces relevant updateItem kind of {@link Notification} from
     * data tree item identified by {@link InstanceIdentifier} path.
     *
     * @param o - Data Tree Item object
     * @param path - Identifier of Data Tree Item
     * @return {@link Notification} - relevant API contract Notification
     */
    U updateNotification(O o, InstanceIdentifier<O> path);

    /**
     * Method produces relevant deleteItem kind of {@link Notification} from
     * path {@link InstanceIdentifier} to deleted item.
     *
     * @param path - Identifier of Data Tree Item
     * @return {@link Notification} - relevant API contract Notification
     */
     D deleteNotification(InstanceIdentifier<O> path);
}

