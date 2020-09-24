/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.statistics.services.direct.multilayer;

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
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.impl.statistics.services.direct.AbstractDirectStatisticsServiceTest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetGroupStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetGroupStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group._case.MultipartReplyGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group._case.multipart.reply.group.GroupStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group._case.multipart.reply.group.GroupStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.group._case.MultipartRequestGroup;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;

public class GroupDirectStatisticsServiceTest extends AbstractDirectStatisticsServiceTest {
    static final Uint32 GROUP_NO = Uint32.ONE;
    private GroupDirectStatisticsService service;

    @Override
    public void setUp() {
        service = new GroupDirectStatisticsService(requestContextStack,
                                                   deviceContext,
                                                   convertorManager,
                                                   multipartWriterProvider);
    }

    @Override
    public void testBuildRequestBody() {
        final GetGroupStatisticsInput input = mock(GetGroupStatisticsInput.class);

        lenient().when(input.getNode()).thenReturn(createNodeRef(NODE_ID));
        when(input.getGroupId()).thenReturn(new GroupId(GROUP_NO));

        final MultipartRequestGroupCase body = (MultipartRequestGroupCase) ((MultipartRequestInput) service
            .buildRequest(new Xid(Uint32.valueOf(42)), input))
            .getMultipartRequestBody();

        final MultipartRequestGroup group = body.getMultipartRequestGroup();

        assertEquals(GROUP_NO, group.getGroupId().getValue());
    }

    @Override
    public void testBuildReply() {
        final MultipartReply reply = mock(MultipartReply.class);
        final MultipartReplyGroupCase groupCase = mock(MultipartReplyGroupCase.class);
        final MultipartReplyGroup group = mock(MultipartReplyGroup.class);
        final GroupStats groupStat = new GroupStatsBuilder()
                .setGroupId(new org.opendaylight.yang.gen.v1.urn
                        .opendaylight.openflow.common.types.rev130731.GroupId(GROUP_NO))
                .setByteCount(Uint64.ONE)
                .setPacketCount(Uint64.ONE)
                .setBucketStats(Collections.emptyList())
                .setDurationSec(Uint32.ONE)
                .setDurationNsec(Uint32.ONE)
                .setRefCount(Uint32.ZERO)
                .build();

        final List<GroupStats> groupStats = Collections.singletonList(groupStat);
        final List<MultipartReply> input = Collections.singletonList(reply);

        when(group.getGroupStats()).thenReturn(groupStats);
        when(groupCase.getMultipartReplyGroup()).thenReturn(group);
        when(reply.getMultipartReplyBody()).thenReturn(groupCase);

        final GetGroupStatisticsOutput output = service.buildReply(input, true);
        assertTrue(output.getGroupStats().size() > 0);

        final org.opendaylight.yang.gen.v1.urn
            .opendaylight.group.types.rev131018.group.statistics.reply.GroupStats stats =
                output.nonnullGroupStats().values().iterator().next();

        assertEquals(stats.getGroupId().getValue(), GROUP_NO);
    }

    @Override
    public void testStoreStatistics() {
        final var stat = new org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.reply
                .GroupStatsBuilder().setGroupId(new GroupId(GROUP_NO)).build();

        final GetGroupStatisticsOutput output = mock(GetGroupStatisticsOutput.class);
        when(output.nonnullGroupStats()).thenReturn(Collections.singletonMap(stat.key(), stat));

        multipartWriterProvider.lookup(MultipartType.OFPMPGROUP).get().write(output, true);
        verify(deviceContext).writeToTransactionWithParentsSlow(eq(LogicalDatastoreType.OPERATIONAL), any(), any());
    }
}
