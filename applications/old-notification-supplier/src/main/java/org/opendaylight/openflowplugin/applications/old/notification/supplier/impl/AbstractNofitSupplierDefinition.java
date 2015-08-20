/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.old.notification.supplier.impl;

import com.google.common.base.Preconditions;
import java.util.Map.Entry;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker.DataChangeScope;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.openflowplugin.applications.old.notification.supplier.OldNotifSupplierDefinition;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.Notification;

/**
 * @param <DATA_TREE_OBJECT>
 * @param <CREATE_NOTIF>
 * @param <UPDATE_NOTIF>
 * @param <DELETE_NOTIF>
 */
public abstract class AbstractNofitSupplierDefinition<DATA_TREE_OBJECT extends DataObject,
                                                      CREATE_NOTIF extends Notification,
                                                      UPDATE_NOTIF extends Notification,
                                                      DELETE_NOTIF extends Notification>
                implements OldNotifSupplierDefinition<DATA_TREE_OBJECT, CREATE_NOTIF, UPDATE_NOTIF, DELETE_NOTIF> {

    private final NotificationProviderService notifProviderService;
    private final Class<DATA_TREE_OBJECT> clazz;
    private ListenerRegistration<DataChangeListener> listenerRegistration;


    /**
     * Default constructor for all Notification Supplier implementation
     *
     * @param notifProviderService
     * @param db
     * @param clazz
     */
    public AbstractNofitSupplierDefinition(final NotificationProviderService notifProviderService, final DataBroker db,
            final Class<DATA_TREE_OBJECT> clazz) {
        Preconditions.checkArgument(db != null, "DataBroker can not be null!");
        this.notifProviderService = Preconditions.checkNotNull(notifProviderService);
        listenerRegistration = db.registerDataChangeListener(LogicalDatastoreType.OPERATIONAL,
                getWildCardPath(), this, DataChangeScope.BASE);
        this.clazz = clazz;
    }

    @Override
    public void onDataChanged(final AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {
        Preconditions.checkArgument(change != null, "ChangeEvent can not be null!");
        if (change.getCreatedData() != null && ! (change.getCreatedData().isEmpty())) {
            for (final Entry<InstanceIdentifier<?>, DataObject> createDataObj : change.getCreatedData().entrySet()) {
                if (clazz.isInstance(createDataObj.getKey().getTargetType())) {
                    final InstanceIdentifier<DATA_TREE_OBJECT> ii = createDataObj.getKey().firstIdentifierOf(clazz);
                    final CREATE_NOTIF notif = createNotification(clazz.cast(createDataObj.getValue()), ii);
                    if (notif != null) {
                        notifProviderService.publish(notif);
                    }
                }
            }
        }

        if (change.getUpdatedData() != null && !(change.getUpdatedData().isEmpty())) {
            for (final Entry<InstanceIdentifier<?>, DataObject> updateDataObj : change.getUpdatedData().entrySet()) {
                if (clazz.isInstance(updateDataObj.getKey().getTargetType())) {
                    final InstanceIdentifier<DATA_TREE_OBJECT> ii = updateDataObj.getKey().firstIdentifierOf(clazz);
                    final UPDATE_NOTIF notif = updateNotification(clazz.cast(updateDataObj.getValue()), ii);
                    if (notif != null) {
                        notifProviderService.publish(notif);
                    }
                }
            }
        }

        if (change.getRemovedPaths() != null && !(change.getRemovedPaths().isEmpty())) {
            for (final InstanceIdentifier<?> deleteDataPath : change.getRemovedPaths()) {
                if (clazz.isInstance(deleteDataPath.getTargetType())) {
                    final DELETE_NOTIF notif = deleteNotification(deleteDataPath.firstIdentifierOf(clazz));
                    if (notif != null) {
                        notifProviderService.publish(notif);
                    }
                }
            }
        }
    }

    @Override
    public void close() throws Exception {
        if (listenerRegistration != null) {
            listenerRegistration.close();
            listenerRegistration = null;
        }
    }

    /**
     * Method returns a wildCard {@link InstanceIdentifier} for {@link Node} from inventory
     * because this path is a base for every OF paths.
     *
     * @return
     */
    protected static InstanceIdentifier<Node> getNodeWildII() {
        return InstanceIdentifier.create(Nodes.class).child(Node.class);
    }

    /**
     * Method returns a keyed {@link InstanceIdentifier} for {@link Node} from inventory
     * because this path is a base for every OF paths.
     *
     * @param key for keyed {@link Node} {@link InstanceIdentifier}
     * @return
     */
    protected static InstanceIdentifier<Node> getNodeWildII(final InstanceIdentifier<?> ii) {
        final NodeKey key = ii.firstKeyOf(Node.class, NodeKey.class);
        Preconditions.checkArgument(key != null);
        return InstanceIdentifier.create(Nodes.class).child(Node.class, key);
    }
}

