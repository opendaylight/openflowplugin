/*
 * Copyright (c) 2018 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frm.util;

import java.math.BigInteger;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.arbitrator.reconcile.service.rev180227.GetActiveBundleOutput;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FrmUtil {
    private static final Logger LOG = LoggerFactory.getLogger(FrmUtil.class);
    private static final String SEPARATOR = ":";
    private static final long RPC_RESULT_TIMEOUT = 2500;
    public static final String OPENFLOW_PREFIX = "openflow:";

    private FrmUtil() {
        throw new IllegalStateException("This class should not be instantiated.");
    }

    public static NodeId getNodeIdFromNodeIdentifier(final InstanceIdentifier<FlowCapableNode> nodeIdent) {
        return nodeIdent.firstKeyOf(Node.class).getId();
    }

    public static String getNodeIdValueFromNodeIdentifier(final InstanceIdentifier<FlowCapableNode> nodeIdent) {
        return getNodeIdFromNodeIdentifier(nodeIdent).getValue();
    }

    public static String getFlowId(final FlowRef flowRef) {
        return flowRef.getValue().firstKeyOf(Flow.class).getId().getValue();
    }

    public static String getFlowId(final InstanceIdentifier<Flow> identifier) {
        return getFlowId(new FlowRef(identifier));
    }

    public static Uint8 getTableId(final FlowTableRef flowTableRef) {
        return flowTableRef.getValue().firstKeyOf(Table.class).getId();
    }

    public static Uint8 getTableId(final InstanceIdentifier<Flow> identifier) {
        return getTableId(new FlowTableRef(identifier));
    }

    public static BigInteger getDpnIdFromNodeName(final InstanceIdentifier<FlowCapableNode> nodeIdent) {
        String nodeId = nodeIdent.firstKeyOf(Node.class).getId().getValue();
        String dpId = nodeId.substring(nodeId.lastIndexOf(SEPARATOR) + 1);
        return new BigInteger(dpId);
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
                for (Action action : actions) {
                    if (action.getAction().implementedInterface()
                            .equals(ActionType.GROUP_ACTION.getActionType())) {
                        return ((GroupActionCase) action.getAction()).getGroupAction()
                                .getGroupId();
                    }
                }
            }
        }
        return null;
    }

    public static InstanceIdentifier<Group> buildGroupInstanceIdentifier(
            final InstanceIdentifier<FlowCapableNode> nodeIdent, final Uint32 groupId) {
        NodeId nodeId = getNodeIdFromNodeIdentifier(nodeIdent);
        InstanceIdentifier<Group> groupInstanceId = InstanceIdentifier.builder(Nodes.class)
                .child(Node.class, new NodeKey(nodeId))
                .augmentation(FlowCapableNode.class)
                .child(Group.class, new GroupKey(new GroupId(groupId)))
                .build();
        return groupInstanceId;
    }

    public static BundleId getActiveBundle(final InstanceIdentifier<FlowCapableNode> nodeIdent,
                                           final ForwardingRulesManager provider) {
        BigInteger dpId = getDpnIdFromNodeName(nodeIdent);
        final NodeRef nodeRef = new NodeRef(nodeIdent.firstIdentifierOf(Node.class));
        GetActiveBundleInputBuilder input = new GetActiveBundleInputBuilder().setNodeId(dpId).setNode(nodeRef);
        try {
            RpcResult<GetActiveBundleOutput> result = provider.getArbitratorReconciliationManager()
                    .getActiveBundle(input.build()).get(RPC_RESULT_TIMEOUT, TimeUnit.MILLISECONDS);
            if (!result.isSuccessful()) {
                LOG.trace("Error while retrieving active bundle present for node {}", dpId);
            } else {
                return result.getResult().getResult();
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            LOG.error("Error while retrieving active bundle present for node {} is {}", dpId , e.getMessage());
        }
        return null;
    }

    public static boolean isGroupExistsOnDevice(final InstanceIdentifier<FlowCapableNode> nodeIdent,
                                                final Uint32 groupId,
                                                final ForwardingRulesManager provider) {
        String nodeId = getNodeIdValueFromNodeIdentifier(nodeIdent);
        return provider.getDevicesGroupRegistry().isGroupPresent(nodeId, groupId);
    }
}
