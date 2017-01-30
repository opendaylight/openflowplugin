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
import org.opendaylight.openflowplugin.impl.statistics.services.direct.AbstractMeterDirectStatisticsService;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetMeterStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetMeterStatisticsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.multipart.reply.multipart.reply.body.MultipartReplyMeterStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.MultipartReply;

public class MeterDirectStatisticsService extends AbstractMeterDirectStatisticsService<MultipartReply> {

    public MeterDirectStatisticsService(RequestContextStack requestContextStack, DeviceContext deviceContext, ConvertorExecutor convertorExecutor) {
        super(requestContextStack, deviceContext, convertorExecutor);
    }

    @Override
    protected GetMeterStatisticsOutput buildReply(List<MultipartReply> input, boolean success) {
        return  new GetMeterStatisticsOutputBuilder()
            .setMeterStats(input
                .stream()
                .flatMap(multipartReply -> MultipartReplyMeterStats.class
                    .cast(multipartReply.getMultipartReplyBody())
                    .getMeterStats()
                    .stream())
                .collect(Collectors.toList()))
            .build();
    }

}
