/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics.services.direct.singlelayer;

import java.util.List;
import java.util.stream.Collectors;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.impl.statistics.services.direct.AbstractQueueDirectStatisticsService;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetQueueStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetQueueStatisticsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.multipart.reply.multipart.reply.body.MultipartReplyQueueStats;

public class QueueDirectStatisticsService extends AbstractQueueDirectStatisticsService<MultipartReply> {

    public QueueDirectStatisticsService(RequestContextStack requestContextStack, DeviceContext deviceContext, ConvertorExecutor convertorExecutor) {
        super(requestContextStack, deviceContext, convertorExecutor);
    }

    @Override
    protected GetQueueStatisticsOutput buildReply(List<MultipartReply> input, boolean success) {
        return  new GetQueueStatisticsOutputBuilder()
            .setQueueIdAndStatisticsMap(input
                .stream()
                .flatMap(multipartReply -> MultipartReplyQueueStats.class
                    .cast(multipartReply.getMultipartReplyBody())
                    .getQueueIdAndStatisticsMap()
                    .stream())
                .collect(Collectors.toList()))
            .build();
    }

}
