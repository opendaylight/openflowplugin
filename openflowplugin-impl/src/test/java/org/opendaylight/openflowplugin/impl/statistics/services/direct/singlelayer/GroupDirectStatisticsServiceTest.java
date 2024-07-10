/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.statistics.services.direct.singlelayer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.impl.statistics.services.direct.AbstractDirectStatisticsServiceTest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetGroupStatisticsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetGroupStatisticsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.multipart.reply.multipart.reply.body.MultipartReplyGroupStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.multipart.request.multipart.request.body.MultipartRequestGroupStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.reply.GroupStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.MultipartReplyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.MultipartRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yangtools.binding.util.BindingMap;
import org.opendaylight.yangtools.yang.common.Uint32;

public class GroupDirectStatisticsServiceTest extends AbstractDirectStatisticsServiceTest {
    static final Uint32 GROUP_NO = Uint32.ONE;
    private SingleGetGroupStatistics service;

    @Override
    public void setUp() {
        service = new SingleGetGroupStatistics(requestContextStack,
                                                   deviceContext,
                                                   convertorManager,
                                                   multipartWriterProvider);
    }

    @Override
    public void testBuildRequestBody() {
        final MultipartRequestGroupStats body = (MultipartRequestGroupStats) ((MultipartRequest) service
            .buildRequest(new Xid(Uint32.valueOf(42L)), new GetGroupStatisticsInputBuilder()
                .setGroupId(new GroupId(GROUP_NO))
                .setNode(createNodeRef(NODE_ID))
                .build()))
            .getMultipartRequestBody();

        assertEquals(GROUP_NO, body.getGroupId().getValue());
    }

    @Override
    public void testBuildReply() {
        final var output = service.buildReply(List.of(new MultipartReplyBuilder()
            .setMultipartReplyBody(new MultipartReplyGroupStatsBuilder()
                .setGroupStats(BindingMap.of(new GroupStatsBuilder()
                    .setGroupId(new GroupId(GROUP_NO))
                    .build()))
                .build())
            .build()), true);
        assertTrue(output.nonnullGroupStats().size() > 0);

        final var stats = output.nonnullGroupStats().values().iterator().next();

        assertEquals(stats.getGroupId().getValue(), GROUP_NO);
    }

    @Override
    public void testStoreStatistics() {
        multipartWriterProvider.lookup(MultipartType.OFPMPGROUP).orElseThrow()
            .write(new GetGroupStatisticsOutputBuilder()
                .setGroupStats(BindingMap.of(new org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018
                    .group.statistics.reply.GroupStatsBuilder().setGroupId(new GroupId(GROUP_NO)).build()))
                .build(), true);
        verify(deviceContext).writeToTransactionWithParentsSlow(eq(LogicalDatastoreType.OPERATIONAL), any(), any());
    }
}
