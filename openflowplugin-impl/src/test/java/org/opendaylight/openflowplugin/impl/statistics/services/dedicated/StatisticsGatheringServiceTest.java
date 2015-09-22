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
import org.opendaylight.openflowplugin.impl.services.ServiceMocking;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;

/**
 * Test for {@link StatisticsGatheringService}.
 */
public class StatisticsGatheringServiceTest extends ServiceMocking {

    private StatisticsGatheringService statisticsGatheringService;

    @Override
    protected void setup() {
        statisticsGatheringService = new StatisticsGatheringService(mockedRequestContextStack, mockedDeviceContext);

    }

    @Test
    public void testGetStatisticsOfType() throws Exception {
        final EventIdentifier eventIdentifier = new EventIdentifier("ut-event", "ut-device-id:1");
        for (MultipartType mpType : MultipartType.values()) {
            statisticsGatheringService.getStatisticsOfType(eventIdentifier, mpType);
        }

        Mockito.verify(mockedRequestContextStack, Mockito.times(15)).createRequestContext();
    }

    @Test
    public void testBuildRequest() throws Exception {
        final long xidValue = 21L;
        Xid xid = new Xid(xidValue);
        for (MultipartType mpType : MultipartType.values()) {
            final OfHeader request = statisticsGatheringService.buildRequest(xid, mpType);
            Assert.assertEquals(MultipartRequestInput.class, request.getImplementedInterface());
            Assert.assertEquals(xidValue, request.getXid().longValue());
            Assert.assertNotNull(request);
        }
    }
}