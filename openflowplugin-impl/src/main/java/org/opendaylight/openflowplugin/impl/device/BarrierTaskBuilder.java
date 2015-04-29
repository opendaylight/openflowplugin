/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.device;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * openflowplugin-impl
 * org.opendaylight.openflowplugin.impl.device
 * Barrier message self restarting builder.
 *
 * @author <a href="mailto:vdemcak@cisco.com">Vaclav Demcak</a>
 *         Created: Apr 3, 2015
 */
public class BarrierTaskBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(BarrierTaskBuilder.class);
    public static final long DELAY = 500L;

    private final DeviceContext deviceCtx;

    public BarrierTaskBuilder(final DeviceContext deviceCtx) {
        this.deviceCtx = Preconditions.checkNotNull(deviceCtx);
        Preconditions.checkNotNull(deviceCtx.getTimer());
    }

    public void buildAndFireBarrierTask() {
        Timeout timeout = deviceCtx.getTimer().newTimeout(new BarrierTask(deviceCtx), DELAY, TimeUnit.MILLISECONDS);
        deviceCtx.setCurrentBarrierTimeout(timeout);
    }


    private final class BarrierTask implements TimerTask {

        private final DeviceContext deviceCtx;

        public BarrierTask(final DeviceContext deviceCtx) {
            this.deviceCtx = deviceCtx;
        }

        /**
         * @return OF-message, ready to send
         */
        private BarrierInput makeBarrier() {
            final BarrierInputBuilder biBuilder = new BarrierInputBuilder();
            biBuilder.setVersion(deviceCtx.getDeviceState().getVersion());
            biBuilder.setXid(deviceCtx.getNextXid().getValue());
            return biBuilder.build();
        }

        @Override
        public void run(final Timeout timeout) throws Exception {
            // check outstanding requests first
            if (deviceCtx.getDeviceState().isValid()) {
                if (deviceCtx.getNumberOfOutstandingRequests() > 0) {
                    BarrierInput barrierInput = makeBarrier();
                    LOG.trace("sending out barrier [{}]", barrierInput.getXid());
                    synchronized (deviceCtx) {
                        final Future<RpcResult<BarrierOutput>> future = deviceCtx.getPrimaryConnectionContext()
                                .getConnectionAdapter().barrier(barrierInput);
                        final ListenableFuture<RpcResult<BarrierOutput>> lsFuture = JdkFutureAdapters.listenInPoolThread(future);
                        Futures.addCallback(lsFuture, makeCallBack());
                    }
                } else {
                    // if no requests
                    buildAndFireBarrierTask();
                }
            } else {
                LOG.trace("DeviceContext is not valid, will not create next barrier task.");
            }
        }

        private FutureCallback<RpcResult<BarrierOutput>> makeCallBack() {
            return new FutureCallback<RpcResult<BarrierOutput>>() {
                @Override
                public void onSuccess(final RpcResult<BarrierOutput> result) {
                    synchronized (deviceCtx) {
                        BarrierProcessor.processOutstandingRequests(result.getResult().getXid(), deviceCtx);
                        buildAndFireBarrierTask();
                    }
                }

                @Override
                public void onFailure(final Throwable t) {
                    LOG.info("Barrier has failed {} ", t.getMessage());
                    LOG.trace("Barrier has failed", t);
                }
            };
        }

    }

}
