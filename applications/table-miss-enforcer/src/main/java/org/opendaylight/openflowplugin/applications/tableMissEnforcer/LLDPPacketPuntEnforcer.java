/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.tableMissEnforcer;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.common.wait.SimpleTaskRetryLooper;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.OutputPortValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Created by Martin Bobak mbobak@cisco.com on 8/27/14.
 */
public class LLDPPacketPuntEnforcer implements DataChangeListener {
    private static final long STARTUP_LOOP_TICK = 500L;
    private static final int STARTUP_LOOP_MAX_RETRIES = 8;
    private static final short TABLE_ID = (short) 0;
    private static final String LLDP_PUNT_WHOLE_PACKET_FLOW = "LLDP_PUNT_WHOLE_PACKET_FLOW";
    private static final String DEFAULT_FLOW_ID = "42";
    private final SalFlowService flowService;
    private final DataBroker dataBroker;
    private ListenerRegistration<DataChangeListener> dataChangeListenerRegistration;

    public LLDPPacketPuntEnforcer(SalFlowService flowService, DataBroker dataBroker) {
        this.flowService = flowService;
        this.dataBroker = dataBroker;
    }

    public void start() {
        final InstanceIdentifier<FlowCapableNode> path = InstanceIdentifier.create(Nodes.class).child(Node.class).
                augmentation(FlowCapableNode.class);
        SimpleTaskRetryLooper looper = new SimpleTaskRetryLooper(STARTUP_LOOP_TICK, STARTUP_LOOP_MAX_RETRIES);
        try {
            dataChangeListenerRegistration = looper.loopUntilNoException(new Callable<ListenerRegistration<DataChangeListener>>() {
                @Override
                public ListenerRegistration<DataChangeListener> call() throws Exception {
                    return dataBroker.registerDataChangeListener(
                            LogicalDatastoreType.OPERATIONAL,
                            path, LLDPPacketPuntEnforcer.this, AsyncDataBroker.DataChangeScope.BASE);
                }
            });
        } catch (Exception e) {
            throw new IllegalStateException("registerDataChangeListener failed", e);
        }
    }

    public void close() {
        if(dataChangeListenerRegistration != null) {
            dataChangeListenerRegistration.close();
        }
    }

    @Override
    public void onDataChanged(AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {
        final Set<InstanceIdentifier<?>> changedDataKeys = change.getCreatedData().keySet();

        if (changedDataKeys != null) {
            for (InstanceIdentifier<?> key : changedDataKeys) {
                AddFlowInputBuilder addFlowInput = new AddFlowInputBuilder(createFlow());
                addFlowInput.setNode(new NodeRef(key.firstIdentifierOf(Node.class)));
                this.flowService.addFlow(addFlowInput.build());
            }
        }
    }


    protected Flow createFlow() {
        FlowBuilder flowBuilder = new FlowBuilder();
        flowBuilder.setMatch(new MatchBuilder().build());
        flowBuilder.setInstructions(createSendToControllerInstructions().build());
        flowBuilder.setPriority(0);

        FlowKey key = new FlowKey(new FlowId(DEFAULT_FLOW_ID));
        flowBuilder.setBarrier(Boolean.FALSE);
        flowBuilder.setBufferId(OFConstants.OFP_NO_BUFFER);
        BigInteger value = BigInteger.valueOf(10L);
        // BigInteger outputPort = BigInteger.valueOf(65535L);
        flowBuilder.setCookie(new FlowCookie(value));
        flowBuilder.setCookieMask(new FlowCookie(value));
        flowBuilder.setHardTimeout(0);
        flowBuilder.setIdleTimeout(0);
        flowBuilder.setInstallHw(false);
        flowBuilder.setStrict(false);
        flowBuilder.setContainerName(null);
        flowBuilder.setFlags(new FlowModFlags(false, false, false, false, true));
        flowBuilder.setId(new FlowId("12"));
        flowBuilder.setTableId(TABLE_ID);
        flowBuilder.setKey(key);
        flowBuilder.setFlowName(LLDP_PUNT_WHOLE_PACKET_FLOW);

        return flowBuilder.build();
    }

    private static InstructionsBuilder createSendToControllerInstructions() {
        List<Action> actionList = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();

        OutputActionBuilder output = new OutputActionBuilder();
        output.setMaxLength(OFConstants.OFPCML_NO_BUFFER);
        Uri value = new Uri(OutputPortValues.CONTROLLER.toString());
        output.setOutputNodeConnector(value);
        ab.setAction(new OutputActionCaseBuilder().setOutputAction(output.build()).build());
        ab.setOrder(0);
        ab.setKey(new ActionKey(0));
        actionList.add(ab.build());
        // Create an Apply Action
        ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);

        // Wrap our Apply Action in an Instruction
        InstructionBuilder ib = new InstructionBuilder();
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());
        ib.setOrder(0);
        ib.setKey(new InstructionKey(0));

        // Put our Instruction in a list of Instructions
        InstructionsBuilder isb = new InstructionsBuilder();
        List<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

}
