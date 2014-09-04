/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.testcommon;

import java.math.BigInteger;

import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Instructions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
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
 * device using {@link SalFlowService} strategy
 */
public class DropTestRpcSender extends AbstractDropTest {
    private final static Logger LOG = LoggerFactory.getLogger(DropTestRpcSender.class);

    private SalFlowService flowService;
    
    /**
     * @param flowService the flowService to set
     */
    public void setFlowService(SalFlowService flowService) {
        this.flowService = flowService;
    }

    private static final ThreadLocal<AddFlowInputBuilder> BUILDER = new ThreadLocal<AddFlowInputBuilder>() {
        @Override
        protected AddFlowInputBuilder initialValue() {
            final AddFlowInputBuilder fb = new AddFlowInputBuilder();

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

    @Override
    protected void processPacket(final NodeKey node, final Match match, final Instructions instructions) {
        final AddFlowInputBuilder fb = BUILDER.get();

        // Finally build our flow
        fb.setMatch(match);
        fb.setInstructions(instructions);
        //fb.setId(new FlowId(Long.toString(fb.hashCode)));

        // Construct the flow instance id
        final InstanceIdentifier<Node> flowInstanceId = InstanceIdentifier
                .builder(Nodes.class) // File under nodes
                .child(Node.class, node).toInstance(); // A particular node identified by nodeKey
        fb.setNode(new NodeRef(flowInstanceId));

        // Add flow
        AddFlowInput flow = fb.build();
        if (LOG.isDebugEnabled()) {
            LOG.debug("onPacketReceived - About to write flow (via SalFlowService) {}", flow);
        }
        flowService.addFlow(flow);
    }

    /**
     * @param notificationService
     */
    public void setNotificationService(NotificationProviderService notificationService) {
        this.notificationService = notificationService;
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
}
