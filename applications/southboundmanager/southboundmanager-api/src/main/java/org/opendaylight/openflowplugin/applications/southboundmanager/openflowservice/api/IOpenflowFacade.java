/*
 * Copyright (c) 2016 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.southboundmanager.openflowservice.api;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;

import java.util.List;
import java.util.TreeMap;

/**
 * The Facade is suppose to provide a thin abstraction over the Openflowplugin RPC.
 * Clients can be agnostic to whether the device is connected to their instance
 * of the controller or whether their instance is the owner of the that device.
 * The facade underneath maintains device ownership state and prevents remote RPC calls.
 *
 * Currently isDirectRPC=false is unsupported. This will be improved upon in the future versions.
 */
public interface IOpenflowFacade {
    /**
     * Adds/Modifies the supplied flow on the nodeId directly using Openflow RPC or via config data store.
     * @param nodeId The device's id.
     * @param flow Flow object to add/modify.
     * @param callable Will be called in event of error.
     * @param isDirectRPC Should be true always. Throws UnsupportedOperationException at Runtime.
     */
    public void modifyFlow(NodeId nodeId, Flow flow, ErrorCallable callable, boolean isDirectRPC);

    /**
     * Deletes the supplied flow on the nodeId directly using Openflow RPC or via config data store.
     * @param nodeId The device's id.
     * @param flow Flow object to delete.
     * @param callable Will be called in event of error.
     * @param isDirectRPC Should be true always. Throws UnsupportedOperationException at Runtime.
     */
    public void deleteFlow(NodeId nodeId, Flow flow, ErrorCallable callable, boolean isDirectRPC);

    /**
     * Adds/Modifies the supplied group on the nodeId directly using Openflow RPC or via config data store.
     * @param nodeId The device's id.
     * @param group Group object to add/modify.
     * @param callable Will be called in event of error.
     * @param isDirectRPC Should be true always. Throws UnsupportedOperationException at Runtime.
     */
    public void modifyGroup(NodeId nodeId, Group group, ErrorCallable callable, boolean isDirectRPC);

    /**
     * Deletes the supplied group on the nodeId directly using Openflow RPC or via config data store.
     * @param nodeId The device's id.
     * @param group Group object to delete.
     * @param callable Will be called in event of error.
     * @param isDirectRPC Should be true always. Throws UnsupportedOperationException at Runtime.
     */
    public void deleteGroup(NodeId nodeId, Group group, ErrorCallable callable, boolean isDirectRPC);

    /**
     * Adds a bundle of groups and flows on the nodeId. Lists of Groups are installed in the order
     * they are submitted. Once the groups are installed, the list of flows is installed.
     * @param nodeId The device's id
     * @param groups Lists of group objects to be pushed in the order.
     * @param flows List of flow objects to be pushed after the groups.
     * @param callable Will be called in event of error.
     * @param isDirectRPC Should be true always. Throws UnsupportedOperationException at Runtime.
     */
    public void modifyBundle(NodeId nodeId, TreeMap<Integer, List<Group>> groups, List<Flow> flows,
                             ErrorCallable callable, boolean isDirectRPC);

    /**
     * Removes a bundle of groups and flows from the nodeId. Lists of Groups are removed in the reverse
     * order. After the groups are removed, flows are removed as well.
     * @param nodeId The device's id.
     * @param groups Lists of group objects to be deleted in the order.
     * @param flows List of flow objects to be deleted after the groups.
     * @param callable Will be called in event of error.
     * @param isDirectRPC Should be true always. Throws UnsupportedOperationException at Runtime.
     */
    public void deleteBundle(NodeId nodeId, TreeMap<Integer, List<Group>> groups, List<Flow> flows,
                             ErrorCallable callable, boolean isDirectRPC);

    /**
     * @param nodeId SwitchId for which resync has to be initiated
     */
    public void executeResync(NodeId nodeId);

    /**
     * @param nodeId SwitchId for which resync has to be initiated
     */
    public void cancelResync(NodeId nodeId);
}