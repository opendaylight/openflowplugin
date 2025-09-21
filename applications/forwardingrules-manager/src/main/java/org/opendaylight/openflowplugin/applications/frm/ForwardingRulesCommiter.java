/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frm;

import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.mdsal.binding.api.DataTreeChangeListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 * forwardingrules-manager org.opendaylight.openflowplugin.applications.frm
 *
 * <p>ForwardingRulesCommiter It represent a contract between DataStore
 * DataTreeModification and relevant SalRpcService for device. Every
 * implementation has to be registered for Configurational/DS tree path.
 */
public interface ForwardingRulesCommiter<D extends DataObject> extends DataTreeChangeListener<D>, AutoCloseable {
    /**
     * Method removes DataObject which is identified by InstanceIdentifier from
     * device.
     *
     * @param identifier
     *            - the whole path to DataObject
     * @param del
     *            - DataObject for removing
     * @param nodeIdent
     *            Node InstanceIdentifier
     */
    void remove(DataObjectIdentifier<D> identifier, D del, DataObjectIdentifier<FlowCapableNode> nodeIdent);

    /**
     * Method updates the original DataObject to the update DataObject in device.
     * Both are identified by same InstanceIdentifier
     *
     * @param identifier the whole path to DataObject
     * @param original original DataObject (for update)
     * @param update changed DataObject (contain updates)
     * @param nodeIdent Node InstanceIdentifier
     */
    void update(DataObjectIdentifier<D> identifier, D original, D update,
        DataObjectIdentifier<FlowCapableNode> nodeIdent);

    /**
     * Method adds the DataObject which is identified by InstanceIdentifier to
     * device.
     *
     * @param identifier the whole path to new DataObject
     * @param add new DataObject
     * @param nodeIdent Node InstanceIdentifier
     * @return A future associated with RPC task. {@code null} is set to the future
     *         if this method does not invoke RPC.
     */
    ListenableFuture<? extends RpcResult<?>> add(DataObjectIdentifier<D> identifier, D add,
        DataObjectIdentifier<FlowCapableNode> nodeIdent);

    /**
     * Method creates stale-marked DataObject which is identified by InstanceIdentifier from device.
     *
     * @param identifier the whole path to DataObject
     * @param del  DataObject removed. Stale-Mark object to be created from this object
     * @param nodeIdent Node InstanceIdentifier
     */
    void createStaleMarkEntity(DataObjectIdentifier<D> identifier, D del,
        DataObjectIdentifier<FlowCapableNode> nodeIdent);

    ListenableFuture<? extends RpcResult<?>> removeWithResult(DataObjectIdentifier<D> identifier, D del,
        DataObjectIdentifier<FlowCapableNode> nodeIdent);

}
