/*
 * Copyright (c) 2015, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.tablemissenforcer;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import org.opendaylight.infrautils.utils.concurrent.LoggingFutures;
import org.opendaylight.mdsal.binding.api.ClusteredDataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataObjectModification.ModificationType;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.applications.deviceownershipservice.DeviceOwnershipService;
import org.opendaylight.openflowplugin.common.wait.SimpleTaskRetryLooper;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.util.BindingMap;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LLDPPacketPuntEnforcer implements AutoCloseable, ClusteredDataTreeChangeListener<FlowCapableNode> {
    private static final Logger LOG = LoggerFactory.getLogger(LLDPPacketPuntEnforcer.class);
    private static final long STARTUP_LOOP_TICK = 500L;
    private static final int STARTUP_LOOP_MAX_RETRIES = 8;
    private static final Uint8 TABLE_ID = Uint8.ZERO;
    private static final String LLDP_PUNT_WHOLE_PACKET_FLOW = "LLDP_PUNT_WHOLE_PACKET_FLOW";
    private static final String DEFAULT_FLOW_ID = "42";
    private final SalFlowService flowService;
    private final DataBroker dataBroker;
    private final DeviceOwnershipService deviceOwnershipService;
    private ListenerRegistration<?> listenerRegistration;

    public LLDPPacketPuntEnforcer(final SalFlowService flowService, final DataBroker dataBroker,
            final DeviceOwnershipService deviceOwnershipService) {
        this.flowService = flowService;
        this.dataBroker = dataBroker;
        this.deviceOwnershipService = requireNonNull(deviceOwnershipService, "DeviceOwnershipService can not be null");
    }

    @SuppressWarnings("IllegalCatch")
    public void start() {
        final InstanceIdentifier<FlowCapableNode> path = InstanceIdentifier.create(Nodes.class).child(Node.class)
                .augmentation(FlowCapableNode.class);
        final DataTreeIdentifier<FlowCapableNode> identifier = DataTreeIdentifier.create(
                LogicalDatastoreType.OPERATIONAL, path);
        SimpleTaskRetryLooper looper = new SimpleTaskRetryLooper(STARTUP_LOOP_TICK, STARTUP_LOOP_MAX_RETRIES);
        try {
            listenerRegistration = looper.loopUntilNoException(() ->
                dataBroker.registerDataTreeChangeListener(identifier, LLDPPacketPuntEnforcer.this));
        } catch (Exception e) {
            throw new IllegalStateException("registerDataTreeChangeListener failed", e);
        }
    }

    @Override
    public void close() {
        if (listenerRegistration != null) {
            listenerRegistration.close();
        }
    }

    @Override
    public void onDataTreeChanged(final Collection<DataTreeModification<FlowCapableNode>> modifications) {
        for (DataTreeModification<FlowCapableNode> modification : modifications) {
            if (modification.getRootNode().getModificationType() == ModificationType.WRITE) {
                String nodeId = modification.getRootPath().getRootIdentifier()
                        .firstKeyOf(Node.class).getId().getValue();
                if (deviceOwnershipService.isEntityOwned(nodeId)) {
                    AddFlowInputBuilder addFlowInput = new AddFlowInputBuilder(createFlow());
                    addFlowInput.setNode(new NodeRef(modification.getRootPath()
                            .getRootIdentifier().firstIdentifierOf(Node.class)));
                    LoggingFutures.addErrorLogging(this.flowService.addFlow(addFlowInput.build()), LOG, "addFlow");
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
