/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.testcommon;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import java.math.BigInteger;
import java.util.concurrent.Callable;
import org.opendaylight.controller.md.sal.binding.api.NotificationService;
import org.opendaylight.openflowplugin.common.wait.SimpleTaskRetryLooper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Instructions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * provides cbench responder behavior: upon packetIn arrival addFlow action is sent out to
 * device using {@link SalFlowService} strategy
 */
public class DropTestRpcSender extends AbstractDropTest {
    private static final Logger LOG = LoggerFactory.getLogger(DropTestRpcSender.class);

    private SalFlowService flowService;

    /**
     * @param flowService the flowService to set
     */
    public void setFlowService(final SalFlowService flowService) {
        this.flowService = flowService;
    }

    private static final ThreadLocal<AddFlowInputBuilder> BUILDER = new ThreadLocal<AddFlowInputBuilder>() {
        @Override
        protected AddFlowInputBuilder initialValue() {
            final AddFlowInputBuilder fb = new AddFlowInputBuilder();

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

    private ListenerRegistration<DropTestRpcSender> notificationRegistration;

    /**
     * start listening on packetIn
     */
    public void start() {
        final SimpleTaskRetryLooper looper = new SimpleTaskRetryLooper(STARTUP_LOOP_TICK,
                STARTUP_LOOP_MAX_RETRIES);
        try {
            notificationRegistration = looper.loopUntilNoException(new Callable<ListenerRegistration<DropTestRpcSender>>() {
                @Override
                public ListenerRegistration<DropTestRpcSender> call() throws Exception {
                    return notificationService.registerNotificationListener(DropTestRpcSender.this);
                }
            });
        } catch (final Exception e) {
            LOG.warn("DropTest sender notification listener registration fail!");
            LOG.debug("DropTest sender notification listener registration fail! ..", e);
            throw new IllegalStateException("DropTest startup fail! Try again later.", e);
        }
    }

    @Override
    protected void processPacket(final InstanceIdentifier<Node> node, final Match match, final Instructions instructions) {
        final AddFlowInputBuilder fb = BUILDER.get();

        // Finally build our flow
        fb.setMatch(match);
        fb.setInstructions(instructions);

        // Construct the flow instance id

        fb.setNode(new NodeRef(node));

        // Add flow
        final AddFlowInput flow = fb.build();
        if (LOG.isDebugEnabled()) {
            LOG.debug("onPacketReceived - About to write flow (via SalFlowService) {}", flow);
        }
        ListenableFuture<RpcResult<AddFlowOutput>> result = JdkFutureAdapters.listenInPoolThread(flowService.addFlow(flow));
        Futures.addCallback(result, new FutureCallback<RpcResult<AddFlowOutput>>() {
            @Override
            public void onSuccess(final RpcResult<AddFlowOutput> o) {
                countFutureSuccess();
            }

            @Override
            public void onFailure(final Throwable throwable) {
                countFutureError();
            }
        });
    }

    /**
     * @param notificationService
     */
    public void setNotificationService(final NotificationService notificationService) {
        this.notificationService = notificationService;
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
}
