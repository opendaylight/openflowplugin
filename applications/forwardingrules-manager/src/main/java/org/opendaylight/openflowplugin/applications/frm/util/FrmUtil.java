/*
 * Copyright (c) 2018 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frm.util;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.opendaylight.openflowplugin.applications.frm.ActionType;
import org.opendaylight.openflowplugin.applications.frm.ForwardingRulesManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.GroupActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowTableRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.WriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.BundleId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.arbitrator.reconcile.service.rev180227.GetActiveBundleInputBuilder;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FrmUtil {
    private static final Logger LOG = LoggerFactory.getLogger(FrmUtil.class);
    private static final String SEPARATOR = ":";
    private static final long RPC_RESULT_TIMEOUT = 2500;
    public static final String OPENFLOW_PREFIX = "openflow:";

    private FrmUtil() {
        // Hidden on purpose
    }

    public static NodeId getNodeIdFromNodeIdentifier(final DataObjectIdentifier<FlowCapableNode> nodeIdent) {
        return nodeIdent.getFirstKeyOf(Node.class).getId();
    }

    public static String getNodeIdValueFromNodeIdentifier(final DataObjectIdentifier<FlowCapableNode> nodeIdent) {
        return getNodeIdFromNodeIdentifier(nodeIdent).getValue();
    }

    public static String getFlowId(final FlowRef flowRef) {
        return flowRef.getValue().getFirstKeyOf(Flow.class).getId().getValue();
    }

    public static String getFlowId(final DataObjectIdentifier<Flow> identifier) {
        return getFlowId(new FlowRef(identifier));
    }

    public static Uint8 getTableId(final FlowTableRef flowTableRef) {
        return flowTableRef.getValue().getFirstKeyOf(Table.class).getId();
    }

    public static Uint8 getTableId(final DataObjectIdentifier<Flow> identifier) {
        return getTableId(new FlowTableRef(identifier));
    }

    public static Uint64 getDpnIdFromNodeName(final DataObjectIdentifier<FlowCapableNode> nodeIdent) {
        String nodeId = nodeIdent.getFirstKeyOf(Node.class).getId().getValue();
        return Uint64.valueOf(nodeId.substring(nodeId.lastIndexOf(SEPARATOR) + 1));
    }

    public static Uint32 isFlowDependentOnGroup(final Flow flow) {
        LOG.debug("Check if flow {} is dependent on group", flow);
        if (flow.getInstructions() != null) {
            Collection<Instruction> instructions = flow.getInstructions().nonnullInstruction().values();
            for (Instruction instruction : instructions) {
                Collection<Action> actions = Collections.emptyList();
                if (instruction.getInstruction().implementedInterface()
                        .equals(ActionType.APPLY_ACTION.getActionType())) {
                    actions = ((ApplyActionsCase) instruction.getInstruction())
                            .getApplyActions().nonnullAction().values();
                } else if (instruction.getInstruction().implementedInterface()
                        .equals(ActionType.WRITE_ACTION.getActionType())) {
                    actions = ((WriteActionsCase)instruction.getInstruction())
                            .getWriteActions().nonnullAction().values();
                }
                if (actions != null) {
                    for (Action action : actions) {
                        if (action.getAction().implementedInterface().equals(ActionType.GROUP_ACTION.getActionType())) {
                            return ((GroupActionCase) action.getAction()).getGroupAction().getGroupId();
                        }
                    }
                }
            }
        }
        return null;
    }

    public static DataObjectIdentifier<Group> buildGroupInstanceIdentifier(
            final DataObjectIdentifier<FlowCapableNode> nodeIdent, final Uint32 groupId) {
        return  DataObjectIdentifier.builder(Nodes.class)
            .child(Node.class, new NodeKey(getNodeIdFromNodeIdentifier(nodeIdent)))
            .augmentation(FlowCapableNode.class)
            .child(Group.class, new GroupKey(new GroupId(groupId)))
            .build();
    }

    public static BundleId getActiveBundle(final DataObjectIdentifier<FlowCapableNode> nodeIdent,
                                           final ForwardingRulesManager provider) {
        final Uint64 dpId = getDpnIdFromNodeName(nodeIdent);
        final NodeRef nodeRef = new NodeRef(nodeIdent.trimTo(Node.class));
        GetActiveBundleInputBuilder input = new GetActiveBundleInputBuilder().setNodeId(dpId).setNode(nodeRef);
        try {
            final var result = provider.getActiveBundle().invoke(input.build())
                .get(RPC_RESULT_TIMEOUT, TimeUnit.MILLISECONDS);
            if (result.isSuccessful()) {
                return result.getResult().getResult();
            }
            LOG.trace("Error while retrieving active bundle present for node {}", dpId);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            LOG.error("Error while retrieving active bundle present for node {} is {}", dpId , e.getMessage());
        }
        return null;
    }

    public static boolean isGroupExistsOnDevice(final DataObjectIdentifier<FlowCapableNode> nodeIdent,
                                                final Uint32 groupId,
                                                final ForwardingRulesManager provider) {
        String nodeId = getNodeIdValueFromNodeIdentifier(nodeIdent);
        return provider.getDevicesGroupRegistry().isGroupPresent(nodeId, groupId);
    }
}
