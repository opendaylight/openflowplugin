/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync;

import java.util.concurrent.Future;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 * Represents a configuration item update-contract for device.
 */
public interface ForwardingRulesUpdateCommitter<D extends DataObject, U extends DataObject> {

    /**
     * Method updates the original DataObject to the update DataObject
     * in device. Both are identified by same InstanceIdentifier
     *
     * @param identifier - the whole path to DataObject
     * @param original   - original DataObject (for update)
     * @param update     - changed DataObject (contain updates)
     * @param nodeIdent  - Node InstanceIdentifier
     * @return RpcResult of action
     */
    Future<RpcResult<U>> update(InstanceIdentifier<D> identifier, D original, D update,
                                InstanceIdentifier<FlowCapableNode> nodeIdent);


}
