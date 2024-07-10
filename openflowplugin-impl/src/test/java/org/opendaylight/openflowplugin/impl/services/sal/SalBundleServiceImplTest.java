/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.sal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.util.concurrent.SettableFuture;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.yang.gen.v1.urn.opendaylight.experimenter.message.service.rev151020.SendExperimenter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.experimenter.message.service.rev151020.SendExperimenterInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.AddBundleMessagesInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.ControlBundleInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.add.bundle.messages.input.MessagesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.add.bundle.messages.input.messages.Message;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.add.bundle.messages.input.messages.MessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.BundleAddFlowCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.BundleAddGroupCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.BundleRemoveFlowCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.BundleRemoveGroupCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.BundleUpdateFlowCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.BundleUpdateGroupCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.BundleUpdatePortCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.send.experimenter.input.experimenter.message.of.choice.BundleAddMessageSalBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.send.experimenter.input.experimenter.message.of.choice.BundleControlSalBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.send.experimenter.input.experimenter.message.of.choice.bundle.add.message.sal.SalAddMessageDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.send.experimenter.input.experimenter.message.of.choice.bundle.control.sal.SalControlDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.BundleControlType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.BundleFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.BundleId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.bundle.common.grouping.BundleProperty;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.Uint32;

@RunWith(MockitoJUnitRunner.class)
public class SalBundleServiceImplTest {
    private static final NodeRef NODE_REF = new NodeRef(InstanceIdentifier.create(Nodes.class)
            .child(Node.class, new NodeKey(new NodeId("openflow:1"))).toIdentifier());
    private static final BundleId BUNDLE_ID = new BundleId(Uint32.ONE);
    private static final BundleFlags BUNDLE_FLAGS = new BundleFlags(true, false);

    @Mock
    private SendExperimenter sendExperimenter;
    @Mock
    private List<BundleProperty> properties;

    @Test
    public void testControlBundle() {
        final var service = new ControlBundleImpl(sendExperimenter);

        final var input = new ControlBundleInputBuilder()
                .setNode(NODE_REF)
                .setBundleId(BUNDLE_ID)
                .setFlags(BUNDLE_FLAGS)
                .setType(BundleControlType.ONFBCTOPENREQUEST)
                .setBundleProperty(properties)
                .build();
        when(sendExperimenter.invoke(any())).thenReturn(SettableFuture.create());
        service.invoke(input);
        verify(sendExperimenter).invoke(new SendExperimenterInputBuilder()
            .setNode(NODE_REF)
            .setExperimenterMessageOfChoice(new BundleControlSalBuilder()
                .setSalControlData(new SalControlDataBuilder(input).build())
                .build())
            .build());
    }

    @Test
    public void testAddBundleMessages() {
        final var service = new AddBundleMessagesImpl(sendExperimenter);

        final var innerMessages = createMessages();
        final var messages = new MessagesBuilder().setMessage(innerMessages).build();
        final var input = new AddBundleMessagesInputBuilder()
                .setNode(NODE_REF)
                .setBundleId(BUNDLE_ID)
                .setFlags(BUNDLE_FLAGS)
                .setBundleProperty(properties)
                .setMessages(messages)
                .build();
        final var dataBuilder = new SalAddMessageDataBuilder();
        dataBuilder.setBundleId(BUNDLE_ID).setFlags(BUNDLE_FLAGS).setBundleProperty(properties);

        final var addMessageBuilder = new BundleAddMessageSalBuilder();
        final var experimenterBuilder = new SendExperimenterInputBuilder()
                .setNode(NODE_REF);
        when(sendExperimenter.invoke(any())).thenReturn(SettableFuture.create());
        service.invoke(input);
        for (var msg : innerMessages) {
            verify(sendExperimenter).invoke(experimenterBuilder
                .setExperimenterMessageOfChoice(addMessageBuilder
                    .setSalAddMessageData(
                        dataBuilder.setNode(NODE_REF).setBundleInnerMessage(
                            msg.getBundleInnerMessage())
                        .build())
                    .build())
                .build());
        }
    }

    private static List<Message> createMessages() {
        final var builder = new MessageBuilder().setNode(NODE_REF);
        return List.of(
            builder.setBundleInnerMessage(new BundleAddFlowCaseBuilder().build()).build(),
            builder.setBundleInnerMessage(new BundleUpdateFlowCaseBuilder().build()).build(),
            builder.setBundleInnerMessage(new BundleRemoveFlowCaseBuilder().build()).build(),
            builder.setBundleInnerMessage(new BundleAddGroupCaseBuilder().build()).build(),
            builder.setBundleInnerMessage(new BundleUpdateGroupCaseBuilder().build()).build(),
            builder.setBundleInnerMessage(new BundleRemoveGroupCaseBuilder().build()).build(),
            builder.setBundleInnerMessage(new BundleUpdatePortCaseBuilder().build()).build());
    }
}
