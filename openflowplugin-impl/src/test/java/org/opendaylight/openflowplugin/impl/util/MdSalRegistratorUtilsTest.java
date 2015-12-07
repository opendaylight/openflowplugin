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

package org.opendaylight.openflowplugin.impl.util;


import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yangtools.yang.binding.RpcService;
import java.math.BigInteger;

public class MdSalRegistratorUtilsTest {

    /**
     * Number of currently registrated services (can be changed) in {@link MdSalRegistratorUtils#registerServices
     * (RpcContext, DeviceContext)}
     */
    private static final int NUMBER_OF_RPC_SERVICE_REGISTRATION = 10;

    @Test
    public void registerServiceTest() {

        final RpcContext mockedRpcContext = mock(RpcContext.class);
        final DeviceContext mockedDeviceContext = mock(DeviceContext.class);
        final ConnectionContext mockedConnectionContext = mock(ConnectionContext.class);

        final FeaturesReply mockedFeatures = mock(FeaturesReply.class);
        when(mockedConnectionContext.getFeatures()).thenReturn(mockedFeatures);

        final BigInteger mockedDataPathId = mock(BigInteger.class);
        when(mockedFeatures.getDatapathId()).thenReturn(mockedDataPathId);

        when(mockedDeviceContext.getPrimaryConnectionContext()).thenReturn(mockedConnectionContext);
        MdSalRegistratorUtils.registerServices(mockedRpcContext,mockedDeviceContext);
        verify(mockedRpcContext, times(NUMBER_OF_RPC_SERVICE_REGISTRATION)).registerRpcServiceImplementation(any
                (RpcService.class.getClass()), any(RpcService.class));
    }

}
