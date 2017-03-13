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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.impl.statistics.services.direct.AbstractDirectStatisticsServiceTest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetNodeConnectorStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetNodeConnectorStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyPortStatsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.stats._case.MultipartReplyPortStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.stats._case.multipart.reply.port.stats.PortStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestPortStatsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.port.stats._case.MultipartRequestPortStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.node.connector.statistics.and.port.number.map.NodeConnectorStatisticsAndPortNumberMap;

public class NodeConnectorDirectStatisticsServiceTest extends AbstractDirectStatisticsServiceTest {
    private PortDirectStatisticsService service;

    @Override
    public void setUp() throws Exception {
        service = new PortDirectStatisticsService(requestContextStack, deviceContext, convertorManager, multipartWriterProvider);
    }

    @Override
    public void testBuildRequestBody() throws Exception {
        final GetNodeConnectorStatisticsInput input = mock(GetNodeConnectorStatisticsInput.class);

        when(input.getNode()).thenReturn(createNodeRef(NODE_ID));
        when(input.getNodeConnectorId()).thenReturn(nodeConnectorId);

        final MultipartRequestPortStatsCase body = (MultipartRequestPortStatsCase) ((MultipartRequestInput)service
            .buildRequest(new Xid(42L), input))
            .getMultipartRequestBody();

        final MultipartRequestPortStats nodeConnector = body.getMultipartRequestPortStats();

        assertEquals(PORT_NO, nodeConnector.getPortNo());
    }

    @Override
    public void testBuildReply() throws Exception {
        final MultipartReply reply = mock(MultipartReply.class);
        final MultipartReplyPortStatsCase nodeConnectorCase = mock(MultipartReplyPortStatsCase.class);
        final MultipartReplyPortStats nodeConnector = mock(MultipartReplyPortStats.class);
        final PortStats nodeConnectorStat = mock(PortStats.class);
        final List<PortStats> nodeConnectorStats = Arrays.asList(nodeConnectorStat);
        final List<MultipartReply> input = Arrays.asList(reply);

        when(nodeConnector.getPortStats()).thenReturn(nodeConnectorStats);
        when(nodeConnectorCase.getMultipartReplyPortStats()).thenReturn(nodeConnector);
        when(reply.getMultipartReplyBody()).thenReturn(nodeConnectorCase);

        when(nodeConnectorStat.getPortNo()).thenReturn(PORT_NO);
        when(nodeConnectorStat.getTxBytes()).thenReturn(BigInteger.ONE);
        when(nodeConnectorStat.getCollisions()).thenReturn(BigInteger.ONE);
        when(nodeConnectorStat.getRxBytes()).thenReturn(BigInteger.ONE);
        when(nodeConnectorStat.getRxCrcErr()).thenReturn(BigInteger.ONE);
        when(nodeConnectorStat.getRxDropped()).thenReturn(BigInteger.ONE);
        when(nodeConnectorStat.getRxErrors()).thenReturn(BigInteger.ONE);
        when(nodeConnectorStat.getRxFrameErr()).thenReturn(BigInteger.ONE);
        when(nodeConnectorStat.getRxOverErr()).thenReturn(BigInteger.ONE);
        when(nodeConnectorStat.getRxPackets()).thenReturn(BigInteger.ONE);
        when(nodeConnectorStat.getTxDropped()).thenReturn(BigInteger.ONE);
        when(nodeConnectorStat.getTxErrors()).thenReturn(BigInteger.ONE);

        final GetNodeConnectorStatisticsOutput output = service.buildReply(input, true);
        assertTrue(output.getNodeConnectorStatisticsAndPortNumberMap().size() > 0);

        final NodeConnectorStatisticsAndPortNumberMap stats =
                output.getNodeConnectorStatisticsAndPortNumberMap().get(0);

        assertEquals(stats.getNodeConnectorId(), nodeConnectorId);
    }

    @Override
    public void testStoreStatistics() throws Exception {
        final NodeConnectorStatisticsAndPortNumberMap stat = mock(NodeConnectorStatisticsAndPortNumberMap.class);
        when(stat.getNodeConnectorId()).thenReturn(nodeConnectorId);

        final List<NodeConnectorStatisticsAndPortNumberMap> stats = Arrays.asList(stat);
        final GetNodeConnectorStatisticsOutput output = mock(GetNodeConnectorStatisticsOutput.class);
        when(output.getNodeConnectorStatisticsAndPortNumberMap()).thenReturn(stats);

        multipartWriterProvider.lookup(MultipartType.OFPMPPORTSTATS).get().write(output, true);
        verify(deviceContext).writeToTransactionWithParentsSlow(eq(LogicalDatastoreType.OPERATIONAL), any(), any());
    }
}
