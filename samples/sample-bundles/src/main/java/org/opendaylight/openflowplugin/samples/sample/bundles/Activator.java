/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.samples.sample.bundles;

import com.google.common.util.concurrent.Futures;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;
import org.opendaylight.controller.md.sal.binding.api.ClusteredDataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification.ModificationType;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.AbstractBrokerAwareActivator;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ConsumerContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareConsumer;
import org.opendaylight.openflowplugin.api.OFConstants;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.AddBundleMessagesInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.AddBundleMessagesInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.ControlBundleInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.ControlBundleInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.SalBundleService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.add.bundle.messages.input.Messages;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.add.bundle.messages.input.MessagesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.add.bundle.messages.input.messages.Message;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.add.bundle.messages.input.messages.MessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.BundleAddFlowCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.bundle.inner.message.grouping.bundle.inner.message.bundle.add.flow._case.AddFlowCaseDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.send.experimenter.input.experimenter.message.of.choice.bundle.add.message.sal.SalAddMessageDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.BundleControlType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.BundleFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.BundleId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sample bundles activator
 */
public class Activator extends AbstractBrokerAwareActivator implements BindingAwareConsumer, ClusteredDataTreeChangeListener<FlowCapableNode>, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(Activator.class);

    private static final BundleId BUNDLE_ID = new BundleId(1L);
    private static final BundleFlags BUNDLE_FLAGS = new BundleFlags(true, true);

    private DataBroker dataBroker;
    private SalBundleService bundleService;

    @Override
    public void close() throws Exception {
        LOG.info("close() passing");
    }

    @Override
    public void onSessionInitialized(ConsumerContext consumerContext) {
        LOG.info("inSessionInitialized() passing");
        dataBroker = consumerContext.getSALService(DataBroker.class);

        final InstanceIdentifier<FlowCapableNode> path = InstanceIdentifier.create(Nodes.class).child(Node.class).
                augmentation(FlowCapableNode.class);
        final DataTreeIdentifier<FlowCapableNode> identifier = new DataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, path);

        dataBroker.registerDataTreeChangeListener(identifier, Activator.this);
        bundleService = consumerContext.getRpcService(SalBundleService.class);
    }

    @Override
    public void onDataTreeChanged(@Nonnull Collection<DataTreeModification<FlowCapableNode>> modifications) {
        for (DataTreeModification modification : modifications) {
            if (modification.getRootNode().getModificationType() == ModificationType.WRITE) {
                LOG.info("node:  {}", modification.getRootPath().getRootIdentifier().firstIdentifierOf(Node.class));

                final NodeRef nodeRef = new NodeRef(modification.getRootPath().getRootIdentifier().firstIdentifierOf(Node.class));

                final ControlBundleInput inputOpen = new ControlBundleInputBuilder()
                        .setNode(nodeRef)
                        .setBundleId(BUNDLE_ID)
                        .setFlags(BUNDLE_FLAGS)
                        .setType(BundleControlType.ONFBCTOPENREQUEST)
                        .build();

                final ControlBundleInput inputCommit = new ControlBundleInputBuilder()
                        .setNode(nodeRef)
                        .setBundleId(BUNDLE_ID)
                        .setFlags(BUNDLE_FLAGS)
                        .setType(BundleControlType.ONFBCTCOMMITREQUEST)
                        .build();

                final List<Message> innerMessages = createMessages(nodeRef);
                final Messages messages = new MessagesBuilder().setMessage(innerMessages).build();
                final AddBundleMessagesInput input = new AddBundleMessagesInputBuilder()
                        .setNode(nodeRef)
                        .setBundleId(BUNDLE_ID)
                        .setFlags(BUNDLE_FLAGS)
                        .setMessages(messages)
                        .build();
                final SalAddMessageDataBuilder dataBuilder = new SalAddMessageDataBuilder();
                dataBuilder.setBundleId(BUNDLE_ID).setFlags(BUNDLE_FLAGS);

                Futures.lazyTransform(bundleService.controlBundle(inputOpen), (openResponse) ->
                        Futures.lazyTransform(bundleService.addBundleMessages(input), (addResponse) ->
                                Futures.lazyTransform(bundleService.controlBundle(inputCommit), (inputResponse) -> {
                                    LOG.info("Bundle status: {}", inputResponse.isSuccessful());
                                    return inputCommit;
                                })));
            }
        }
    }

    @Override
    protected void onBrokerAvailable(BindingAwareBroker bindingAwareBroker, BundleContext bundleContext) {
        LOG.info("onBrokerAvailable() passing");
        bindingAwareBroker.registerConsumer(this);
    }

    private static List<Message> createMessages(NodeRef nodeRef) {
        List<Message> messages  = new ArrayList<>();

        messages.add(new MessageBuilder().setNode(nodeRef).setBundleInnerMessage(
                new BundleAddFlowCaseBuilder()
                        .setAddFlowCaseData(new AddFlowCaseDataBuilder(createFlow()).build()).build()).build());
        return messages;
    }

    static Flow createFlow() {
        // TODO change to add group and flows belonging to this group
        FlowBuilder flowBuilder = new FlowBuilder();
        flowBuilder.setMatch(new MatchBuilder().build());
        flowBuilder.setInstructions(createSendToControllerInstructions().build());
        flowBuilder.setPriority(0);

        FlowKey key = new FlowKey(new FlowId("42"));
        flowBuilder.setBarrier(Boolean.FALSE);
        flowBuilder.setBufferId(OFConstants.OFP_NO_BUFFER);
        BigInteger value = BigInteger.valueOf(10L);
        flowBuilder.setCookie(new FlowCookie(value));
        flowBuilder.setCookieMask(new FlowCookie(value));
        flowBuilder.setHardTimeout(0);
        flowBuilder.setIdleTimeout(0);
        flowBuilder.setInstallHw(false);
        flowBuilder.setStrict(false);
        flowBuilder.setContainerName(null);
        flowBuilder.setFlags(new FlowModFlags(false, false, false, false, true));
        flowBuilder.setId(new FlowId("12"));
        flowBuilder.setTableId((short) 0);
        flowBuilder.setKey(key);
        flowBuilder.setFlowName("Table miss entry");

        return flowBuilder.build();
    }

    private static InstructionsBuilder createSendToControllerInstructions() {
        List<Action> actionList = new ArrayList<>();
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
        List<Instruction> instructions = new ArrayList<>();
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        return isb;
    }

}
