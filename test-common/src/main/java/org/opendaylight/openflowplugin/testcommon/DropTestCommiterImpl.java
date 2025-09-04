/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.testcommon;

import static java.util.Objects.requireNonNull;

import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.NotificationService;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Instructions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides cbench responder behavior: upon packetIn arrival addFlow action is sent out to device using dataStore
 * strategy (FRM involved).
 */
@Singleton
@Component(service = DropTestCommiter.class, immediate = true)
public final class DropTestCommiterImpl extends AbstractDropTest implements DropTestCommiter {
    private static final Logger LOG = LoggerFactory.getLogger(DropTestCommiterImpl.class);
    private static final TableKey ZERO_TABLE = new TableKey(Uint8.ZERO);
    private static final ThreadLocal<FlowBuilder> BUILDER = ThreadLocal.withInitial(() -> {
        final var cookie = new FlowCookie(Uint64.TEN);
        return new FlowBuilder()
            .setPriority(PRIORITY)
            .setBufferId(BUFFER_ID)
            .setCookie(cookie)
            .setCookieMask(cookie)
            .setTableId(TABLE_ID)
            .setHardTimeout(HARD_TIMEOUT)
            .setIdleTimeout(IDLE_TIMEOUT)
            .setFlags(new FlowModFlags(false, false, false, false, false));
    });

    private final AtomicLong idCounter = new AtomicLong();
    private final DataBroker dataBroker;
    private final NotificationService notificationService;

    private Registration reg = null;

    @Inject
    @Activate
    public DropTestCommiterImpl(@Reference final DataBroker dataBroker,
            @Reference final NotificationService notificationService) {
        this.dataBroker = requireNonNull(dataBroker);
        this.notificationService = requireNonNull(notificationService);
    }

    @PreDestroy
    @Deactivate
    @Override
    public void close() {
        stop();
        super.close();
        LOG.debug("DropTestProvider terminated");
    }

    /**
     * Start listening on packetIn.
     *
     * @return {@code false} if already started
     */
    @Override
    public synchronized boolean start() {
        if (reg != null) {
            return false;
        }
        reg = notificationService.registerListener(PacketReceived.class, this);
        LOG.debug("DropTestProvider started");
        return true;
    }

    /**
     * Stop listening on packetIn.
     *
     * @return {@code false} if already stopped
     */
    @Override
    public synchronized boolean stop() {
        if (reg == null) {
            return false;
        }
        reg.close();
        reg = null;
        LOG.debug("DropTestProvider stopped");
        return true;
    }

    @Override
    protected void processPacket(final InstanceIdentifier<Node> node, final Match match,
            final Instructions instructions) {

        // Finally build our flow
        final FlowBuilder fb = BUILDER.get();
        fb.setMatch(match);
        fb.setInstructions(instructions);
        fb.setId(new FlowId(String.valueOf(fb.hashCode()) + "." + idCounter.getAndIncrement()));

        // Construct the flow instance id
        final var flowInstanceId = node.toIdentifier().toBuilder()
            // That is flow capable, only FlowCapableNodes have tables
            .augmentation(FlowCapableNode.class)
            // In the table identified by TableKey
            .child(Table.class, ZERO_TABLE)
            // A flow identified by flowKey
            .child(Flow.class, new FlowKey(fb.getId()))
            .build();

        final var flow = fb.build();
        final var transaction = dataBroker.newReadWriteTransaction();

        LOG.debug("onPacketReceived - About to write flow {}", flow);
        transaction.mergeParentStructurePut(LogicalDatastoreType.CONFIGURATION, flowInstanceId, flow);
        transaction.commit();
        LOG.debug("onPacketReceived - About to write flow commited");
    }
}
