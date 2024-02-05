/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.statistics.services;

import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.atomic.AtomicLong;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.GetQueueStatisticsFromGivenPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.GetQueueStatisticsFromGivenPortInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.GetQueueStatisticsFromGivenPortOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;

@Deprecated
public final class GetQueueStatisticsFromGivenPortImpl implements GetQueueStatisticsFromGivenPort {
    private final OneQueueOnePortService oneQueueOnePort;
    private final NotificationPublishService notificationPublishService;

    public GetQueueStatisticsFromGivenPortImpl(final RequestContextStack requestContextStack,
            final DeviceContext deviceContext, final AtomicLong compatibilityXidSeed,
            final NotificationPublishService notificationPublishService) {
        oneQueueOnePort = new OneQueueOnePortService(requestContextStack, deviceContext, compatibilityXidSeed);
        this.notificationPublishService = requireNonNull(notificationPublishService);
    }

    @Override
    public ListenableFuture<RpcResult<GetQueueStatisticsFromGivenPortOutput>> invoke(
            final GetQueueStatisticsFromGivenPortInput input) {
        return oneQueueOnePort.handleAndNotify(input, notificationPublishService);
    }
}
