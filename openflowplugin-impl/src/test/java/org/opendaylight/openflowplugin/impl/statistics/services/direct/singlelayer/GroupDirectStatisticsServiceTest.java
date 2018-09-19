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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.impl.statistics.services.direct.AbstractDirectStatisticsServiceTest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetGroupStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetGroupStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.multipart.reply.multipart.reply.body.MultipartReplyGroupStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.multipart.request.multipart.request.body.MultipartRequestGroupStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.reply.GroupStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.reply.GroupStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.MultipartReplyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.MultipartRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;

public class GroupDirectStatisticsServiceTest extends AbstractDirectStatisticsServiceTest {
    static final Long GROUP_NO = 1L;
    private GroupDirectStatisticsService service;

    @Override
    public void setUp() throws Exception {
        service = new GroupDirectStatisticsService(requestContextStack,
                                                   deviceContext,
                                                   convertorManager,
                                                   multipartWriterProvider);
    }

    @Override
    public void testBuildRequestBody() throws Exception {
        final GetGroupStatisticsInput input = mock(GetGroupStatisticsInput.class);

        lenient().when(input.getNode()).thenReturn(createNodeRef(NODE_ID));
        when(input.getGroupId()).thenReturn(new GroupId(GROUP_NO));

        final MultipartRequestGroupStats body = (MultipartRequestGroupStats) ((MultipartRequest) service
            .buildRequest(new Xid(42L), input))
            .getMultipartRequestBody();

        assertEquals(GROUP_NO, body.getGroupId().getValue());
    }

    @Override
    public void testBuildReply() throws Exception {
        final GroupStats groupStat = new GroupStatsBuilder()
                .setGroupId(new GroupId(GROUP_NO))
                .build();

        final MultipartReply reply = new MultipartReplyBuilder()
                .setMultipartReplyBody(new MultipartReplyGroupStatsBuilder()
                        .setGroupStats(Collections.singletonList(groupStat))
                        .build())
                .build();

        final List<MultipartReply> input = Collections.singletonList(reply);

        final GetGroupStatisticsOutput output = service.buildReply(input, true);
        assertTrue(output.getGroupStats().size() > 0);

        final org.opendaylight.yang.gen.v1.urn
            .opendaylight.group.types.rev131018.group.statistics.reply.GroupStats stats = output.getGroupStats().get(0);

        assertEquals(stats.getGroupId().getValue(), GROUP_NO);
    }

    @Override
    public void testStoreStatistics() throws Exception {
        final org.opendaylight.yang.gen.v1.urn
                .opendaylight.group.types.rev131018.group.statistics.reply.GroupStats stat =
                mock(org.opendaylight.yang.gen.v1.urn
                        .opendaylight.group.types.rev131018.group.statistics.reply.GroupStats.class);
        when(stat.getGroupId()).thenReturn(new GroupId(GROUP_NO));

        final List<org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.reply
                .GroupStats>
                stats = Collections.singletonList(stat);
        final GetGroupStatisticsOutput output = mock(GetGroupStatisticsOutput.class);
        when(output.getGroupStats()).thenReturn(stats);

        multipartWriterProvider.lookup(MultipartType.OFPMPGROUP).get().write(output, true);
        verify(deviceContext).writeToTransactionWithParentsSlow(eq(LogicalDatastoreType.OPERATIONAL), any(), any());
    }
}
