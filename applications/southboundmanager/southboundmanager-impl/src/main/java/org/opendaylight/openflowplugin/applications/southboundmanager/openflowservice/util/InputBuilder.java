/*
 * Copyright (c) 2016 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.southboundmanager.openflowservice.util;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowTableRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class InputBuilder {
    public static AddFlowInputBuilder ADD_FLOW (NodeId nodeId, Flow flow) {
        InstanceIdentifier<Table> tableIId = getTableId(flow.getTableId(), nodeId);
        InstanceIdentifier<Flow> flowIId = getFlowIId(tableIId, flow.getId());
        AddFlowInputBuilder flowInputBuilder = new AddFlowInputBuilder(flow);
        flowInputBuilder.setNode(new NodeRef(getFlowCapableNodeId(nodeId)));
        flowInputBuilder.setFlowTable(new FlowTableRef(tableIId));
        flowInputBuilder.setFlowRef(new FlowRef(flowIId));
        return flowInputBuilder;
    }

    public static RemoveFlowInputBuilder REMOVE_FLOW (NodeId nodeId, Flow flow) {
        InstanceIdentifier<Table> tableIId = getTableId(flow.getTableId(), nodeId);
        InstanceIdentifier<Flow> flowIId = getFlowIId(tableIId, flow.getId());
        RemoveFlowInputBuilder flowInputBuilder = new RemoveFlowInputBuilder(flow);
        flowInputBuilder.setNode(new NodeRef(getFlowCapableNodeId(nodeId)));
        flowInputBuilder.setFlowTable(new FlowTableRef(tableIId));
        flowInputBuilder.setFlowRef(new FlowRef(flowIId));
        return flowInputBuilder;
    }

    public static AddGroupInputBuilder ADD_GROUP (NodeId nodeId, Group group) {
        InstanceIdentifier<Group> groupIId = getGroupIId(nodeId, group.getGroupId());
        AddGroupInputBuilder groupInputBuilder = new AddGroupInputBuilder(group);
        groupInputBuilder.setNode(new NodeRef(getFlowCapableNodeId(nodeId)));
        groupInputBuilder.setGroupRef(new GroupRef(groupIId));
        groupInputBuilder.setBuckets(group.getBuckets());
        return groupInputBuilder;
    }

    public static RemoveGroupInputBuilder REMOVE_GROUP (NodeId nodeId, Group group) {
        InstanceIdentifier<Group> groupIId = getGroupIId(nodeId, group.getGroupId());
        RemoveGroupInputBuilder groupInputBuilder = new RemoveGroupInputBuilder(group);
        groupInputBuilder.setNode(new NodeRef(getFlowCapableNodeId(nodeId)));
        groupInputBuilder.setGroupRef(new GroupRef(groupIId));
        return groupInputBuilder;
    }

    private static InstanceIdentifier<Node> getFlowCapableNodeId(NodeId nodeid){
        return InstanceIdentifier.builder(Nodes.class)
                .child(Node.class, new NodeKey(nodeid))
                .build();
    }

    private static InstanceIdentifier<Table> getTableId(Short tableId, NodeId nodeId) {
        return InstanceIdentifier.builder(Nodes.class)
                .child(Node.class, new NodeKey(nodeId))
                .augmentation(FlowCapableNode.class)
                .child(Table.class, new TableKey(tableId))
                .build();
    }

    private static InstanceIdentifier<Flow> getFlowIId(final InstanceIdentifier<Table> tablePath,
                                                       final FlowId flowId) {
        return tablePath.child(Flow.class, new FlowKey(flowId));
    }

    private static InstanceIdentifier<Group> getGroupIId(final NodeId nodeId, final GroupId groupId) {
        return InstanceIdentifier.builder(Nodes.class)
                .child(Node.class, new NodeKey(nodeId)).augmentation(FlowCapableNode.class)
                .child(Group.class, new GroupKey(groupId)).build();
    }
}
