/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.old.notification.supplier.impl;

import com.google.common.base.Preconditions;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker.DataChangeScope;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.applications.old.notification.supplier.OldNotifSupplierDefinition;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Public abstract basic Supplier implementation contains code for a make Supplier
 * instance, registration Supplier like {@link DataChangeLister} and close method.
 * In additional case, it contains help methods for all Supplier implementations.
 *
 * @param <O> - data tree item Object
 */
public abstract class AbstractNotifSupplierBase<O extends DataObject> implements
        OldNotifSupplierDefinition<O> {

    protected final Class<O> clazz;
    private ListenerRegistration<DataChangeListener> listenerRegistration;

    /**
     * Default constructor for all Notification Supplier implementation
     *
     * @param notifProviderService
     * @param db
     * @param clazz
     */
    public AbstractNotifSupplierBase(final DataBroker db, final Class<O> clazz) {
        Preconditions.checkArgument(db != null, "DataBroker can not be null!");
        listenerRegistration = db.registerDataChangeListener(LogicalDatastoreType.OPERATIONAL, getWildCardPath(), this,
                DataChangeScope.BASE);
        this.clazz = clazz;
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
