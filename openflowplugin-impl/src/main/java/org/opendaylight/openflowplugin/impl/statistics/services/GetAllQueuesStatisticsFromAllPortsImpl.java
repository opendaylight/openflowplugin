/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.GetAllQueuesStatisticsFromAllPorts;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.GetAllQueuesStatisticsFromAllPortsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.GetAllQueuesStatisticsFromAllPortsOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;

@Deprecated
public final class GetAllQueuesStatisticsFromAllPortsImpl implements GetAllQueuesStatisticsFromAllPorts {
    private final NotificationPublishService notificationPublishService;
    private final AllQueuesAllPortsService allQueuesAllPorts;

    public GetAllQueuesStatisticsFromAllPortsImpl(final RequestContextStack requestContextStack,
            final DeviceContext deviceContext, final AtomicLong compatibilityXidSeed,
            final NotificationPublishService notificationPublishService) {
        allQueuesAllPorts = new AllQueuesAllPortsService(requestContextStack, deviceContext, compatibilityXidSeed);
        this.notificationPublishService = requireNonNull(notificationPublishService);
    }

    @Override
    public ListenableFuture<RpcResult<GetAllQueuesStatisticsFromAllPortsOutput>> invoke(
            final GetAllQueuesStatisticsFromAllPortsInput input) {
        return allQueuesAllPorts.handleAndNotify(input, notificationPublishService);
    }
}
