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
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupDescription;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupDescriptionInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupDescriptionOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;

@Deprecated
public final class GetGroupDescriptionImpl implements GetGroupDescription {
    private final GroupDescriptionService groupDesc;
    private final NotificationPublishService notificationPublishService;

    public GetGroupDescriptionImpl(final RequestContextStack requestContextStack, final DeviceContext deviceContext,
            final AtomicLong compatibilityXidSeed, final NotificationPublishService notificationPublishService,
            final ConvertorExecutor convertorExecutor) {
        groupDesc = new GroupDescriptionService(requestContextStack, deviceContext, compatibilityXidSeed,
            convertorExecutor);
        this.notificationPublishService = requireNonNull(notificationPublishService);
    }

    @Override
    public ListenableFuture<RpcResult<GetGroupDescriptionOutput>> invoke(final GetGroupDescriptionInput input) {
        return groupDesc.handleAndNotify(input, notificationPublishService);
    }
}
