/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.test;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.infrautils.utils.concurrent.LoggingFutures;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DecNwTtlCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.dec.nw.ttl._case.DecNwTtlBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxReg0;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.dst.choice.grouping.dst.choice.DstNxRegCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionRegLoadNodesNodeTableFlowApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.reg.load.grouping.NxRegLoadBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.reg.load.grouping.nx.reg.load.DstBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.test.rev130819.TestFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.test.rev130819.TestFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.test.rev130819.TestService;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.util.BindingMap;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of TestService.
 *
 * @author msunal
 */
public class Test implements TestService {
    private static final Logger LOG = LoggerFactory.getLogger(Test.class);

    private SalFlowService flowService;

    @Override
    public ListenableFuture<RpcResult<TestFlowOutput>> testFlow(final TestFlowInput input) {
        // Construct the flow instance id
        final InstanceIdentifier<Node> flowInstanceId = InstanceIdentifier
                .builder(Nodes.class) // File under nodes
                // A particular node identified by nodeKey
                .child(Node.class, new NodeKey(new NodeId("openflow:1"))).build();

        pushFlowViaRpc(new AddFlowInputBuilder()
            .setPriority(Uint16.TWO)
            .setMatch(new MatchBuilder()
                .setLayer3Match(new Ipv4MatchBuilder().setIpv4Destination(new Ipv4Prefix("10.0.0.1/24")).build())
                .setEthernetMatch(new EthernetMatchBuilder()
                    .setEthernetType(new EthernetTypeBuilder().setType(new EtherType(Uint32.valueOf(0x0800))).build())
                    .build())
                // .addAugmentation(createNxMatchAugment().getAugmentationObject())
                .build())
            .setInstructions(new InstructionsBuilder()
                .setInstruction(BindingMap.of(new InstructionBuilder()
                    .setOrder(0)
                    .setInstruction(new ApplyActionsCaseBuilder()
                        .setApplyActions(new ApplyActionsBuilder()
                            .setAction(BindingMap.of(new ActionBuilder()
                                .setOrder(0)
                                .setAction(new DecNwTtlCaseBuilder().setDecNwTtl(new DecNwTtlBuilder().build()).build())
                                .build(), new ActionBuilder()
                                // base part
                                .setOrder(1)
                                .setAction(new NxActionRegLoadNodesNodeTableFlowApplyActionsCaseBuilder()
                                    // vendor part
                                    .setNxRegLoad(new NxRegLoadBuilder()
                                        .setDst(new DstBuilder()
                                            .setDstChoice(new DstNxRegCaseBuilder().setNxReg(NxmNxReg0.class).build())
                                            .setStart(Uint16.ZERO)
                                            .setEnd(Uint16.valueOf(5))
                                            .build())
                                        .setValue(Uint64.valueOf(55L))
                                        .build())
                                    .build())
                                .build()))
                            .build())
                        .build())
                    .build()))
                .build())
            .setBarrier(Boolean.FALSE)
            .setCookie(new FlowCookie(Uint64.TEN))
            .setCookieMask(new FlowCookie(Uint64.TEN))
            .setHardTimeout(Uint16.ZERO)
            .setIdleTimeout(Uint16.ZERO)
            .setInstallHw(false)
            .setStrict(false)
            .setContainerName(null)
            .setFlags(new FlowModFlags(false, false, false, false, true))
            .setTableId(Uint8.ZERO)
            .setFlowName("NiciraFLOW")
            .setNode(new NodeRef(flowInstanceId))
            .build());

        return Futures.immediateFuture(RpcResultBuilder.<TestFlowOutput>status(true).build());
    }

    private void pushFlowViaRpc(final AddFlowInput addFlowInput) {
        if (flowService != null) {
            LoggingFutures.addErrorLogging(flowService.addFlow(addFlowInput), LOG, "addFlow");
        }
    }

    public void setFlowService(final SalFlowService flowService) {
        this.flowService = flowService;
    }
}
