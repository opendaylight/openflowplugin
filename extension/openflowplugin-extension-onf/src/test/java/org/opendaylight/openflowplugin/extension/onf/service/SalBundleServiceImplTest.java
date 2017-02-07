/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.extension.onf.service;

import com.google.common.util.concurrent.SettableFuture;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.yang.gen.v1.urn.opendaylight.experimenter.message.service.rev151020.SalExperimenterMessageService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.experimenter.message.service.rev151020.SendExperimenterInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.AddBundleMessagesInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.AddBundleMessagesInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.ControlBundleInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.ControlBundleInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.SalBundleService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.add.bundle.messages.input.Messages;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.add.bundle.messages.input.MessagesBuilder;
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

/**
 * Test for {@link org.opendaylight.openflowplugin.extension.onf.service.SalBundleServiceImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SalBundleServiceImplTest {

    private static final NodeRef NODE_REF = new NodeRef(InstanceIdentifier.create(Nodes.class)
            .child(Node.class, new NodeKey(new NodeId("openflow:1"))));
    private static final BundleId BUNDLE_ID = new BundleId(1L);
    private static final BundleFlags BUNDLE_FLAGS = new BundleFlags(true, false);

    private SalBundleService service;
    @Mock
    private SalExperimenterMessageService experimenterMessageService;
    @Mock
    private List<BundleProperty> properties;

    @Before
    public void setUp() {
        service = new SalBundleServiceImpl(experimenterMessageService);
    }

    @Test
    public void testControlBundle() {
        final ControlBundleInput input = new ControlBundleInputBuilder()
                .setNode(NODE_REF)
                .setBundleId(BUNDLE_ID)
                .setFlags(BUNDLE_FLAGS)
                .setType(BundleControlType.ONFBCTOPENREQUEST)
                .setBundleProperty(properties)
                .build();
        final SendExperimenterInputBuilder experimenterBuilder = new SendExperimenterInputBuilder();
        experimenterBuilder.setNode(NODE_REF);
        experimenterBuilder.setExperimenterMessageOfChoice(new BundleControlSalBuilder()
                .setSalControlData(new SalControlDataBuilder(input).build())
                .build());
        service.controlBundle(input);
        Mockito.verify(experimenterMessageService).sendExperimenter(experimenterBuilder.build());
    }

    @Test
    public void testAddBundleMessages() throws Exception {
        final List<Messages> innerMessages = createMessages();
        final AddBundleMessagesInput input = new AddBundleMessagesInputBuilder()
                .setNode(NODE_REF)
                .setBundleId(BUNDLE_ID)
                .setFlags(BUNDLE_FLAGS)
                .setBundleProperty(properties)
                .setMessages(innerMessages)
                .build();
        final SalAddMessageDataBuilder dataBuilder = new SalAddMessageDataBuilder();
        dataBuilder.setBundleId(BUNDLE_ID).setFlags(BUNDLE_FLAGS).setBundleProperty(properties);

        final BundleAddMessageSalBuilder addMessageBuilder = new BundleAddMessageSalBuilder();
        final SendExperimenterInputBuilder experimenterBuilder = new SendExperimenterInputBuilder()
                .setNode(NODE_REF);
        Mockito.when(experimenterMessageService.sendExperimenter(Matchers.any())).thenReturn(SettableFuture.create());
        service.addBundleMessages(input);
        for (Messages msg : innerMessages) {
            Mockito.verify(experimenterMessageService)
                    .sendExperimenter(
                            experimenterBuilder.setExperimenterMessageOfChoice(
                                    addMessageBuilder
                                            .setSalAddMessageData(
                                                    dataBuilder.setBundleInnerMessage(msg.getBundleInnerMessage()).build()
                                            ).build()
                            ).build()
                    );
        }
    }

    private static List<Messages> createMessages() {
        List<Messages> messages  = new ArrayList<>();
        messages.add(new MessagesBuilder().setBundleInnerMessage(new BundleAddFlowCaseBuilder().build()).build());
        messages.add(new MessagesBuilder().setBundleInnerMessage(new BundleUpdateFlowCaseBuilder().build()).build());
        messages.add(new MessagesBuilder().setBundleInnerMessage(new BundleRemoveFlowCaseBuilder().build()).build());
        messages.add(new MessagesBuilder().setBundleInnerMessage(new BundleAddGroupCaseBuilder().build()).build());
        messages.add(new MessagesBuilder().setBundleInnerMessage(new BundleUpdateGroupCaseBuilder().build()).build());
        messages.add(new MessagesBuilder().setBundleInnerMessage(new BundleRemoveGroupCaseBuilder().build()).build());
        messages.add(new MessagesBuilder().setBundleInnerMessage(new BundleUpdatePortCaseBuilder().build()).build());
        return messages;
    }

}