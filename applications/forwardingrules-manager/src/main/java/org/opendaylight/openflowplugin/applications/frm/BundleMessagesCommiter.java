/*
 * Copyright (c) 2019 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frm;

import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.AddBundleMessagesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.BundleId;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;

public interface BundleMessagesCommiter<D extends DataObject> {

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
    void remove(InstanceIdentifier<D> identifier, D del, InstanceIdentifier<FlowCapableNode> nodeIdent,
            BundleId bundleId);

    /**
     * Method updates the original DataObject to the update DataObject in device.ForwardingRulesManager.java.
     * Both are identified by same InstanceIdentifier.
     *
     * @param identifier
     *            - the whole path to DataObject
     * @param original
     *            - original DataObject (for update)
     * @param update
     *            - changed DataObject (contain updates)
     * @param nodeIdent
     *            Node InstanceIdentifier
     */
    void update(InstanceIdentifier<D> identifier, D original, D update,
            InstanceIdentifier<FlowCapableNode> nodeIdent, BundleId bundleId);

    /**
     * Method adds the DataObject which is identified by InstanceIdentifier to
     * device.
     *
     * @param identifier
     *            - the whole path to new DataObject
     * @param add
     *            - new DataObject
     * @param nodeIdent
     *            Node InstanceIdentifier
     * @return A future associated with RPC task. {@code null} is set to the future
     *         if this method does not invoke RPC.
     */
    ListenableFuture<RpcResult<AddBundleMessagesOutput>> add(InstanceIdentifier<D> identifier, D add,
                                                     InstanceIdentifier<FlowCapableNode> nodeIdent, BundleId bundleId);

}
