/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.testcommon;

import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.opendaylight.mdsal.binding.api.NotificationService;
import org.opendaylight.mdsal.binding.api.RpcService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Instructions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides cbench responder behavior: upon packetIn arrival addFlow action is sent out to device using
 * {@link AddFlow} strategy.
 */
@Singleton
@Component(service = DropTestRpcSender.class, immediate = true)
public final class DropTestRpcSenderImpl extends AbstractDropTest implements DropTestRpcSender {
    private static final Logger LOG = LoggerFactory.getLogger(DropTestRpcSenderImpl.class);
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

    private final NotificationService notificationService;
    private final AddFlow addFlow;

    private Registration reg = null;

    @Inject
    @Activate
    public DropTestRpcSenderImpl(@Reference final NotificationService notificationService,
            @Reference final RpcService rpcService) {
        this.notificationService = requireNonNull(notificationService);
        addFlow = rpcService.getRpc(AddFlow.class);
    }

    @PreDestroy
    @Deactivate
    @Override
    public void close() {
        stop();
        super.close();
        LOG.debug("DropTestProvider terminated");
    }

    @Override
    public synchronized boolean start() {
        if (reg != null) {
            return false;
        }
        reg = notificationService.registerListener(PacketReceived.class, this);
        LOG.debug("DropTestProvider started");
        return true;
    }

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
    protected void processPacket(final DataObjectIdentifier<Node> node, final Match match,
            final Instructions instructions) {
        final AddFlowInputBuilder fb = BUILDER.get();

        // Finally build our flow
        fb.setMatch(match);
        fb.setInstructions(instructions);

        // Construct the flow instance id

        fb.setNode(new NodeRef(node));

        // Add flow
        final var flow = fb.build();
        LOG.debug("onPacketReceived - About to write flow (via SalFlowService) {}", flow);
        Futures.addCallback(addFlow.invoke(flow), new FutureCallback<>() {
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
}
