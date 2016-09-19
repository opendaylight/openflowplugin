/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.notification.supplier.impl.item.stat;

import com.google.common.base.Preconditions;
import java.util.Collection;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
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
     * @param db - DataBroker for DataTreeChangeListener registration
     * @param clazz - Statistics Notification Class
     */
    public AbstractNotificationSupplierForItemStat(final NotificationProviderService notifProviderService,
            final DataBroker db, final Class<O> clazz) {
        super(db, clazz);
        this.notifProviderService = Preconditions.checkNotNull(notifProviderService);
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
        final N notif = createNotification(add, identifier);
        if (notif != null) {
            notifProviderService.publish(notif);
        }
    }


    public void remove(InstanceIdentifier<O> identifier , O del){
        //EMPTY NO-OP
    }


    public void update(InstanceIdentifier<O> identifier , O before, O after){
        //EMPTY NO-OP
    }

}

