/*
 * Copyright (c) 2018 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frm.util;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
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
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("IllegalCatch")
public final class FrmUtil {
    private static final Logger LOG = LoggerFactory.getLogger(FrmUtil.class);
    private static final String SEPARATOR = ":";
    private static final long RPC_RESULT_TIMEOUT = 2500;

    private static final String JMX_OBJ_NAME_LIST_OF_SHRDS = "org.opendaylight.controller:type="
            + "DistributedConfigDatastore,Category=ShardManager,name=shard-manager-config";
    private static String JMX_OBJECT_SHARD_STATUS = "";

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

    public static Uint64 getDpnIdFromNodeName(final InstanceIdentifier<FlowCapableNode> nodeIdent) {
        String nodeId = nodeIdent.firstKeyOf(Node.class).getId().getValue();
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
        final Uint64 dpId = getDpnIdFromNodeName(nodeIdent);
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

    public static String getInventoryConfigDataStoreStatus() {
        boolean statusResult = true;
        try {
            ArrayList listOfShards = getAttributeJMXCommand(JMX_OBJ_NAME_LIST_OF_SHRDS, "LocalShards");
            if (listOfShards != null) {
                for (Object listOfShard : listOfShards) {
                    LOG.info("Listofshard is  {} ",listOfShard);
                    if (listOfShard.toString().contains("inventory")) {
                        JMX_OBJECT_SHARD_STATUS =
                                "org.opendaylight.controller:Category=Shards,name=" + listOfShard
                                        + ",type=DistributedConfigDatastore";
                        LOG.info("JMX object shard status is {} ",JMX_OBJECT_SHARD_STATUS);
                        String leader = getLeaderJMX(JMX_OBJECT_SHARD_STATUS, "Leader");
                        if (leader != null && leader.length() > 1) {
                            LOG.info("{} ::Inventory Shard has the Leader as:: {}", listOfShard, leader);
                        } else {
                            statusResult = false;
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("ERROR ::", e);
        }
        if (statusResult) {
            return "OPERATIONAL";
        } else {
            return "ERROR";
        }
    }

    private static ArrayList getAttributeJMXCommand(String objectName, String attributeName) {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ArrayList listOfShards = new ArrayList();
        if (mbs != null) {
            try {
                listOfShards = (ArrayList) mbs.getAttribute(new ObjectName(objectName), attributeName);
            } catch (MBeanException | AttributeNotFoundException | InstanceNotFoundException
                    | MalformedObjectNameException | ReflectionException e) {
                LOG.error("Exception while reading list of shards ", e);
            }
        }
        return listOfShards;
    }

    private static String getLeaderJMX(String objectName, String atrName) {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        String leader = "";
        if (mbs != null) {
            try {
                leader  = (String) mbs.getAttribute(new ObjectName(objectName), atrName);
            } catch (MalformedObjectNameException monEx) {
                LOG.error("CRITICAL EXCEPTION : Malformed Object Name Exception");
            } catch (MBeanException mbEx) {
                LOG.error("CRITICAL EXCEPTION : MBean Exception");
            } catch (InstanceNotFoundException infEx) {
                LOG.error("CRITICAL EXCEPTION : Instance Not Found Exception");
            } catch (ReflectionException rEx) {
                LOG.error("CRITICAL EXCEPTION : Reflection Exception");
            } catch (Exception e) {
                LOG.error("Attribute not found");
            }
        }
        return leader;
    }
}