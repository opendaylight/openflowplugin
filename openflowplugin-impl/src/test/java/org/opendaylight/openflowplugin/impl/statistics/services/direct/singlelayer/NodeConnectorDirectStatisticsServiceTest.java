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
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetNodeConnectorStatisticsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetNodeConnectorStatisticsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.MultipartReplyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.MultipartRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.multipart.reply.multipart.reply.body.MultipartReplyPortStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.multipart.request.multipart.request.body.MultipartRequestPortStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.node.connector.statistics.and.port.number.map.NodeConnectorStatisticsAndPortNumberMapBuilder;
import org.opendaylight.yangtools.binding.util.BindingMap;
import org.opendaylight.yangtools.yang.common.Uint32;

public class NodeConnectorDirectStatisticsServiceTest extends AbstractDirectStatisticsServiceTest {
    private SingleGetNodeConnectorStatistics service;

    @Override
    public void setUp() {
        service = new SingleGetNodeConnectorStatistics(requestContextStack, deviceContext, convertorManager,
            multipartWriterProvider);
    }

    @Override
    public void testBuildRequestBody() {
        final var body = (MultipartRequestPortStats) ((MultipartRequest)service
            .buildRequest(new Xid(Uint32.valueOf(42L)), new GetNodeConnectorStatisticsInputBuilder()
                .setNode(createNodeRef(NODE_ID))
                .setNodeConnectorId(nodeConnectorId)
                .build()))
            .getMultipartRequestBody();

        assertEquals(nodeConnectorId, body.getNodeConnectorId());
    }

    @Override
    public void testBuildReply() {
        final var output = service.buildReply(List.of(new MultipartReplyBuilder()
            .setMultipartReplyBody(new MultipartReplyPortStatsBuilder()
                .setNodeConnectorStatisticsAndPortNumberMap(BindingMap.of(
                    new NodeConnectorStatisticsAndPortNumberMapBuilder().setNodeConnectorId(nodeConnectorId).build()))
                .build())
            .build()), true);
        assertTrue(output.nonnullNodeConnectorStatisticsAndPortNumberMap().size() > 0);

        final var stats = output.nonnullNodeConnectorStatisticsAndPortNumberMap().values().iterator().next();

        assertEquals(stats.getNodeConnectorId(), nodeConnectorId);
    }

    @Override
    public void testStoreStatistics() {
        multipartWriterProvider.lookup(MultipartType.OFPMPPORTSTATS).orElseThrow()
            .write(new GetNodeConnectorStatisticsOutputBuilder()
                .setNodeConnectorStatisticsAndPortNumberMap(BindingMap.of(
                    new NodeConnectorStatisticsAndPortNumberMapBuilder().setNodeConnectorId(nodeConnectorId).build()))
                .build(), true);
        verify(deviceContext).writeToTransactionWithParentsSlow(eq(LogicalDatastoreType.OPERATIONAL), any(), any());
    }
}
