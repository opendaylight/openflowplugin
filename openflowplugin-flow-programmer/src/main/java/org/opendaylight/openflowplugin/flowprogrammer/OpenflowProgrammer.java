/*
 * Copyright (c) 2015 Intel, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.flowprogrammer;

import com.google.common.util.concurrent.CheckedFuture;
import java.lang.Boolean;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;

import org.opendaylight.openflowplugin.flowprogrammer.flowconflictdetection.FlowConflictDetection;
import org.opendaylight.openflowplugin.flowprogrammer.flowutils.OpenflowUtils;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
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
    private static final String FLOWSPACE_PREFIX = "flowspace";
    private static final BigInteger FLOWSPACE_COOKIE = new BigInteger("FCFCFCFCCFCFCFCF", 16);
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

    
    private static final int[] PRJ_PRIORITIES = {10, 10, 10, 10, 10};

    private static final Logger LOG = LoggerFactory.getLogger(OpenflowProgrammer.class);
    private static DataBroker[] dataBrokers = new DataBroker[FlowConflictDetection.MAX_PRJ_ID];
    private static  HashMap<String, Boolean> tbl_zero_initialized = new HashMap<String, Boolean>();
    private static HashMap<String, AtomicLong> curFlowSpaceId = new HashMap<String, AtomicLong>();

    public static void setDataBroker(DataBroker dataBroker, short projectId) {
        dataBrokers[projectId] = dataBroker;
    }

    private static <U extends org.opendaylight.yangtools.yang.binding.DataObject> boolean writeMergeTransactionAPI
            (InstanceIdentifier<U> addIID, U data, LogicalDatastoreType logicalDatastoreType, short projectID) {
        boolean ret;

        WriteTransaction writeTx = dataBrokers[projectID].newWriteOnlyTransaction();
        //writeTx.put(logicalDatastoreType, addIID, data, true);
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

    private static <U extends org.opendaylight.yangtools.yang.binding.DataObject> boolean deleteTransactionAPI(InstanceIdentifier<U> deleteIID, LogicalDatastoreType logicalDatastoreType, short projectID) {
        boolean ret;

        WriteTransaction writeTx = dataBrokers[projectID].newWriteOnlyTransaction();
        writeTx.delete(logicalDatastoreType, deleteIID);
        CheckedFuture<Void, TransactionCommitFailedException> submitFuture = writeTx.submit();
        try {
            submitFuture.checkedGet();
            ret = true;
        } catch (TransactionCommitFailedException e) {
            LOG.error("deleteTransactionAPI: Transaction failed. Message: {}", e.getMessage());
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
     * @param tableID The table which "flow" will be added to
     * @param projectID The project which the caller belongs to
     * @return true if successful, false otherwise
     */
    public static boolean writeFlow(FlowBuilder flow, NodeId nodeId, short tableID, short projectID) {
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

        LOG.debug("Openflow Programmer: write flow to Node {}, table {}, project {}", nodeId.getValue(), flow.getTableId(), projectID);

        if (!writeMergeTransactionAPI(flowInstanceId, flow.build(), LogicalDatastoreType.CONFIGURATION, projectID)) {
            LOG.error("{}: Failed to create Flow on node: {}",
                    Thread.currentThread().getStackTrace()[1], nodeId.getValue());
            return false;
        }
        return true;
    }

    /**
     * This method removes flow from the specified table, all the
     * projects should call it to remove flow from tables.
     *
     * <p>
     * @param flowId The flow Id to be removed
     * @param nodeId The Openflow node which "flow" will be added to
     * @param tableID The table which "flow" will be added to
     * @param projectID The project which the caller belongs to
     * @return true if successful, false otherwise
     */
    public static boolean removeFlow(FlowId flowId, NodeId nodeId, short tableId, short projectId) {
        // Create the flow path
        InstanceIdentifier<Flow> flowInstanceId = InstanceIdentifier.builder(Nodes.class)
                .child(Node.class,  new NodeKey(nodeId)).augmentation(FlowCapableNode.class)
                .child(Table.class, new TableKey(tableId))
                .child(Flow.class,  new FlowKey(flowId))
                .build();

        LOG.debug("Openflow Programmer: remove flow from Node {}, table {}, project {}", nodeId.getValue(), tableId, projectId);
        if (! deleteTransactionAPI(flowInstanceId, LogicalDatastoreType.CONFIGURATION, projectId)) {
            LOG.error("{}: Failed to remove Flow on node: {}",
                    Thread.currentThread().getStackTrace()[1], nodeId.getValue());
            return false;
        }
        return true;
    }

    private static long getFlowSpaceId(NodeId nodeId) {
        if (curFlowSpaceId.get(nodeId.getValue()) == null) {
            curFlowSpaceId.put(nodeId.getValue(), new AtomicLong());
        }
        return curFlowSpaceId.get(nodeId.getValue()).getAndIncrement();
    }

    /**
     * This method initializes TBL_ZERO. The first addFlowSpace
     * call will call initializeTblZero to initialize TBL_ZERO.
     *
     * <p>
     * @param nodeId Openflow node ID
     * @param projectID Project ID
     * @return true if successful, false otherwise
     */
    private static boolean initializeTblZero(NodeId nodeId, short projectID) {
        String flowId = FLOWSPACE_PREFIX + ":t0:d" + String.valueOf(getFlowSpaceId(nodeId));
        FlowBuilder flow = OpenflowUtils.createDefaultDropFlow(TBL_ZERO, 0, FLOWSPACE_COOKIE, flowId);
        boolean ret = writeFlow(flow, nodeId, TBL_ZERO, projectID);
        if (ret == true) {
            tbl_zero_initialized.put(nodeId.getValue(), Boolean.TRUE);
            LOG.info("initializeTblZero: projectID {}", projectID);
        }
        else {
            LOG.error("initializeTblZero: failed to write default flow, projectID {}", projectID);
        }
        return ret;
    }

    /**
     * This method adds flow into TBL_ZERO, every project must set
     * different flows, otherwise it will fail. TBL_ZERO acts as
     * a classifier, it will classify the flows and steer them into
     * the first table of every project, so this method will add
     * goto_table action into the added flow.
     *
     * <p>
     * @param flow The FlowBuilder to add
     * @param nodeId The Openflow node which "flow" will be added to
     * @param projectID The project which the caller belongs to
     * @return true if successful, false otherwise
     */
    public static boolean addFlowSpace(FlowBuilder flow, NodeId nodeId, short projectID) {
        boolean ret;
        boolean ret2;

        /* Initialize table zero if not yet */
        if ((tbl_zero_initialized.get(nodeId.getValue()) == null)
            || !tbl_zero_initialized.get(nodeId.getValue()).equals(Boolean.TRUE)) {
            initializeTblZero(nodeId, projectID);
        }

        String flowId = FLOWSPACE_PREFIX + ":t0:f" + String.valueOf(getFlowSpaceId(nodeId));
        Match match = flow.getMatch();
        flow.setId(new FlowId(flowId));
        flow.setKey(new FlowKey(flow.getId()));
        flow.setFlowName(flow.getId().getValue());
        flow.setCookie(new FlowCookie(FLOWSPACE_COOKIE));
        flow.setCookieMask(new FlowCookie(FLOWSPACE_COOKIE));
        flow.setTableId(TBL_ZERO);
        flow.setPriority(PRJ_PRIORITIES[projectID]);
        ret = writeFlow(flow, nodeId, projectID, TBL_ZERO);

        /* Disable conflict detection before it is really ready */
        /*
        ret = FlowConflictDetection.isFlowOk(flow, nodeId, TBL_ZERO, projectID);
        
        if (ret == true) {
            boolean ret2 = writeFlow(flow, nodeId, projectID, TBL_ZERO);
            if (ret2 == true) {
                FlowConflictDetection.saveFlow(flow, nodeId, TBL_ZERO, projectID);
            }
            else {
                LOG.error("addFlowSpace: writeFlow failed");
            }
        }
        */
        LOG.info("Openflow Programmer: addFlowSpace to node {}, project {}, ret = {}", nodeId.getValue(), projectID, ret);
        return ret;
    }
}
