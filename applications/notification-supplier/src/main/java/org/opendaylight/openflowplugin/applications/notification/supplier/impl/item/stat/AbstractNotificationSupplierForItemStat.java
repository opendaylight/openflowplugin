/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.notification.supplier.impl.item.stat;

import com.google.common.base.Preconditions;
import java.util.Map.Entry;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.openflowplugin.applications.notification.supplier.NotificationSupplierForItemStat;
import org.opendaylight.openflowplugin.applications.notification.supplier.impl.AbstractNotificationSupplierBase;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.Notification;

/**
 * Class is package protected abstract implementation for all Old Statistics
 * Notification Suppliers
 *
 * @param <O> - Statistics {@link DataObject}
 * @param <N> - Statistics Notification
 */
abstract class AbstractNotificationSupplierForItemStat<O extends DataObject,
                                                N extends Notification>
                    extends AbstractNotificationSupplierBase<O>
                    implements NotificationSupplierForItemStat<O, N> {

    private final NotificationProviderService notifProviderService;

    /**
     * Default constructor for all Statistic Notification Supplier implementation
     *
     * @param notifProviderService - notification publisher
     * @param db - DataBroker for DataChangeEvent registration
     * @param clazz - Statistics Notification Class
     */
    public AbstractNotificationSupplierForItemStat(final NotificationProviderService notifProviderService,
            final DataBroker db, final Class<O> clazz) {
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
                    final N notif = createNotification((O) createDataObj.getValue(), ii);
                    if (notif != null) {
                        notifProviderService.publish(notif);
                    }
                }
            }
        }
    }
}

