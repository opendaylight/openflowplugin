/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.testcommon;

import java.math.BigInteger;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.NotificationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * provides cbench responder behavior: upon packetIn arrival addFlow action is sent out to 
 * device using dataStore strategy (FRM involved)
 */
public class DropTestCommiter extends AbstractDropTest {
    private final static Logger LOG = LoggerFactory.getLogger(DropTestCommiter.class);
    
    private DataBroker dataService;

    private static final ThreadLocal<FlowBuilder> BUILDER = new ThreadLocal<FlowBuilder>() {
        @Override
        protected FlowBuilder initialValue() {
            final FlowBuilder fb = new FlowBuilder();

            fb.setPriority(4);
            fb.setBufferId(0L);
            final FlowCookie cookie = new FlowCookie(BigInteger.valueOf(10));
            fb.setCookie(cookie);
            fb.setCookieMask(cookie);

            fb.setTableId((short) 0);
            fb.setHardTimeout(300);
            fb.setIdleTimeout(240);
            fb.setFlags(new FlowModFlags(false, false, false, false, false));
            return fb;
        }
    };

    private NotificationProviderService notificationService;

    private ListenerRegistration<NotificationListener> notificationRegistration;
    
    /**
     * start listening on packetIn
     */
    public void start() {
        notificationRegistration = notificationService.registerNotificationListener(this);
    }
    
    /**
     * @param dataService the dataService to set
     */
    public void setDataService(DataBroker dataService) {
        this.dataService = dataService;
    }

    @Override
    protected void processPacket(final NodeKey node, final Match match, final Instructions instructions) {

        // Finally build our flow
        final FlowBuilder fb = BUILDER.get();
        fb.setMatch(match);
        fb.setInstructions(instructions);
        fb.setId(new FlowId(String.valueOf(fb.hashCode())));

        // Construct the flow instance id
        final InstanceIdentifier<Flow> flowInstanceId =
                InstanceIdentifier.builder(Nodes.class) // File under nodes
                        .child(Node.class, node) // A particular node identified by nodeKey
                        .augmentation(FlowCapableNode.class) // That is flow capable, only FlowCapableNodes have tables
                        .child(Table.class, new TableKey((short) 0)) // In the table identified by TableKey
                        .child(Flow.class, new FlowKey(fb.getId())) // A flow identified by flowKey
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
        try {
            LOG.debug("DropTestProvider stopped.");
            if (notificationRegistration != null) {
                notificationRegistration.close();
            }
        } catch (Exception e) {
            LOG.error("unregistration of notification listener failed", e);
        }
    }

    /**
     * @param notificationService
     */
    public void setNotificationService(NotificationProviderService notificationService) {
        this.notificationService = notificationService;
    }
}
