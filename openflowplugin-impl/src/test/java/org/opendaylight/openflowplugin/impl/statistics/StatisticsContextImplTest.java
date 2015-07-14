/*
 *
 *  * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *  *
 *  * This program and the accompanying materials are made available under the
 *  * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 *  * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 *
 */

package org.opendaylight.openflowplugin.impl.statistics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;

public class StatisticsContextImplTest extends StatisticsContextImpAbstract {

    private static final Long TEST_XID = 55l;

    @Test
    public void createRequestContextTest() {
        when(mockedDeviceContext.getReservedXid()).thenReturn(TEST_XID);

        StatisticsContextImpl statisticsContext = new StatisticsContextImpl(mockedDeviceContext);
        RequestContext<Object> requestContext = statisticsContext.createRequestContext();
        assertNotNull(requestContext);
        assertEquals(TEST_XID, requestContext.getXid().getValue());
    }

    /**
     * There is nothing to check in close method
     */
    @Test
    public void closeTest() {
        StatisticsContextImpl statisticsContext = new StatisticsContextImpl(mockedDeviceContext);
        statisticsContext.createRequestContext();
        statisticsContext.close();
    }
}
