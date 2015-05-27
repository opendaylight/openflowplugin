/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.testcommon;

import java.math.BigInteger;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationService;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.common.wait.SimpleTaskRetryLooper;
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
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * provides cbench responder behavior: upon packetIn arrival addFlow action is sent out to
 * device using dataStore strategy (FRM involved)
 */
public class DropTestCommiter extends AbstractDropTest {
    private static final Logger LOG = LoggerFactory.getLogger(DropTestCommiter.class);
    private static final TableKey ZERO_TABLE = new TableKey((short) 0);
    private DataBroker dataService;

    private static final AtomicLong ID_COUNTER = new AtomicLong();

    private static final ThreadLocal<FlowBuilder> BUILDER = new ThreadLocal<FlowBuilder>() {
        @Override
        protected FlowBuilder initialValue() {
            final FlowBuilder fb = new FlowBuilder();

            fb.setPriority(PRIORITY);
            fb.setBufferId(BUFFER_ID);
            final FlowCookie cookie = new FlowCookie(BigInteger.TEN);
            fb.setCookie(cookie);
            fb.setCookieMask(cookie);

            fb.setTableId(TABLE_ID);
            fb.setHardTimeout(HARD_TIMEOUT);
            fb.setIdleTimeout(IDLE_TIMEOUT);
            fb.setFlags(new FlowModFlags(false, false, false, false, false));
            return fb;
        }
    };

    private NotificationService notificationService;

    private ListenerRegistration<DropTestCommiter> notificationRegistration;

    /**
     * start listening on packetIn
     */
    public void start() {
        final SimpleTaskRetryLooper looper = new SimpleTaskRetryLooper(STARTUP_LOOP_TICK,
                STARTUP_LOOP_MAX_RETRIES);
        try {
            notificationRegistration = looper.loopUntilNoException(new Callable<ListenerRegistration<DropTestCommiter>>() {
                @Override
                public ListenerRegistration<DropTestCommiter> call() throws Exception {
                    return notificationService.registerNotificationListener(DropTestCommiter.this);
                }
            });
        } catch (final Exception e) {
            LOG.warn("DropTest committer notification listener registration fail!");
            LOG.debug("DropTest committer notification listener registration fail! ..", e);
            throw new IllegalStateException("DropTest startup fail! Try again later.", e);
        }
    }

    /**
     * @param dataService the dataService to set
     */
    public void setDataService(final DataBroker dataService) {
        this.dataService = dataService;
    }

    @Override
    protected void processPacket(final InstanceIdentifier<Node> node, final Match match, final Instructions instructions) {

        // Finally build our flow
        final FlowBuilder fb = BUILDER.get();
        fb.setMatch(match);
        fb.setInstructions(instructions);
        fb.setId(new FlowId(String.valueOf(fb.hashCode()) + "." + ID_COUNTER.getAndIncrement()));

        // Construct the flow instance id
        final InstanceIdentifier<Flow> flowInstanceId = node.builder()
                // That is flow capable, only FlowCapableNodes have tables
                .augmentation(FlowCapableNode.class)
                // In the table identified by TableKey
                .child(Table.class, ZERO_TABLE)
                // A flow identified by flowKey
                .child(Flow.class, new FlowKey(fb.getId()))
                .build();

        final Flow flow = fb.build();
        final ReadWriteTransaction transaction = dataService.newReadWriteTransaction();

        if (LOG.isDebugEnabled()) {
            LOG.debug("onPacketReceived - About to write flow {}", flow);
        }
        transaction.put(LogicalDatastoreType.CONFIGURATION, flowInstanceId, flow, true);
        transaction.submit();
        LOG.debug("onPacketReceived - About to write flow commited");
    }

    @Override
    public void close() {
        super.close();
        try {
            LOG.debug("DropTestProvider stopped.");
            if (notificationRegistration != null) {
                notificationRegistration.close();
            }
        } catch (final Exception e) {
            LOG.warn("unregistration of notification listener failed: {}", e.getMessage());
            LOG.debug("unregistration of notification listener failed.. ", e);
        }
    }

    /**
     * @param notificationService
     */
    public void setNotificationService(final NotificationService notificationService) {
        this.notificationService = notificationService;
    }
}
