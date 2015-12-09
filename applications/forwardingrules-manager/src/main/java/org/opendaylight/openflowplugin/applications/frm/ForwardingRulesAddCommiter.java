/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frm;

import java.util.concurrent.Future;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 * forwardingrules-manager
 *
 * It represent an configuration item add-contract for device.
 *
 * @author <a href="mailto:vdemcak@cisco.com">Vaclav Demcak</a>
 *
 *         Created: Aug 25, 2014
 */
public interface ForwardingRulesAddCommiter<D extends DataObject, A extends DataObject> {

    /**
     * Method adds the DataObject which is identified by InstanceIdentifier
     * to device.
     *
     * @param identifier - the whole path to new DataObject
     * @param add        - new DataObject
     * @param nodeIdent  Node InstanceIdentifier
     */
    Future<RpcResult<A>> add(InstanceIdentifier<D> identifier, D add, InstanceIdentifier<FlowCapableNode> nodeIdent);

}

