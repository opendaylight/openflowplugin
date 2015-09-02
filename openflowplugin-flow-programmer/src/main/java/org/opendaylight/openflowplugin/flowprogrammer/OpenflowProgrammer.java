/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.flowprogrammer;

import com.google.common.util.concurrent.CheckedFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;

import org.opendaylight.openflowplugin.flowprogrammer.flowconflictdetection.FlowConflictDetection;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * This class implements Openflow Programmer, it is used to program
 * Openflow rules, all the projects which are to program Openflow rules
 * should call Openflow Programmer APIs, Openflow Programmer will
 * help detect flow conflict, uniformly manage table ID, flow ID and
 * coordinate all the related projects in order to make sure they can
 * work together as expected.
 *
 * <p>
 *
 * @author Yi Yang (yi.y.yang@intel.com)
 *
 * <p>
 * @since 2015-08-25
 */

public class OpenflowProgrammer {
    /* Project ID: statically allocated */
    public static final short PRJ_SFC =   0;
    public static final short PRJ_OVSDB = 1;
    public static final short PRJ_GBP   = 2;

    /* Table ID: statically allocated, table 0 reserved */
    public static final short TBL_ZERO = 0;

    /* For PRJ_SFC */
    private static final short PRJ_SFC_OFFSET = 1;
    public static final short TBL_SFC_INGRESS_TRANSPORT = PRJ_SFC_OFFSET + 0;
    public static final short TBL_SFC_PATH_MAPPER =       PRJ_SFC_OFFSET + 1;
    public static final short TBL_SFC_PATH_MAPPER_ACL =   PRJ_SFC_OFFSET + 2;
    public static final short TBL_SFC_NEXT_HOP =          PRJ_SFC_OFFSET + 3;
    public static final short TBL_SFC_TRANSPORT_EGRESS =  PRJ_SFC_OFFSET + 4;

    /* For PRJ_OVSDB */
    private static final short PRJ_OVSDB_OFFSET = 20;
    public static final short TBL_OVSDB_CLASSIFIER =    PRJ_OVSDB_OFFSET + 0;
    public static final short TBL_OVSDB_ARP_RESPONDER = PRJ_OVSDB_OFFSET + 1;
    public static final short TBL_OVSDB_INBOUND_NAT =   PRJ_OVSDB_OFFSET + 2;
    public static final short TBL_OVSDB_EGRESS_ACL =    PRJ_OVSDB_OFFSET + 3;
    public static final short TBL_OVSDB_LOAD_BALANCER = PRJ_OVSDB_OFFSET + 4;
    public static final short TBL_OVSDB_ROUTING =       PRJ_OVSDB_OFFSET + 5;
    public static final short TBL_OVSDB_L3_FORWARDING = PRJ_OVSDB_OFFSET + 6;
    public static final short TBL_OVSDB_L2_REWRITE =    PRJ_OVSDB_OFFSET + 7;
    public static final short TBL_OVSDB_INGRESS_ACL =   PRJ_OVSDB_OFFSET + 8;
    public static final short TBL_OVSDB_OUTBOUND_NAT =  PRJ_OVSDB_OFFSET + 9;
    public static final short TBL_OVSDB_L2_FORWARDING = PRJ_OVSDB_OFFSET + 10;

    /* For PRJ_GBP */
    private static final short PRJ_GBP_OFFSET = 40;
    public static final short TBL_GBP_PORTSECURITY =       PRJ_GBP_OFFSET + 0;
    public static final short TBL_GBP_INGRESS_NAT =        PRJ_GBP_OFFSET + 1;
    public static final short TBL_GBP_SOURCE_MAPPER =      PRJ_GBP_OFFSET + 2;
    public static final short TBL_GBP_DESTINATION_MAPPER = PRJ_GBP_OFFSET + 3;
    public static final short TBL_GBP_POLICY_ENFORCER =    PRJ_GBP_OFFSET + 4;
    public static final short TBL_GBP_EGRESS_NAT =         PRJ_GBP_OFFSET + 5;
    public static final short TBL_GBP_EXTERNAL_MAPPER =    PRJ_GBP_OFFSET + 6;

    private static final Logger LOG = LoggerFactory.getLogger(OpenflowProgrammer.class);
    private static DataBroker[] dataBrokers = new DataBroker[FlowConflictDetection.MAX_PRJ_ID];

    public static void setDataBroker(DataBroker dataBroker, short projectId) {
        dataBrokers[projectId] = dataBroker;
    }

    private static <U extends org.opendaylight.yangtools.yang.binding.DataObject> boolean writeMergeTransactionAPI
            (InstanceIdentifier<U> addIID, U data, LogicalDatastoreType logicalDatastoreType, short projectID) {
        boolean ret;

        WriteTransaction writeTx = dataBrokers[projectID].newWriteOnlyTransaction();
        writeTx.merge(logicalDatastoreType, addIID, data, true);
        CheckedFuture<Void, TransactionCommitFailedException> submitFuture = writeTx.submit();
        try {
            submitFuture.checkedGet();
            ret = true;
        } catch (TransactionCommitFailedException e) {
            LOG.error("writeMergeTransactionAPI: Transaction failed. Message: {}", e.getMessage());
            ret = false;
        }
        return ret;
    }

    /**
     * This method adds flow into the specified table, all the
     * projects should call it to program Openflow tables, it can
     * help detect flow conflict if needed, but it is limited in
     * a project, conflict between projects won't be covered.
     *
     * <p>
     * @param flow The flow to add
     * @param nodeId The Openflow node which "flow" will be added to
     * @param projectID The project which the caller belongs to
     * @param tableID The table which "flow" will be added to
     * @return true if successful, false otherwise
     */
    public boolean writeFlow(Flow flow, NodeId nodeId, short projectID, short tableID) {
        // Create the NodeBuilder
        NodeBuilder nodeBuilder = new NodeBuilder();
        nodeBuilder.setId(nodeId);
        nodeBuilder.setKey(new NodeKey(nodeBuilder.getId()));

        // Create the flow path, which will include the Node, Table, and Flow
        InstanceIdentifier<Flow> flowInstanceId = InstanceIdentifier.builder(Nodes.class)
            .child(Node.class, nodeBuilder.getKey()).augmentation(FlowCapableNode.class)
            .child(Table.class, new TableKey(flow.getTableId()))
            .child(Flow.class, flow.getKey())
            .build();

        LOG.debug("Openflow Programmer: write flow to Node {}, table {}", nodeId.getValue(), flow.getTableId());

        if (!writeMergeTransactionAPI(flowInstanceId, flow, LogicalDatastoreType.CONFIGURATION, projectID)) {
            LOG.error("{}: Failed to create Flow on node: {}",
                    Thread.currentThread().getStackTrace()[1], nodeId.getValue());
            return false;
        }
        return true;
    }

    /**
     * This method adds flow into TBL_ZERO, every project must set
     * different flows, otherwise it will fail. TBL_ZERO acts as
     * a classifier, it will classify the flows and steer them into
     * the first table of every project, so this method will add
     * goto_table action into the added flow.
     *
     * <p>
     * @param flow The flow to add
     * @param nodeId The Openflow node which "flow" will be added to
     * @param projectID The project which the caller belongs to
     * @return true if successful, false otherwise
     */
    public boolean addFlowSpace(Flow flow, NodeId nodeId, short projectID) {
        Match match = flow.getMatch();
        FlowId flowId = flow.getId();
        boolean ret = FlowConflictDetection.isFlowOk(flow, nodeId, TBL_ZERO, projectID);
        if (ret == true) {
            ret = writeFlow(flow, nodeId, projectID, TBL_ZERO);
            FlowConflictDetection.saveFlow(flow, nodeId, TBL_ZERO, projectID);
        }
        return ret;
    }
}
