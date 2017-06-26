/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.core.connection;

import com.google.common.util.concurrent.ListenableFuture;
import io.netty.channel.Channel;
import java.net.InetSocketAddress;
import java.util.concurrent.Future;
import org.opendaylight.openflowjava.statistics.CounterEventTypes;
import org.opendaylight.openflowjava.statistics.StatisticsCounters;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.system.rev130927.DisconnectEvent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.system.rev130927.SwitchIdleEvent;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Notification;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 * Class is only wrapper for {@link AbstractConnectionAdapter} to provide statistics
 * records for counting all needed RPC messages in Openflow Java.
 */
abstract class AbstractConnectionAdapterStatistics extends AbstractConnectionAdapter implements MessageConsumer {

    private final StatisticsCounters statisticsCounters;

    AbstractConnectionAdapterStatistics(final Channel channel, final InetSocketAddress address) {
        super(channel, address);
        statisticsCounters = StatisticsCounters.getInstance();
    }

    @Override
    public Future<RpcResult<Void>> flowMod(final FlowModInput input) {
        statisticsCounters.incrementCounter(CounterEventTypes.DS_FLOW_MODS_ENTERED);
        return super.flowMod(input);
    }

    @Override
    protected <IN extends OfHeader, OUT extends OfHeader> ListenableFuture<RpcResult<OUT>> sendToSwitchExpectRpcResultFuture(
            final IN input, final Class<OUT> responseClazz, final String failureInfo) {
        statisticsCounters.incrementCounter(CounterEventTypes.DS_ENTERED_OFJAVA);
        return super.sendToSwitchExpectRpcResultFuture(input, responseClazz, failureInfo);
    }

    @Override
    protected ListenableFuture<RpcResult<Void>> sendToSwitchFuture(final DataObject input, final String failureInfo) {
        statisticsCounters.incrementCounter(CounterEventTypes.DS_ENTERED_OFJAVA);
        return super.sendToSwitchFuture(input, failureInfo);
    }

    @Override
    public void consume(final DataObject message) {
        if (Notification.class.isInstance(message)) {
            if (!(DisconnectEvent.class.isInstance(message) || SwitchIdleEvent.class.isInstance(message))) {
                statisticsCounters.incrementCounter(CounterEventTypes.US_MESSAGE_PASS);
            }
        } else if (OfHeader.class.isInstance(message)) {
            statisticsCounters.incrementCounter(CounterEventTypes.US_MESSAGE_PASS);
        }
        consumeDeviceMessage(message);
    }

    /**
     * Method is equivalent to {@link MessageConsumer#consume(DataObject)} to prevent missing method
     * in every children of {@link AbstractConnectionAdapterStatistics} class, because we overriding
     * original method for {@link StatisticsCounters}
     *
     * @param message from device to processing
     */
    protected abstract void consumeDeviceMessage(DataObject message);
}

