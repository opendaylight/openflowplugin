/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.notification.supplier.impl;

import com.google.common.base.Preconditions;
import java.util.concurrent.Callable;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.applications.notification.supplier.NotificationSupplierDefinition;
import org.opendaylight.openflowplugin.common.wait.SimpleTaskRetryLooper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Public abstract basic Supplier implementation contains code for a make Supplier instance,
 * registration Supplier like {@link org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener}
 * and close method. In additional case, it contains help methods for all Supplier implementations.
 *
 * @param <O> - data tree item Object extends {@link DataObject}
 */
public abstract class AbstractNotificationSupplierBase<O extends DataObject> implements
        NotificationSupplierDefinition<O> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractNotificationSupplierBase.class);

    protected final Class<O> clazz;
    private ListenerRegistration<DataTreeChangeListener<O>> listenerRegistration;
    private static final int STARTUP_LOOP_TICK = 500;
    private static final int STARTUP_LOOP_MAX_RETRIES = 8;


    final DataTreeIdentifier<O> treeId =
            new DataTreeIdentifier<>(LogicalDatastoreType.OPERATIONAL, getWildCardPath());
    /**
     * Default constructor for all Notification Supplier implementation
     *
     * @param db    - {@link DataBroker}
     * @param clazz - API contract class extended {@link DataObject}
     */
    public AbstractNotificationSupplierBase(final DataBroker db, final Class<O> clazz) {
        Preconditions.checkArgument(db != null, "DataBroker can not be null!");
        this.clazz = clazz;

        SimpleTaskRetryLooper looper = new SimpleTaskRetryLooper(STARTUP_LOOP_TICK, STARTUP_LOOP_MAX_RETRIES);
        try {
            listenerRegistration =  looper.loopUntilNoException(new Callable<ListenerRegistration<DataTreeChangeListener<O>>>() {
                @Override
                public ListenerRegistration<DataTreeChangeListener<O>> call() throws Exception {
                    return db.registerDataTreeChangeListener(treeId, AbstractNotificationSupplierBase.this);
                }
            });
        }catch(final Exception ex){
            LOG.debug("AbstractNotificationSupplierBase DataTreeChange listener registration fail ..{}", ex.getMessage());
            throw new IllegalStateException("Notification supplier startup fail! System needs restart.", ex);
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
     * @return WildCarded InstanceIdentifier for Node
     */
    protected static InstanceIdentifier<Node> getNodeWildII() {
        return InstanceIdentifier.create(Nodes.class).child(Node.class);
    }

    /**
     * Method returns a keyed {@link InstanceIdentifier} for {@link Node} from inventory
     * because this path is a base for every OF paths.
     *
     * @param ii - key for keyed {@link Node} {@link InstanceIdentifier}
     * @return Keyed InstanceIdentifier for Node
     */
    protected static KeyedInstanceIdentifier<Node, NodeKey> getNodeII(final InstanceIdentifier<?> ii) {
        final NodeKey key = ii.firstKeyOf(Node.class, NodeKey.class);
        Preconditions.checkArgument(key != null);
        return InstanceIdentifier.create(Nodes.class).child(Node.class, key);
    }

    /**
     * @param path pointer to element
     * @return extracted {@link NodeKey} and wrapped in {@link NodeRef}
     */
    public static NodeRef createNodeRef(InstanceIdentifier<?> path) {
        final InstanceIdentifier<Node> nodePath = Preconditions.checkNotNull(path.firstIdentifierOf(Node.class));
        return new NodeRef(nodePath);
    }

    /**
     * @param path pointer to element
     * @return extracted {@link NodeId}
     */
    public static NodeId getNodeId(InstanceIdentifier<?> path) {
        final NodeKey nodeKey = Preconditions.checkNotNull(path.firstKeyOf(Node.class, NodeKey.class));
        return nodeKey.getId();
    }

}
