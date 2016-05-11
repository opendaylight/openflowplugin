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
 * This class implements Openflowplugin Flow Programmer, it is used to
 * program Openflow, all the applications which are to fix app co-existence
 * should call Flow Programmer APIs.
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
    private static final BigInteger FLOWSPACE_COOKIE = new BigInteger("FEFEFEFEEFEFEFEF", 16);

    /* Table ID: table 0 reserved */
    public static final short TBL_ZERO = 0;

    private static short cur_avail_tbl_no = 0;
    private static final Object lockObj = new Object();

    private static final Logger LOG = LoggerFactory.getLogger(OpenflowProgrammer.class);
    private static HashMap<String, DataBroker> dataBrokers = new HashMap<String, DataBroker>();
    private static boolean tbl_zero_initialized = false;
    private static HashMap<String, AtomicLong> curFlowSpaceId = new HashMap<String, AtomicLong>();
    private static HashMap<String, Short> appTblOffset = new HashMap<String, Short>();
    private static HashMap<String, Short> appMaxTblId = new HashMap<String, Short>();

    public static void setDataBroker(DataBroker dataBroker, String appId) {
        if (dataBrokers.get(appId) == null) {
            dataBrokers.put(appId, dataBroker);
        }
    }

    private static <U extends org.opendaylight.yangtools.yang.binding.DataObject> boolean writeMergeTransactionAPI
            (InstanceIdentifier<U> addIID, U data, LogicalDatastoreType logicalDatastoreType, String appId) {
        boolean ret;

        WriteTransaction writeTx = dataBrokers.get(appId).newWriteOnlyTransaction();
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

    private static <U extends org.opendaylight.yangtools.yang.binding.DataObject> boolean deleteTransactionAPI(InstanceIdentifier<U> deleteIID, LogicalDatastoreType logicalDatastoreType, String appId) {
        boolean ret;

        WriteTransaction writeTx = dataBrokers.get(appId).newWriteOnlyTransaction();
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
     * This method registers an application and allocate table number
     * space for it, then the registered application can get its
     * table number offset
     *
     * <p>
     * @param appId The registered application ID
     * @param maxTableId max table number of the registered application
     * @return true if successful, false otherwise
     */
    public static boolean registerApp(String appId, short maxTableId) {
        boolean ret = false;
        synchronized (lockObj) {
            if (appTblOffset.get(appId) == null) {
                appTblOffset.put(appId, Short.valueOf(cur_avail_tbl_no));
                appMaxTblId.put(appId, Short.valueOf(maxTableId));
                cur_avail_tbl_no += maxTableId + 1;
                ret = true;
            }
        }
        return ret;
    }

    /**
     * This method gets table offset of an application
     *
     * <p>
     * @param appId The registered application ID
     * @return app table offset if registered, 0 otherwise
     */
    public static short getAppTblOffset(String appId) {
        Short offset = appTblOffset.get(appId);
        if (offset == null) {
            return ((short) 0);
        }
        else {
            return offset.shortValue();
        }
    }

    /**
     * This method gets max table ID of an application
     *
     * <p>
     * @param appId The registered application ID
     * @return max table id of the given app if registered, 0 otherwise
     */
    public static short getAppMaxTblId(String appId) {
        Short tblId = appMaxTblId.get(appId);
        if (tblId == null) {
            return ((short) 0);
        }
        else {
            return tblId.shortValue();
        }
    }

    private static boolean addFlow(FlowBuilder flow, NodeId nodeId, short tableID, String appId) {
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

        LOG.debug("Openflow Programmer: add flow to Node {}, table {}, application {}", nodeId.getValue(), flow.getTableId(), appId);

        if (!writeMergeTransactionAPI(flowInstanceId, flow.build(), LogicalDatastoreType.CONFIGURATION, appId)) {
            LOG.error("{}: Failed to create Flow on node: {}",
                    Thread.currentThread().getStackTrace()[1], nodeId.getValue());
            return false;
        }
        return true;
    }

    /**
     * This method adds flow into the specified table, all the
     * application should call it to program Openflow tables.
     *
     * <p>
     * @param flow The flow to add
     * @param nodeId The Openflow node which "flow" will be added to
     * @param tableID The table which "flow" will be added to
     * @param appId Application name
     * @return true if successful, false otherwise
     */
    public static boolean writeFlow(FlowBuilder flow, NodeId nodeId, short tableID, String appId) {
        if (tableID == TBL_ZERO) {
            return false;
        }
        return addFlow(flow, nodeId, tableID, appId);
    }

    private static boolean deleteFlow(FlowId flowId, NodeId nodeId, short tableId, String appId) {
        // Create the flow path
        InstanceIdentifier<Flow> flowInstanceId = InstanceIdentifier.builder(Nodes.class)
                .child(Node.class,  new NodeKey(nodeId)).augmentation(FlowCapableNode.class)
                .child(Table.class, new TableKey(tableId))
                .child(Flow.class,  new FlowKey(flowId))
                .build();

        LOG.debug("Openflow Programmer: remove flow from Node {}, table {}, application {}", nodeId.getValue(), tableId, appId);
        if (! deleteTransactionAPI(flowInstanceId, LogicalDatastoreType.CONFIGURATION, appId)) {
            LOG.error("{}: Failed to remove Flow on node: {}",
                    Thread.currentThread().getStackTrace()[1], nodeId.getValue());
            return false;
        }
        return true;
    }

    /**
     * This method removes flow from the specified table, all the
     * applications should call it to remove flow from tables.
     *
     * <p>
     * @param flowId The flow Id to be removed
     * @param nodeId The Openflow node which "flow" will be added to
     * @param tableId The table which "flow" will be added to
     * @param appId Application name
     * @return true if successful, false otherwise
     */
    public static boolean removeFlow(FlowId flowId, NodeId nodeId, short tableId, String appId) {
        if (tableId == TBL_ZERO) {
            return false;
        }
        return deleteFlow(flowId, nodeId, tableId, appId);
    }

    private static long getFlowSpaceId(NodeId nodeId) {
        synchronized (lockObj) {
            if (curFlowSpaceId.get(nodeId.getValue()) == null) {
                curFlowSpaceId.put(nodeId.getValue(), new AtomicLong());
            }
        }
        return curFlowSpaceId.get(nodeId.getValue()).getAndIncrement();
    }

    /**
     * This method initializes TBL_ZERO. The first addFlowSpace
     * call will call initializeTblZero to initialize TBL_ZERO.
     *
     * <p>
     * @param nodeId Openflow node ID
     * @param appId Application name
     * @return true if successful, false otherwise
     */
    private static boolean initializeTblZero(NodeId nodeId, String appId) {
        String flowId = FLOWSPACE_PREFIX + ":" + appId + ":" + String.valueOf(getFlowSpaceId(nodeId));
        FlowBuilder flow = OpenflowUtils.createDefaultDropFlow(TBL_ZERO, 0, FLOWSPACE_COOKIE, flowId);
        boolean ret = addFlow(flow, nodeId, TBL_ZERO, appId);
        if (ret == true) {
            LOG.info("initializeTblZero: appId {}", appId);
        }
        else {
            LOG.error("initializeTblZero: failed to write default flow, application {}", appId);
        }
        return ret;
    }

    /**
     * This method adds flow into TBL_ZERO, every applicaition
     * should set different flows. TBL_ZERO acts as application
     * dispatcher, it will classify the flows and steer them into
     * the first table of every application, this method will
     * automatically add goto_table action for the added flow.
     *
     * <p>
     * @param flow The FlowBuilder to add
     * @param nodeId The Openflow node which "flow" will be added to
     * @param appId Application name
     * @return true if successful, false otherwise
     */
    public static synchronized boolean addFlowSpace(FlowBuilder flow, NodeId nodeId, String appId) {
        boolean ret = false;

        synchronized (lockObj) {
            /* Initialize table zero if not yet */
            if (tbl_zero_initialized == false) {
                initializeTblZero(nodeId, appId);
                tbl_zero_initialized = true;
            }

            String flowId = FLOWSPACE_PREFIX + ":" + appId + ":" + String.valueOf(getFlowSpaceId(nodeId));
            Match match = flow.getMatch();
            flow.setId(new FlowId(flowId));
            flow.setKey(new FlowKey(flow.getId()));
            flow.setFlowName(flow.getId().getValue());
            flow.setCookie(flow.getCookie());
            flow.setCookieMask(flow.getCookieMask());
            flow.setTableId(TBL_ZERO);
            ret = addFlow(flow, nodeId, TBL_ZERO, appId);
        }

        LOG.info("Openflow Programmer: addFlowSpace to node {}, application {}, ret = {}", nodeId.getValue(), appId, ret);
        return ret;
    }

    /**
     * This method removes flow from TBL_ZERO, all the
     * applications should call it to remove flow from TBL_ZERO.
     *
     * <p>
     * @param flowId The flow Id to be removed
     * @param nodeId The Openflow node which "flow" will be added to
     * @param tableId The table which "flow" will be added to
     * @param appId Application name
     * @return true if successful, false otherwise
     */
    public static boolean removeFlowSpace(FlowId flowId, NodeId nodeId, String appId) {
        return deleteFlow(flowId, nodeId, TBL_ZERO, appId);
    }
}
