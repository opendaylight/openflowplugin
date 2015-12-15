/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics.services;

import java.util.concurrent.atomic.AtomicLong;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestPortStatsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.GetNodeConnectorStatisticsInputBuilder;

/**
 * Test of {@link PortStatsService}
 */
public class PortStatsServiceTest extends AbstractStatsServiceTest {

    private PortStatsService portStatsService;

    public void setUp() {
        portStatsService = new PortStatsService(rqContextStack, deviceContext,
                new AtomicLong());
    }

    @Test
    public void testBuildRequest() throws Exception {
        Xid xid = new Xid(42L);
        GetNodeConnectorStatisticsInputBuilder input = new GetNodeConnectorStatisticsInputBuilder()
                .setNodeConnectorId(new NodeConnectorId("junitProto:11:12"))
                .setNode(createNodeRef("junitProto:11"));

        final OfHeader request = portStatsService.buildRequest(xid, input.build());

        Assert.assertTrue(request instanceof MultipartRequestInput);
        final MultipartRequestInput mpRequest = (MultipartRequestInput) request;
        Assert.assertTrue(mpRequest.getMultipartRequestBody() instanceof MultipartRequestPortStatsCase);
        final MultipartRequestPortStatsCase mpRequestBody = (MultipartRequestPortStatsCase) mpRequest.getMultipartRequestBody();
        Assert.assertEquals(12L, mpRequestBody.getMultipartRequestPortStats().getPortNo().longValue());

    }
}