/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.old.notification.supplier.impl.item;

import com.google.common.base.Preconditions;
import java.util.Map.Entry;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.openflowplugin.applications.old.notification.supplier.OldNotifSupplierForItem;
import org.opendaylight.openflowplugin.applications.old.notification.supplier.impl.AbstractNotifSupplierBase;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.Notification;

/**
 * Class is package protected abstract implementation for all Old Root Items
 * Notification Suppliers
 *
 * @param <O> - data tree item Object
 * @param <C> - Create notification
 * @param <U> - Update notification
 * @param <D> - Delete notification
 */
abstract class AbstractNotifSupplierForItem<O extends DataObject,
                                            C extends Notification,
                                            U extends Notification,
                                            D extends Notification>
                    extends AbstractNotifSupplierBase<O>
                    implements OldNotifSupplierForItem<O, C, U, D> {

    private final NotificationProviderService notifProviderService;

    /**
     * Default constructor for all item Notification Supplier implementation
     *
     * @param notifProviderService - notification publisher
     * @param db - DataBroker for DataChangeEvent registration
     * @param clazz - Statistics Notification Class
     */
    public AbstractNotifSupplierForItem(final NotificationProviderService notifProviderService, final DataBroker db,
            final Class<O> clazz) {
        super(db, clazz);
        this.notifProviderService = Preconditions.checkNotNull(notifProviderService);
    }

    @Override
    public void onDataChanged(final AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {
        Preconditions.checkArgument(change != null, "ChangeEvent can not be null!");
        if (change.getCreatedData() != null && !(change.getCreatedData().isEmpty())) {
            for (final Entry<InstanceIdentifier<?>, DataObject> createDataObj : change.getCreatedData().entrySet()) {
                if (clazz.isAssignableFrom(createDataObj.getKey().getTargetType())) {
                    final InstanceIdentifier<O> ii = createDataObj.getKey().firstIdentifierOf(clazz);
                    final C notif = createNotification((O) createDataObj.getValue(), ii);
                    if (notif != null) {
                        notifProviderService.publish(notif);
                    }
                }
            }
        }

        if (change.getUpdatedData() != null && !(change.getUpdatedData().isEmpty())) {
            for (final Entry<InstanceIdentifier<?>, DataObject> updateDataObj : change.getUpdatedData().entrySet()) {
                if (clazz.isAssignableFrom(updateDataObj.getKey().getTargetType())) {
                    final InstanceIdentifier<O> ii = updateDataObj.getKey().firstIdentifierOf(clazz);
                    final U notif = updateNotification((O) updateDataObj.getValue(), ii);
                    if (notif != null) {
                        notifProviderService.publish(notif);
                    }
                }
            }
        }

        if (change.getRemovedPaths() != null && !(change.getRemovedPaths().isEmpty())) {
            for (final InstanceIdentifier<?> deleteDataPath : change.getRemovedPaths()) {
                if (clazz.isAssignableFrom(deleteDataPath.getTargetType())) {
                    final D notif = deleteNotification(deleteDataPath.firstIdentifierOf(clazz));
                    if (notif != null) {
                        notifProviderService.publish(notif);
                    }
                }
            }
        }
    }
}
