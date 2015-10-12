/*
 * Copyright (c) 2015 Intel, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.flowprogrammer.flowconflictdetection;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.flowprogrammer.AbstractDataStoreManager;
import org.opendaylight.openflowplugin.flowprogrammer.OpenflowProgrammer;
import org.opendaylight.openflowplugin.flowprogrammer.flowutils.OpenflowUtils;
import org.opendaylight.openflowplugin.flowprogrammer.flowconflictdetection.FlowConflictDetection;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlowConflictDetectionTest extends AbstractDataStoreManager {
    private static InstructionsBuilder isb = null;
    private static final Logger LOG = LoggerFactory.getLogger(FlowConflictDetectionTest.class);
    private static boolean init = false;

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
        LOG.info("beforeClass() done!");
    }

    @Before
    public void before() {
        if (init == true) {
            return;
        }
        setFlowProgrammerImpl();
        init = true;
    }

    @Test
    public void testSaveFlow() throws Exception {
        MatchBuilder match = new MatchBuilder();
        OpenflowUtils.addMatchEtherType(match, (long)0x0800);

        FlowBuilder flowbuilder = OpenflowUtils.createFlowBuilder(OpenflowProgrammer.TBL_SFC_INGRESS_TRANSPORT, 0, new BigInteger("9999", 10), "SFC_TEST_FLOW", match, isb);
        FlowConflictDetection.saveFlow(flowbuilder, new NodeId("openflow:1"), OpenflowProgrammer.TBL_SFC_INGRESS_TRANSPORT, OpenflowProgrammer.PRJ_SFC);
        assertTrue("Always true", true);
    }

    @Test
    public void testIsFlowOkInProject() throws Exception {
        MatchBuilder match = new MatchBuilder();
        OpenflowUtils.addMatchEtherType(match, (long)0x0800);

        FlowBuilder flowbuilder = OpenflowUtils.createFlowBuilder(OpenflowProgrammer.TBL_SFC_INGRESS_TRANSPORT, 0, new BigInteger("9999", 10), "SFC_TEST_FLOW", match, isb);
        FlowConflictDetection.saveFlow(flowbuilder, new NodeId("openflow:1"), OpenflowProgrammer.TBL_SFC_INGRESS_TRANSPORT, OpenflowProgrammer.PRJ_SFC);
        boolean ret = FlowConflictDetection.isFlowOkInProject(flowbuilder, new NodeId("openflow:1"), OpenflowProgrammer.TBL_SFC_INGRESS_TRANSPORT, OpenflowProgrammer.PRJ_SFC);
        assertFalse("Must be false", ret);
    }

    @Test
    public void testIsFlowOk() throws Exception {
        MatchBuilder match = new MatchBuilder();
        OpenflowUtils.addMatchEtherType(match, (long)0x0800);

        FlowBuilder flowbuilder = OpenflowUtils.createFlowBuilder(OpenflowProgrammer.TBL_SFC_INGRESS_TRANSPORT, 0, new BigInteger("9999", 10), "SFC_TEST_FLOW", match, isb);
        boolean ret = FlowConflictDetection.isFlowOk(flowbuilder, new NodeId("openflow:1"), OpenflowProgrammer.TBL_SFC_INGRESS_TRANSPORT, OpenflowProgrammer.PRJ_GBP);
        assertFalse("Must be false", ret);
        ret = FlowConflictDetection.isFlowOk(flowbuilder, new NodeId("openflow:1"), OpenflowProgrammer.TBL_SFC_INGRESS_TRANSPORT, OpenflowProgrammer.PRJ_SFC);
        assertTrue("Must be true", ret);
    }

    @AfterClass
    public static void afterClass() {
        //Clean datastore
        FlowConflictDetection.clearAllFlows();
        LOG.info("afterClass() done");
    }
}
