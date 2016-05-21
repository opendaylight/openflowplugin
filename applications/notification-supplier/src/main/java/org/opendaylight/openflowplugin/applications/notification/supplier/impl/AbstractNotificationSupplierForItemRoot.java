/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.notification.supplier.impl;

import com.google.common.base.Preconditions;
import java.util.Map.Entry;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.openflowplugin.applications.notification.supplier.NotificationSupplierForItemRoot;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.Notification;

/**
 * Class is package protected abstract implementation for all Root Items
 * Notification Suppliers
 *
 * @param <O> - data tree item Object
 * @param <C> - Create notification
 * @param <D> - Delete notification
 */
abstract class AbstractNotificationSupplierForItemRoot<O extends DataObject,
                                               C extends Notification,
                                               D extends Notification>
                    extends AbstractNotificationSupplierBase<O>
                    implements NotificationSupplierForItemRoot<O, C, D> {

    private final NotificationProviderService notificationProviderService;

    /**
     * Default constructor for all Root Item Notification Supplier implementation
     *
     * @param notificationProviderService - notification publisher
     * @param db - DataBroker for DataChangeEvent registration
     * @param clazz - Statistics Notification Class
     */
    public AbstractNotificationSupplierForItemRoot(final NotificationProviderService notificationProviderService, final DataBroker db,
            final Class<O> clazz) {
        super(db, clazz);
        this.notificationProviderService = Preconditions.checkNotNull(notificationProviderService);
    }

    @Override
    public void onDataChanged(final AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {
        Preconditions.checkArgument(change != null, "ChangeEvent can not be null!");
        if (change.getCreatedData() != null && ! (change.getCreatedData().isEmpty())) {
            for (final Entry<InstanceIdentifier<?>, DataObject> createDataObj : change.getCreatedData().entrySet()) {
                if (clazz.isAssignableFrom(createDataObj.getKey().getTargetType())) {
                    final InstanceIdentifier<O> ii = createDataObj.getKey().firstIdentifierOf(clazz);
                    final C notif = createNotification((O) createDataObj.getValue(), ii);
                    if (notif != null) {
                        notificationProviderService.publish(notif);
                    }
                }
            }
        }

        if (change.getRemovedPaths() != null && !(change.getRemovedPaths().isEmpty())) {
            for (final InstanceIdentifier<?> deleteDataPath : change.getRemovedPaths()) {
                if (clazz.isAssignableFrom(deleteDataPath.getTargetType())) {
                    final D notif = deleteNotification(deleteDataPath.firstIdentifierOf(clazz));
                    if (notif != null) {
                        notificationProviderService.publish(notif);
                    }
                }
            }
        }
    }
}

