/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.notification.supplier.impl.item.stat;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataObjectModification;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.openflowplugin.applications.notification.supplier.NotificationSupplierForItemStat;
import org.opendaylight.openflowplugin.applications.notification.supplier.impl.AbstractNotificationSupplierBase;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.Notification;

/**
 * Class is package protected abstract implementation for all Old Statistics
 * Notification Suppliers.
 *
 * @param <O> - Statistics {@link DataObject}
 * @param <N> - Statistics Notification
 */
public abstract class AbstractNotificationSupplierForItemStat<O extends DataObject, N extends Notification> extends
        AbstractNotificationSupplierBase<O> implements NotificationSupplierForItemStat<O, N> {

    private final NotificationPublishService notifProviderService;

    /**
     * Default constructor for all Statistic Notification Supplier implementation.
     *
     * @param notifProviderService - notification publisher
     * @param db                   - DataBroker for DataTreeChangeListener registration
     * @param clazz                - Statistics Notification Class
     */
    public AbstractNotificationSupplierForItemStat(final NotificationPublishService notifProviderService,
                                                   final DataBroker db, final Class<O> clazz) {
        super(db, clazz);
        this.notifProviderService = requireNonNull(notifProviderService);
    }

    @Override
    public void onDataTreeChanged(final Collection<DataTreeModification<O>> changes) {
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


    public void add(final InstanceIdentifier<O> identifier, final O add) {
        final N notif = createNotification(add, identifier);
        if (notif != null) {
            try {
                notifProviderService.putNotification(notif);
            } catch (InterruptedException e) {
                throw new IllegalStateException("Interrupted while publishing " + notif, e);
            }
        }
    }

    public void remove(final InstanceIdentifier<O> identifier, final O del) {
        //EMPTY NO-OP
    }

    public void update(final InstanceIdentifier<O> identifier, final O before, final O after) {
        //EMPTY NO-OP
    }
}

