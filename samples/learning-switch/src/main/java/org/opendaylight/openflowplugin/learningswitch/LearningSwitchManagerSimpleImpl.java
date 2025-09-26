/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.learningswitch;

import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.NotificationService;
import org.opendaylight.mdsal.binding.api.RpcService;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.TransmitPacket;
import org.opendaylight.yangtools.binding.DataObjectReference;
import org.opendaylight.yangtools.concepts.Registration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listens to packetIn notification.
 * <ul>
 * <li>in HUB mode simply floods all switch ports (except ingress port)</li>
 * <li>in LSWITCH mode collects source MAC address of packetIn and bind it with ingress port.
 * If target MAC address is already bound then a flow is created (for direct communication between
 * corresponding MACs)</li>
 * </ul>
 */
public final class LearningSwitchManagerSimpleImpl implements DataTreeChangeListenerRegistrationHolder, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(LearningSwitchManagerSimpleImpl.class);

    private final Registration dataTreeChangeListenerRegistration;
    private final Registration packetInRegistration;

    public LearningSwitchManagerSimpleImpl(final DataBroker dataBroker, final NotificationService notificationService,
            final RpcService rpcService) {
        LOG.debug("start() -->");
        final var dataStoreAccessor = new FlowCommitWrapperImpl(dataBroker);

        final var learningSwitchHandler = new LearningSwitchHandlerSimpleImpl(dataStoreAccessor,
            rpcService.getRpc(TransmitPacket.class), this);
        packetInRegistration = notificationService.registerListener(PacketReceived.class, learningSwitchHandler);

        dataTreeChangeListenerRegistration = dataBroker.registerLegacyTreeChangeListener(
            LogicalDatastoreType.OPERATIONAL, DataObjectReference.builder(Nodes.class)
                .child(Node.class)
                .augmentation(FlowCapableNode.class)
                .child(Table.class)
                .build(), new WakeupOnNode(learningSwitchHandler));
        LOG.debug("start() <--");
    }

    @Override
    public void close() {
        LOG.debug("stop() -->");
        //TODO: remove flow (created in #start())
        packetInRegistration.close();
        dataTreeChangeListenerRegistration.close();
        LOG.debug("stop() <--");
    }

    @Override
    public Registration getDataTreeChangeListenerRegistration() {
        return dataTreeChangeListenerRegistration;
    }
}
