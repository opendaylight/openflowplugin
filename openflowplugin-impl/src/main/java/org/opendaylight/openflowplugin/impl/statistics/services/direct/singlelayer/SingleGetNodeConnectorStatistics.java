/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.statistics.services.direct.singlelayer;

import java.util.List;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.impl.datastore.MultipartWriterProvider;
import org.opendaylight.openflowplugin.impl.statistics.services.direct.AbstractGetNodeConnectorStatistics;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetNodeConnectorStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetNodeConnectorStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetNodeConnectorStatisticsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.MultipartRequestBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.multipart.reply.multipart.reply.body.MultipartReplyPortStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.multipart.request.multipart.request.body.MultipartRequestPortStatsBuilder;
import org.opendaylight.yangtools.binding.util.BindingMap;

public final class SingleGetNodeConnectorStatistics extends AbstractGetNodeConnectorStatistics<MultipartReply> {
    public SingleGetNodeConnectorStatistics(final RequestContextStack requestContextStack,
            final DeviceContext deviceContext, final ConvertorExecutor convertorExecutor,
            final MultipartWriterProvider statisticsWriterProvider) {
        super(requestContextStack, deviceContext, convertorExecutor, statisticsWriterProvider);
    }

    @Override
    protected GetNodeConnectorStatisticsOutput buildReply(final List<MultipartReply> input, final boolean success) {
        return new GetNodeConnectorStatisticsOutputBuilder()
            .setNodeConnectorStatisticsAndPortNumberMap(input.stream()
                .flatMap(multipartReply -> ((MultipartReplyPortStats) multipartReply.getMultipartReplyBody())
                    .nonnullNodeConnectorStatisticsAndPortNumberMap().values().stream())
                .collect(BindingMap.toOrderedMap()))
            .build();
    }

    @Override
    protected OfHeader buildRequest(final Xid xid,  final GetNodeConnectorStatisticsInput input) {
        return new MultipartRequestBuilder()
            .setXid(xid.getValue())
            .setVersion(getVersion())
            .setRequestMore(false)
            .setMultipartRequestBody(new MultipartRequestPortStatsBuilder()
                .setNodeConnectorId(input.getNodeConnectorId())
                .build())
            .build();
    }
}
