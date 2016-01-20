/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
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
 * Notifications for Statistics have little bit different case,
 * because it looks like they have response for create and update.
 * But follow the statistics internal implementation processing
 * is talks only about create event.
 *
 * @param <O> - data tree item Object
 * @param <N> - Statistics Notification
 */
public interface NotifSupplierForItemStat<O extends DataObject, N extends Notification>
        extends NotifSupplierDefinition<O> {


    /**
     * Method produces relevant Statistics kind of {@link Notification} from statistics
     * data tree item identified by {@link InstanceIdentifier} path.
     *
     * @param o    - Statistics Data Tree Item
     * @param path - Identifier of Data Tree Item
     * @return {@link Notification} - relevant API contract Notification
     */
    N createNotification(O o, InstanceIdentifier<O> path);
}

