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
 * Represents a configuration item remove-contract for device.
 */
public interface ForwardingRulesRemoveCommitter<D extends DataObject, R extends DataObject> {

    /**
     * Method removes DataObject which is identified by InstanceIdentifier
     * from device.
     *
     * @param identifier - the whole path to DataObject
     * @param del        - DataObject for removing
     * @param nodeIdent  - Node InstanceIdentifier
     */
    Future<RpcResult<R>> remove(InstanceIdentifier<D> identifier, D del, InstanceIdentifier<FlowCapableNode> nodeIdent);

}
