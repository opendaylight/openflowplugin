/*
 * Copyright (c) 2015 Intel, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.flowprogrammer;

import com.google.common.util.concurrent.CheckedFuture;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.openflowplugin.flowprogrammer.AbstractDataStoreManager;
import org.opendaylight.openflowplugin.flowprogrammer.OpenflowProgrammer;
import org.opendaylight.openflowplugin.flowprogrammer.flowconflictdetection.FlowConflictDetection;
import org.opendaylight.openflowplugin.flowprogrammer.flowutils.OpenflowUtils;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.GoToTableCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.go.to.table._case.GoToTableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenflowProgrammerTest extends AbstractDataStoreManager {
    private static InstructionsBuilder isb = null;
    private static final Logger LOG = LoggerFactory.getLogger(OpenflowProgrammerTest.class);
    private static boolean init = false;
    private static DataBroker dataBroker = null;

    private static <U extends org.opendaylight.yangtools.yang.binding.DataObject> boolean writeMergeTransactionAPI
            (InstanceIdentifier<U> addIID, U data, LogicalDatastoreType logicalDatastoreType) {
        boolean ret;

        WriteTransaction writeTx = dataBroker.newWriteOnlyTransaction();
        writeTx.merge(logicalDatastoreType, addIID, data, true);
        CheckedFuture<Void, TransactionCommitFailedException> submitFuture = writeTx.submit();
        try {
            submitFuture.checkedGet();
            ret = true;
        } catch (TransactionCommitFailedException e) {
            LOG.info("writeMergeTransactionAPI: Transaction failed. Message: {}", e.getMessage());
            ret = false;
        }
        return ret;
    }

    private static <U extends org.opendaylight.yangtools.yang.binding.DataObject> boolean deleteTransactionAPI(InstanceIdentifier<U> deleteIID, LogicalDatastoreType logicalDatastoreType) {
        boolean ret;

        WriteTransaction writeTx = dataBroker.newWriteOnlyTransaction();
        writeTx.delete(logicalDatastoreType, deleteIID);
        CheckedFuture<Void, TransactionCommitFailedException> submitFuture = writeTx.submit();
        try {
            submitFuture.checkedGet();
            ret = true;
        } catch (TransactionCommitFailedException e) {
            LOG.info("deleteTransactionAPI: Transaction failed. Message: {}", e.getMessage());
            ret = false;
        }
        return ret;
    }

    @BeforeClass
    public static void beforeClass() {
        int order = 0;

        /* Initialize InstructionsBuilder */
        List<Action> actionList = new ArrayList<Action>();
        ApplyActionsBuilder aab = new ApplyActionsBuilder();

        // Set Dscp
        actionList.add(OpenflowUtils.createActionWriteDscp((short)11, order++));

        // Create an Apply Action
        aab.setAction(actionList);

        // Wrap our Apply Action in an Instruction
        InstructionBuilder ib = new InstructionBuilder();
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());
        ib.setOrder(0);
        ib.setKey(new InstructionKey(0));

        List<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(ib.build());

        // Action, goto path mapper table
        GoToTableBuilder gotoTb = OpenflowUtils.createActionGotoTable(OpenflowProgrammer.TBL_SFC_PATH_MAPPER);

        ib = new InstructionBuilder();
        ib.setKey(new InstructionKey(order));
        ib.setOrder(order++);
        ib.setInstruction(new GoToTableCaseBuilder().setGoToTable(gotoTb.build()).build());
        instructions.add(ib.build());

        // Finish up the instructions
        isb = new InstructionsBuilder();
        isb.setInstruction(instructions);
        LOG.info("beforeClass() done");
    }

    @Before
    public void before() {
        if (init == true) {
            return;
        }
        setFlowProgrammerImpl();
        dataBroker = getDataBroker();
        OpenflowProgrammer.setDataBroker(dataBroker, OpenflowProgrammer.PRJ_SFC);
        OpenflowProgrammer.setDataBroker(dataBroker, OpenflowProgrammer.PRJ_GBP);

        /* Add a node to inventory */
        // Create the NodeBuilder
        NodeBuilder nodeBuilder = new NodeBuilder();
        nodeBuilder.setId(new NodeId("openflow:1"));
        nodeBuilder.setKey(new NodeKey(nodeBuilder.getId()));

        InstanceIdentifier<Node> nodeId = InstanceIdentifier.builder(Nodes.class).child(Node.class, nodeBuilder.getKey()).build();
        boolean ret = writeMergeTransactionAPI(nodeId, nodeBuilder.build(), LogicalDatastoreType.CONFIGURATION);
        assertTrue("Must be true", ret);
        init = true;
    }

    @Test
    public void testWriteFlow() throws Exception {
        MatchBuilder match = new MatchBuilder();
        OpenflowUtils.addMatchEtherType(match, (long)0x0800);

        FlowBuilder flowbuilder = OpenflowUtils.createFlowBuilder(OpenflowProgrammer.TBL_SFC_INGRESS_TRANSPORT, 0, new BigInteger("9999", 10), "SFC_TEST_FLOW", match, isb);
        boolean ret = OpenflowProgrammer.writeFlow(flowbuilder, new NodeId("openflow:1"), OpenflowProgrammer.TBL_SFC_INGRESS_TRANSPORT, OpenflowProgrammer.PRJ_SFC);
        assertTrue("Must be true", ret);
    }

    @Test
    public void testRemoveFlow() throws Exception {
        BigInteger bigInt = new BigInteger("9999", 10);
        boolean ret = OpenflowProgrammer.removeFlow(new FlowId(bigInt.toString()), new NodeId("openflow:1"), OpenflowProgrammer.TBL_SFC_INGRESS_TRANSPORT, OpenflowProgrammer.PRJ_SFC);
        assertTrue("Must be true", ret);
    }

    @Test
    public void testAddFlowSpace() throws Exception {
        MatchBuilder match = new MatchBuilder();
        OpenflowUtils.addMatchEtherType(match, (long)0x0800);

        FlowBuilder flowbuilder1 = OpenflowUtils.createFlowBuilder(OpenflowProgrammer.TBL_ZERO, 0, new BigInteger("9991", 10), "SFC_TEST_FLOW1", match, isb);
        boolean ret = OpenflowProgrammer.addFlowSpace(flowbuilder1, new NodeId("openflow:1"), OpenflowProgrammer.PRJ_SFC);
        assertTrue("Must be true", ret);
        FlowBuilder flowbuilder2 = OpenflowUtils.createFlowBuilder(OpenflowProgrammer.TBL_ZERO, 0, new BigInteger("9992", 10), "GBP_TEST_FLOW1", match, isb);
        ret = OpenflowProgrammer.addFlowSpace(flowbuilder2, new NodeId("openflow:1"), OpenflowProgrammer.PRJ_GBP);
        assertFalse("Must be false", ret);
    }

    @AfterClass
    public static void afterClass() {
        //Clean datastore
        InstanceIdentifier<Node> nodeId = InstanceIdentifier.builder(Nodes.class).child(Node.class, new NodeKey(new NodeId("openflow:1"))).build();
        boolean ret = deleteTransactionAPI(nodeId, LogicalDatastoreType.CONFIGURATION);
        assertTrue("Must be true", ret);
        FlowConflictDetection.clearAllFlows();
        LOG.info("afterClass() done");
    }
}
