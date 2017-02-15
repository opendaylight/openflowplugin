/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frm;

import java.util.concurrent.Future;
import org.opendaylight.controller.md.sal.binding.api.ClusteredDataTreeChangeListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 * forwardingrules-manager
 * org.opendaylight.openflowplugin.applications.frm
 *
 * ForwardingRulesCommiter
 * It represent a contract between DataStore DataTreeModification and relevant
 * SalRpcService for device. Every implementation has to be registered for
 * Configurational/DS tree path.
 */
public interface ForwardingRulesCommiter<D extends DataObject> extends AutoCloseable,
        ClusteredDataTreeChangeListener<D> {

    /**
     * Method removes DataObject which is identified by InstanceIdentifier
     * from device.
     *
     * @param identifier - the whole path to DataObject
     * @param del - DataObject for removing
     * @param nodeIdent Node InstanceIdentifier
     */
    void remove(InstanceIdentifier<D> identifier, D del,
            InstanceIdentifier<FlowCapableNode> nodeIdent);

    /**
     * Method updates the original DataObject to the update DataObject
     * in device. Both are identified by same InstanceIdentifier
     *
     * @param identifier - the whole path to DataObject
     * @param original - original DataObject (for update)
     * @param update - changed DataObject (contain updates)
     * @param nodeIdent Node InstanceIdentifier
     */
    void update(InstanceIdentifier<D> identifier, D original, D update,
            InstanceIdentifier<FlowCapableNode> nodeIdent);

    /**
     * Method adds the DataObject which is identified by InstanceIdentifier
     * to device.
     *
     * @param identifier - the whole path to new DataObject
     * @param add - new DataObject
     * @param nodeIdent Node InstanceIdentifier
     * @return A future associated with RPC task. {@code null} is set to the
     *         future if this method does not invoke RPC.
     */
    Future<? extends RpcResult<?>> add(InstanceIdentifier<D> identifier, D add,
            InstanceIdentifier<FlowCapableNode> nodeIdent);


    /**
     * Method creates stale-marked DataObject which is identified by InstanceIdentifier
     * from device.
     *
     * @param identifier - the whole path to DataObject
     * @param del - DataObject removed. Stale-Mark object to be created from this object
     * @param nodeIdent Node InstanceIdentifier
     */
    void createStaleMarkEntity(InstanceIdentifier<D> identifier, D del,
                InstanceIdentifier<FlowCapableNode> nodeIdent);



    Future<? extends RpcResult<?>> removeWithResult(InstanceIdentifier<D> identifier, D del,
                                               InstanceIdentifier<FlowCapableNode> nodeIdent);



}

