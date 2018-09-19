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
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetNodeConnectorStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetNodeConnectorStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.MultipartReplyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.MultipartRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.multipart.reply.multipart.reply.body.MultipartReplyPortStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.multipart.request.multipart.request.body.MultipartRequestPortStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.node.connector.statistics.and.port.number.map.NodeConnectorStatisticsAndPortNumberMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.node.connector.statistics.and.port.number.map.NodeConnectorStatisticsAndPortNumberMapBuilder;

public class NodeConnectorDirectStatisticsServiceTest extends AbstractDirectStatisticsServiceTest {
    private PortDirectStatisticsService service;

    @Override
    public void setUp() throws Exception {
        service = new PortDirectStatisticsService(requestContextStack,
                                                  deviceContext,
                                                  convertorManager,
                                                  multipartWriterProvider);
    }

    @Override
    public void testBuildRequestBody() throws Exception {
        final GetNodeConnectorStatisticsInput input = mock(GetNodeConnectorStatisticsInput.class);

        lenient().when(input.getNode()).thenReturn(createNodeRef(NODE_ID));
        when(input.getNodeConnectorId()).thenReturn(nodeConnectorId);

        final MultipartRequestPortStats body = (MultipartRequestPortStats) ((MultipartRequest)service
            .buildRequest(new Xid(42L), input))
            .getMultipartRequestBody();

        assertEquals(nodeConnectorId, body.getNodeConnectorId());
    }

    @Override
    public void testBuildReply() throws Exception {
        final NodeConnectorStatisticsAndPortNumberMap portStat = new NodeConnectorStatisticsAndPortNumberMapBuilder()
                .setNodeConnectorId(nodeConnectorId)
                .build();

        final MultipartReply reply = new MultipartReplyBuilder()
                .setMultipartReplyBody(new MultipartReplyPortStatsBuilder()
                        .setNodeConnectorStatisticsAndPortNumberMap(Collections.singletonList(portStat))
                        .build())
                .build();

        final List<MultipartReply> input = Collections.singletonList(reply);
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

        final List<NodeConnectorStatisticsAndPortNumberMap> stats = Collections.singletonList(stat);
        final GetNodeConnectorStatisticsOutput output = mock(GetNodeConnectorStatisticsOutput.class);
        when(output.getNodeConnectorStatisticsAndPortNumberMap()).thenReturn(stats);

        multipartWriterProvider.lookup(MultipartType.OFPMPPORTSTATS).get().write(output, true);
        verify(deviceContext).writeToTransactionWithParentsSlow(eq(LogicalDatastoreType.OPERATIONAL), any(), any());
    }
}
