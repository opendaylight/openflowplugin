/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics.services.dedicated;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.EventIdentifier;
import org.opendaylight.openflowplugin.impl.datastore.MultipartWriterProviderFactory;
import org.opendaylight.openflowplugin.impl.services.ServiceMocking;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManager;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManagerFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;

/**
 * Test for {@link StatisticsGatheringOnTheFlyService}.
 */
public class StatisticsGatheringOnTheFlyServiceTest extends ServiceMocking {

    public static final NodeId NODE_ID = new NodeId(DUMMY_NODE_ID);
    private StatisticsGatheringOnTheFlyService<MultipartReply> statisticsGatheringService;

    @Override
    protected void setup() {
        final ConvertorManager convertorManager = ConvertorManagerFactory.createDefaultManager();
        statisticsGatheringService = new StatisticsGatheringOnTheFlyService<>(mockedRequestContextStack, mockedDeviceContext, convertorManager, MultipartWriterProviderFactory.createDefaultProvider(mockedDeviceContext));
        Mockito.doReturn(NODE_ID).when(mockedPrimConnectionContext).getNodeId();
        Mockito.when(mockedDeviceInfo.getNodeId()).thenReturn(NODE_ID);
        Mockito.when(mockedDeviceContext.getDeviceInfo().getNodeId()).thenReturn(NODE_ID);
    }

    @Test
    public void testGetStatisticsOfType() throws Exception {
        final EventIdentifier eventIdentifier = new EventIdentifier("ut-event", "ut-device-id:1");
        statisticsGatheringService.getStatisticsOfType(eventIdentifier, MultipartType.OFPMPFLOW);
        Mockito.verify(mockedRequestContextStack).createRequestContext();
    }

    @Test
    public void testBuildRequest() throws Exception {
        final long xidValue = 21L;
        Xid xid = new Xid(xidValue);
        final OfHeader request = statisticsGatheringService.buildRequest(xid, MultipartType.OFPMPFLOW);
        Assert.assertEquals(MultipartRequestInput.class, request.getImplementedInterface());
        Assert.assertEquals(xidValue, request.getXid().longValue());
        Assert.assertNotNull(request);
    }
}
