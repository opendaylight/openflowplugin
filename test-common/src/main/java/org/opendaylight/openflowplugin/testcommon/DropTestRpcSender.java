/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.testcommon;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import org.opendaylight.mdsal.binding.api.NotificationService;
import org.opendaylight.openflowplugin.impl.services.sal.SalFlowRpcs;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Instructions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides cbench responder behavior: upon packetIn arrival addFlow action is sent out to
 * device using {@link SalFlowRpcs} strategy.
 */
public class DropTestRpcSender extends AbstractDropTest {
    private static final Logger LOG = LoggerFactory.getLogger(DropTestRpcSender.class);

    private SalFlowRpcs flowService;

    public void setFlowService(final SalFlowRpcs flowService) {
        this.flowService = flowService;
    }

    private static final ThreadLocal<AddFlowInputBuilder> BUILDER = ThreadLocal.withInitial(() -> {
        final var cookie = new FlowCookie(Uint64.TEN);
        return new AddFlowInputBuilder()
            .setPriority(PRIORITY)
            .setBufferId(BUFFER_ID)
            .setCookie(cookie)
            .setCookieMask(cookie)
            .setTableId(TABLE_ID)
            .setHardTimeout(HARD_TIMEOUT)
            .setIdleTimeout(IDLE_TIMEOUT)
            .setFlags(new FlowModFlags(false, false, false, false, false));
    });

    private NotificationService notificationService;

    private Registration notificationRegistration;

    /**
     * Start listening on packetIn.
     */
    public void start() {
        notificationRegistration = notificationService.registerListener(PacketReceived.class, this);
    }

    @Override
    protected void processPacket(final InstanceIdentifier<Node> node, final Match match,
            final Instructions instructions) {
        final AddFlowInputBuilder fb = BUILDER.get();

        // Finally build our flow
        fb.setMatch(match);
        fb.setInstructions(instructions);

        // Construct the flow instance id

        fb.setNode(new NodeRef(node));

        // Add flow
        final AddFlowInput flow = fb.build();
        LOG.debug("onPacketReceived - About to write flow (via SalFlowService) {}", flow);
        ListenableFuture<RpcResult<AddFlowOutput>> result = flowService.getRpcClassToInstanceMap()
            .getInstance(AddFlow.class).invoke(flow);
        Futures.addCallback(result, new FutureCallback<RpcResult<AddFlowOutput>>() {
            @Override
            public void onSuccess(final RpcResult<AddFlowOutput> result) {
                countFutureSuccess();
            }

            @Override
            public void onFailure(final Throwable throwable) {
                countFutureError();
            }
        }, MoreExecutors.directExecutor());
    }

    public void setNotificationService(final NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public void close() {
        super.close();
        LOG.debug("DropTestProvider stopped.");
        if (notificationRegistration != null) {
            notificationRegistration.close();
        }
    }
}
