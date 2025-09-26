/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.learningswitch.multi;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.NotificationService;
import org.opendaylight.mdsal.binding.api.RpcService;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.openflowplugin.learningswitch.DataTreeChangeListenerRegistrationHolder;
import org.opendaylight.openflowplugin.learningswitch.FlowCommitWrapperImpl;
import org.opendaylight.openflowplugin.learningswitch.WakeupOnNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.TransmitPacket;
import org.opendaylight.yangtools.binding.DataObjectReference;
import org.opendaylight.yangtools.concepts.Registration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
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
@Singleton
@Component(service = { })
public final class LearningSwitchManagerMultiImpl implements DataTreeChangeListenerRegistrationHolder, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(LearningSwitchManagerMultiImpl.class);

    private final Registration dataTreeChangeListenerRegistration;
    private final Registration packetInRegistration;

    @Inject
    @Activate
    public LearningSwitchManagerMultiImpl(@Reference final DataBroker dataBroker,
            @Reference final NotificationService notificationService, @Reference final RpcService rpcService) {
        LOG.debug("start() -->");
        final var dataStoreAccessor = new FlowCommitWrapperImpl(dataBroker);

        final var packetInDispatcher = new PacketInDispatcherImpl();
        final var learningSwitchHandler = new MultipleLearningSwitchHandlerFacadeImpl(
                dataStoreAccessor, rpcService.getRpc(TransmitPacket.class), packetInDispatcher);
        packetInRegistration = notificationService.registerListener(PacketReceived.class, packetInDispatcher);

        dataTreeChangeListenerRegistration = dataBroker.registerLegacyTreeChangeListener(
            LogicalDatastoreType.OPERATIONAL, DataObjectReference.builder(Nodes.class)
                .child(Node.class)
                .augmentation(FlowCapableNode.class)
                .child(Table.class)
                .build(), new WakeupOnNode(learningSwitchHandler));
        LOG.debug("start() <--");
    }

    @PreDestroy
    @Deactivate
    @Override
    public void close() {
        LOG.debug("stop() -->");
        //TODO: remove flow (created in #start())
        packetInRegistration.close();
        dataTreeChangeListenerRegistration.close();
        LOG.debug("stop() <--");
    }

    // FIXME: why?
    @Override
    public Registration getDataTreeChangeListenerRegistration() {
        return dataTreeChangeListenerRegistration;
    }
}
