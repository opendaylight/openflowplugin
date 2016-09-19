/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.notification.supplier.impl.item;

import com.google.common.base.Preconditions;
import java.util.Collection;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.openflowplugin.applications.notification.supplier.NotificationSupplierForItem;
import org.opendaylight.openflowplugin.applications.notification.supplier.impl.AbstractNotificationSupplierBase;
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
abstract class AbstractNotificationSupplierForItem<O extends DataObject,
                                            C extends Notification,
                                            U extends Notification,
                                            D extends Notification>
                    extends AbstractNotificationSupplierBase<O>
                    implements NotificationSupplierForItem<O, C, U, D> {

    private final NotificationProviderService notificationProviderService;

    /**
     * Default constructor for all item Notification Supplier implementation
     *
     * @param notifProviderService - notification publisher
     * @param db - DataBroker for DataTreeChangeListener registration
     * @param clazz - Statistics Notification Class
     */
    public AbstractNotificationSupplierForItem(final NotificationProviderService notifProviderService, final DataBroker db,
            final Class<O> clazz) {
        super(db, clazz);
        this.notificationProviderService = Preconditions.checkNotNull(notifProviderService);
    }

    @Override
    public void onDataTreeChanged(Collection<DataTreeModification<O>> changes) {

        Preconditions.checkNotNull(changes, "Changes may not be null!");

        for (DataTreeModification<O> change : changes) {
            final InstanceIdentifier<O> key = change.getRootPath().getRootIdentifier();
            final DataObjectModification<O> mod = change.getRootNode();
            switch (mod.getModificationType()) {
                case DELETE:
                    remove(key, mod.getDataBefore());
                    break;
                case SUBTREE_MODIFIED:
                    update(key, mod.getDataBefore(), mod.getDataAfter());
                    break;
                case WRITE:
                    if (mod.getDataBefore() == null) {
                        add(key, mod.getDataAfter());
                    } else {
                        update(key, mod.getDataBefore(), mod.getDataAfter());
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unhandled modification type " + mod.getModificationType());
            }
        }
    }


    public void add(InstanceIdentifier<O> identifier , O add ){
        final C notif = createNotification(add, identifier);
        if (notif != null) {
            notificationProviderService.publish(notif);
        }
    }


    public void remove(InstanceIdentifier<O> identifier , O del){
        final D notif = deleteNotification(identifier.firstIdentifierOf(clazz));
        if (notif != null) {
            notificationProviderService.publish(notif);
        }
    }


    public void update(InstanceIdentifier<O> identifier , O before, O after){

        final U notif = updateNotification(after, identifier);
        if (notif != null) {
            notificationProviderService.publish(notif);
        }
    }

}
