/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frm;

import java.util.concurrent.Future;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 * forwardingrules-manager
 *
 * It represent an configuration item update-contract for device.
 *
 * @author <a href="mailto:vdemcak@cisco.com">Vaclav Demcak</a>
 *
 *         Created: Aug 25, 2014
 */
public interface ForwardingRulesUpdateCommiter<D extends DataObject, U extends DataObject> extends
        AutoCloseable, DataTreeChangeListener<D> {

    /**
     * Method updates the original DataObject to the update DataObject
     * in device. Both are identified by same InstanceIdentifier
     *
     * @param identifier - the whole path to DataObject
     * @param original   - original DataObject (for update)
     * @param update     - changed DataObject (contain updates)
     * @param nodeIdent  Node InstanceIdentifier
     */
    Future<RpcResult<U>> update(InstanceIdentifier<D> identifier, D original, D update,
                                InstanceIdentifier<FlowCapableNode> nodeIdent);


}

