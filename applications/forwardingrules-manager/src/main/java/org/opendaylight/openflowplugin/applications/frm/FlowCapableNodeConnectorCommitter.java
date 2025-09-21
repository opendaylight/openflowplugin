/*
 * Copyright (c) 2015 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frm;

import org.opendaylight.mdsal.binding.api.DataTreeChangeListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;

public interface FlowCapableNodeConnectorCommitter<D extends DataObject> extends AutoCloseable,
        DataTreeChangeListener<D> {
    /**
     * Method removes DataObject which is identified by InstanceIdentifier
     * from device.
     *
     * @param identifier - the whole path to DataObject
     * @param del - DataObject for removing
     * @param nodeConnIdent NodeConnector InstanceIdentifier
     */
    void remove(DataObjectIdentifier<D> identifier, D del,
        DataObjectIdentifier<FlowCapableNodeConnector> nodeConnIdent);

    /**
     * Method updates the original DataObject to the update DataObject
     * in device. Both are identified by same InstanceIdentifier
     *
     * @param identifier - the whole path to DataObject
     * @param original - original DataObject (for update)
     * @param update - changed DataObject (contain updates)
     * @param nodeConnIdent NodeConnector InstanceIdentifier
     */
    void update(DataObjectIdentifier<D> identifier, D original, D update,
        DataObjectIdentifier<FlowCapableNodeConnector> nodeConnIdent);

    /**
     * Method adds the DataObject which is identified by InstanceIdentifier
     * to device.
     *
     * @param identifier - the whole path to new DataObject
     * @param add - new DataObject
     * @param nodeConnIdent NodeConnector InstanceIdentifier
     */
    void add(DataObjectIdentifier<D> identifier, D add, DataObjectIdentifier<FlowCapableNodeConnector> nodeConnIdent);

}
