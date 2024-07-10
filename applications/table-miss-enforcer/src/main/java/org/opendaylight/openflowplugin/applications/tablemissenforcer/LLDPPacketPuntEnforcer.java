/*
 * Copyright (c) 2015, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.tablemissenforcer;

import static java.util.Objects.requireNonNull;

import java.util.List;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.opendaylight.infrautils.utils.concurrent.LoggingFutures;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataObjectModification.ModificationType;
import org.opendaylight.mdsal.binding.api.DataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.binding.api.RpcService;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.applications.deviceownershipservice.DeviceOwnershipService;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.OutputPortValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.binding.util.BindingMap;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Component(service = { })
public final class LLDPPacketPuntEnforcer implements AutoCloseable, DataTreeChangeListener<FlowCapableNode> {
    private static final Logger LOG = LoggerFactory.getLogger(LLDPPacketPuntEnforcer.class);
    private static final Uint8 TABLE_ID = Uint8.ZERO;
    private static final String LLDP_PUNT_WHOLE_PACKET_FLOW = "LLDP_PUNT_WHOLE_PACKET_FLOW";
    private static final String DEFAULT_FLOW_ID = "42";

    private final DeviceOwnershipService deviceOwnershipService;
    private final Registration listenerRegistration;
    private final AddFlow addFlow;

    @Inject
    @Activate
    public LLDPPacketPuntEnforcer(@Reference final DataBroker dataBroker,
            @Reference final DeviceOwnershipService deviceOwnershipService,
            @Reference final RpcService rpcService) {
        this.deviceOwnershipService = requireNonNull(deviceOwnershipService);
        addFlow = rpcService.getRpc(AddFlow.class);
        listenerRegistration = dataBroker.registerTreeChangeListener(
            DataTreeIdentifier.of(LogicalDatastoreType.OPERATIONAL,
                InstanceIdentifier.create(Nodes.class).child(Node.class).augmentation(FlowCapableNode.class)),
            this);
    }

    @PreDestroy
    @Deactivate
    @Override
    public void close() {
        listenerRegistration.close();
    }

    @Override
    public void onDataTreeChanged(final List<DataTreeModification<FlowCapableNode>> modifications) {
        for (var modification : modifications) {
            if (modification.getRootNode().modificationType() == ModificationType.WRITE) {
                final var nodeId = modification.getRootPath().path().firstKeyOf(Node.class).getId().getValue();
                if (deviceOwnershipService.isEntityOwned(nodeId)) {
                    LoggingFutures.addErrorLogging(addFlow.invoke(new AddFlowInputBuilder(createFlow())
                        .setNode(new NodeRef(modification.getRootPath().path().firstIdentifierOf(Node.class)))
                        .build()), LOG, "addFlow");
                } else {
                    LOG.debug("Node {} is not owned by this controller, so skip adding LLDP table miss flow", nodeId);
                }
            }
        }
    }

    static Flow createFlow() {
        return new FlowBuilder()
            .setMatch(new MatchBuilder().build())
            .setInstructions(createSendToControllerInstructions().build())
            .setPriority(Uint16.ZERO)
            .setBarrier(Boolean.FALSE)
            .setBufferId(OFConstants.OFP_NO_BUFFER)
            .setCookie(new FlowCookie(Uint64.TEN))
            .setCookieMask(new FlowCookie(Uint64.TEN))
            .setHardTimeout(Uint16.ZERO)
            .setIdleTimeout(Uint16.ZERO)
            .setInstallHw(false)
            .setStrict(false)
            .setContainerName(null)
            .setFlags(new FlowModFlags(false, false, false, false, true))
            .setId(new FlowId("12"))
            .setTableId(TABLE_ID)
            .withKey(new FlowKey(new FlowId(DEFAULT_FLOW_ID)))
            .setFlowName(LLDP_PUNT_WHOLE_PACKET_FLOW)
            .build();
    }

    private static InstructionsBuilder createSendToControllerInstructions() {
        return new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                // Wrap our Apply Action in an Instruction
                .setInstruction(new ApplyActionsCaseBuilder()
                    .setApplyActions(new ApplyActionsBuilder()
                        // Create an Apply Action
                        .setAction(BindingMap.of(new ActionBuilder()
                            .setAction(new OutputActionCaseBuilder()
                                .setOutputAction(new OutputActionBuilder()
                                    .setMaxLength(OFConstants.OFPCML_NO_BUFFER)
                                    .setOutputNodeConnector(new Uri(OutputPortValues.CONTROLLER.toString()))
                                    .build())
                                .build())
                            .setOrder(0)
                            .build()))
                        .build())
                    .build())
                .setOrder(0)
                .build()));
    }
}
