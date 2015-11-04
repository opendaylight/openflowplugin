/**
 * Copyright (c) 2014 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor: usha.m.s@ericsson.com
 */
package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetVlanIdActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.vlan.id.action._case.SetVlanIdActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowTableRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Instructions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInputBuilder;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.DataContainer;

/**
 * test for {@link org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.FlowConvertor}
 */
public class FlowConverterTest {


    @Test
    public void testCloneAndAugmentFlowWithSetVlanId() {
        MockFlow mockFlow = new MockFlow();
        Action action1 = createAction(
            new SetVlanIdActionCaseBuilder().setSetVlanIdAction(
                new SetVlanIdActionBuilder().setVlanId(new VlanId(10)).build())
                .build(),
            0);

        mockFlow.setMatch(new MatchBuilder().setEthernetMatch(createEthernetMatch()).build());
        mockFlow.setInstructions(toApplyInstruction(Collections.singletonList(action1)));

        List<FlowModInputBuilder> flowModInputBuilders =
            FlowConvertor.toFlowModInputs(mockFlow, OFConstants.OFP_VERSION_1_3, BigInteger.ONE);
        Assert.assertEquals(2, flowModInputBuilders.size());

    }

    private static Action createAction(final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionCase,
        final int order) {
        Action action = new ActionBuilder().setOrder(order).setAction(actionCase).build();
        return action;
    }

    private static EthernetMatch createEthernetMatch() {
        EthernetMatchBuilder ethernetMatchBuilder = new EthernetMatchBuilder();
        ethernetMatchBuilder.setEthernetType(new EthernetTypeBuilder().setType(new EtherType(33024L)).build());
        return ethernetMatchBuilder.build();
    }

    private static Instructions toApplyInstruction(
        final List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action> actions) {
        return new InstructionsBuilder()
            .setInstruction(
                Collections.singletonList(
                    new InstructionBuilder()
                        .setOrder(0)
                        .setInstruction(
                            new ApplyActionsCaseBuilder()
                                .setApplyActions((new ApplyActionsBuilder()).setAction(actions).build())
                                .build()
                        ).build())
            ).build();
    }

    private static Flow createFlowWithActions(final Action...actions) {
        MockFlow mockFlow = new MockFlow();
        mockFlow.setInstructions(toApplyInstruction(Arrays.asList(actions)));
        return mockFlow;
    }

    private static class MockFlow implements AddFlowInput {
        private Instructions instructions;
        private Match match;

        public void setInstructions(final Instructions instructions) {
            this.instructions = instructions;
        }

        public void setMatch(final Match match) {
            this.match = match;
        }


        @Override
        public FlowRef getFlowRef() {
            return null;
        }

        @Override
        public <E extends Augmentation<AddFlowInput>> E getAugmentation(final Class<E> augmentationType) {
            return null;
        }

        @Override
        public FlowTableRef getFlowTable() {
            return null;
        }

        @Override
        public Match getMatch() {
            return match;
        }

        @Override
        public Instructions getInstructions() {
            return instructions;
        }

        @Override
        public String getContainerName() {
            return null;
        }

        @Override
        public FlowCookie getCookieMask() {
            return null;
        }

        @Override
        public Long getBufferId() {
            return null;
        }

        @Override
        public BigInteger getOutPort() {
            return null;
        }

        @Override
        public Long getOutGroup() {
            return null;
        }

        @Override
        public FlowModFlags getFlags() {
            return null;
        }

        @Override
        public String getFlowName() {
            return null;
        }

        @Override
        public Boolean isInstallHw() {
            return null;
        }

        @Override
        public Boolean isBarrier() {
            return null;
        }

        @Override
        public Boolean isStrict() {
            return null;
        }

        @Override
        public Boolean isCollectStats() {
            return null;
        }

        @Override
        public Integer getPriority() {
            return null;
        }

        @Override
        public Integer getIdleTimeout() {
            return null;
        }

        @Override
        public Integer getHardTimeout() {
            return null;
        }

        @Override
        public FlowCookie getCookie() {
            return null;
        }

        @Override
        public Short getTableId() {
            return null;
        }

        @Override
        public NodeRef getNode() {
            return null;
        }

        @Override
        public Uri getTransactionUri() {
            return null;
        }

        @Override
        public Class<? extends DataContainer> getImplementedInterface() {
            return null;
        }
    }

}
