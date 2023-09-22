/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.statistics.services;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableClassToInstanceMap;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.atomic.AtomicLong;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.GetAllQueuesStatisticsFromAllPorts;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.GetAllQueuesStatisticsFromAllPortsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.GetAllQueuesStatisticsFromAllPortsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.GetAllQueuesStatisticsFromGivenPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.GetAllQueuesStatisticsFromGivenPortInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.GetAllQueuesStatisticsFromGivenPortOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.GetQueueStatisticsFromGivenPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.GetQueueStatisticsFromGivenPortInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.GetQueueStatisticsFromGivenPortOutput;
import org.opendaylight.yangtools.yang.binding.Rpc;
import org.opendaylight.yangtools.yang.common.RpcResult;

public class OpendaylightQueueStatisticsRpcs {
    private final AllQueuesAllPortsService allQueuesAllPorts;
    private final AllQueuesOnePortService allQueuesOnePort;
    private final OneQueueOnePortService oneQueueOnePort;
    private final NotificationPublishService notificationPublishService;

    public OpendaylightQueueStatisticsRpcs(final RequestContextStack requestContextStack,
                                                  final DeviceContext deviceContext,
                                                  final AtomicLong compatibilityXidSeed,
                                                  final NotificationPublishService notificationPublishService) {
        this.notificationPublishService = notificationPublishService;
        allQueuesAllPorts = new AllQueuesAllPortsService(requestContextStack, deviceContext, compatibilityXidSeed);
        allQueuesOnePort = new AllQueuesOnePortService(requestContextStack, deviceContext, compatibilityXidSeed);
        oneQueueOnePort = new OneQueueOnePortService(requestContextStack, deviceContext, compatibilityXidSeed);
    }

    private ListenableFuture<RpcResult<GetAllQueuesStatisticsFromAllPortsOutput>> getAllQueuesStatisticsFromAllPorts(
            final GetAllQueuesStatisticsFromAllPortsInput input) {
        return allQueuesAllPorts.handleAndNotify(input, notificationPublishService);
    }

    private ListenableFuture<RpcResult<GetAllQueuesStatisticsFromGivenPortOutput>> getAllQueuesStatisticsFromGivenPort(
            final GetAllQueuesStatisticsFromGivenPortInput input) {
        return allQueuesOnePort.handleAndNotify(input, notificationPublishService);
    }

    private ListenableFuture<RpcResult<GetQueueStatisticsFromGivenPortOutput>> getQueueStatisticsFromGivenPort(
            final GetQueueStatisticsFromGivenPortInput input) {
        return oneQueueOnePort.handleAndNotify(input, notificationPublishService);
    }

    public ClassToInstanceMap<Rpc<?,?>> getRpcClassToInstanceMap() {
        return ImmutableClassToInstanceMap.<Rpc<?, ?>>builder()
            .put(GetAllQueuesStatisticsFromAllPorts.class, this::getAllQueuesStatisticsFromAllPorts)
            .put(GetAllQueuesStatisticsFromGivenPort.class, this::getAllQueuesStatisticsFromGivenPort)
            .put(GetQueueStatisticsFromGivenPort.class, this::getQueueStatisticsFromGivenPort)
            .build();
    }
}
