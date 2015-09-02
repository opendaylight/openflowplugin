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
import org.opendaylight.openflowplugin.flowprogrammer.flowconflictdetection.FlowConflictDetection;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Dscp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetFieldCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.field._case.SetFieldBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.GoToTableCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.go.to.table._case.GoToTableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.IpMatchBuilder;

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

    private static ActionBuilder createActionBuilder(int order) {
        ActionBuilder ab = new ActionBuilder();
        ab.setOrder(order);
        ab.setKey(new ActionKey(order));

        return ab;
    }

    private static Action createActionWriteDscp(short dscpVal, final int order) {
        IpMatchBuilder ipMatch = new IpMatchBuilder();
        Dscp dscp = new Dscp(dscpVal);
        ipMatch.setIpDscp(dscp);

        SetFieldCaseBuilder setFieldCase = new SetFieldCaseBuilder();
        setFieldCase.setSetField(
                new SetFieldBuilder().setIpMatch(ipMatch.build())
                .build());

        ActionBuilder ab = createActionBuilder(order);
        ab.setAction(setFieldCase.build());

        return ab.build();
    }

    private static GoToTableBuilder createActionGotoTable(final short toTable) {
        GoToTableBuilder gotoTb = new GoToTableBuilder();
        gotoTb.setTableId(toTable);

        return gotoTb;
    }

    @BeforeClass
    public static void beforeClass() {
        int order = 0;

        /* Initialize InstructionsBuilder */
        List<Action> actionList = new ArrayList<Action>();
        ApplyActionsBuilder aab = new ApplyActionsBuilder();

        // Set Dscp
        actionList.add(createActionWriteDscp((short)11, order++));

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
        GoToTableBuilder gotoTb = createActionGotoTable(OpenflowProgrammer.TBL_SFC_PATH_MAPPER);

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

    private static void addMatchEtherType(MatchBuilder match, final long etherType) {
        EthernetMatchBuilder ethernetMatch = new EthernetMatchBuilder();
        EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(etherType));
        ethernetMatch.setEthernetType(ethTypeBuilder.build());

        match.setEthernetMatch(ethernetMatch.build());
    }

    private static FlowBuilder createFlowBuilder(
            final short table, final int priority, final BigInteger cookieValue,
            final String flowName, MatchBuilder match, InstructionsBuilder isb) {
        FlowBuilder flow = new FlowBuilder();
        String idStr = cookieValue.toString();
        flow.setId(new FlowId(idStr));
        flow.setKey(new FlowKey(new FlowId(idStr)));
        flow.setTableId(table);
        flow.setFlowName(flowName);
        flow.setCookie(new FlowCookie(cookieValue));
        flow.setCookieMask(new FlowCookie(cookieValue));
        flow.setContainerName(null);
        flow.setStrict(false);
        flow.setMatch(match.build());
        flow.setInstructions(isb.build());
        flow.setPriority(priority);
        flow.setHardTimeout(0);
        flow.setIdleTimeout(0);
        flow.setFlags(new FlowModFlags(false, false, false, false, false));
        if (null == flow.isBarrier()) {
            flow.setBarrier(Boolean.FALSE);
        }
        return flow;
    }

    @Test
    public void testSaveFlow() throws Exception {
        MatchBuilder match = new MatchBuilder();
        addMatchEtherType(match, (long)0x0800);

        FlowBuilder flowbuilder = createFlowBuilder(OpenflowProgrammer.TBL_SFC_INGRESS_TRANSPORT, 0, new BigInteger("9999", 10), "SFC_TEST_FLOW", match, isb);
        FlowConflictDetection.saveFlow(flowbuilder.build(), new NodeId("openflow:1"), OpenflowProgrammer.TBL_SFC_INGRESS_TRANSPORT, OpenflowProgrammer.PRJ_SFC);
        assertTrue("Always true", true);
    }

    @Test
    public void testIsFlowOkInProject() throws Exception {
        MatchBuilder match = new MatchBuilder();
        addMatchEtherType(match, (long)0x0800);

        FlowBuilder flowbuilder = createFlowBuilder(OpenflowProgrammer.TBL_SFC_INGRESS_TRANSPORT, 0, new BigInteger("9999", 10), "SFC_TEST_FLOW", match, isb);
        FlowConflictDetection.saveFlow(flowbuilder.build(), new NodeId("openflow:1"), OpenflowProgrammer.TBL_SFC_INGRESS_TRANSPORT, OpenflowProgrammer.PRJ_SFC);
        boolean ret = FlowConflictDetection.isFlowOkInProject(flowbuilder.build(), new NodeId("openflow:1"), OpenflowProgrammer.TBL_SFC_INGRESS_TRANSPORT, OpenflowProgrammer.PRJ_SFC);
        assertFalse("Must be false", ret);
    }

    @Test
    public void testIsFlowOk() throws Exception {
        MatchBuilder match = new MatchBuilder();
        addMatchEtherType(match, (long)0x0800);

        FlowBuilder flowbuilder = createFlowBuilder(OpenflowProgrammer.TBL_SFC_INGRESS_TRANSPORT, 0, new BigInteger("9999", 10), "SFC_TEST_FLOW", match, isb);
        boolean ret = FlowConflictDetection.isFlowOk(flowbuilder.build(), new NodeId("openflow:1"), OpenflowProgrammer.TBL_SFC_INGRESS_TRANSPORT, OpenflowProgrammer.PRJ_GBP);
        assertFalse("Must be false", ret);
        ret = FlowConflictDetection.isFlowOk(flowbuilder.build(), new NodeId("openflow:1"), OpenflowProgrammer.TBL_SFC_INGRESS_TRANSPORT, OpenflowProgrammer.PRJ_SFC);
        assertTrue("Must be true", ret);
    }

    @AfterClass
    public static void afterClass() {
        //Clean datastore
        FlowConflictDetection.clearAllFlows();
        LOG.info("afterClass() done");
    }
}
