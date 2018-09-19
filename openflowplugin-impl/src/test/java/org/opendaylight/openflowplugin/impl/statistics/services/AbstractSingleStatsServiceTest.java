/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics.services;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.impl.rpc.AbstractRequestContext;
import org.opendaylight.yangtools.yang.common.RpcResult;

public abstract class AbstractSingleStatsServiceTest extends AbstractStatsServiceTest {

    @Mock
    protected NotificationPublishService notificationPublishService;

    protected RequestContext<Object> rqContext;

    protected RpcResult<Object> rpcResult;

    @Override
    public void init() throws Exception {
        super.init();
        rqContext = new AbstractRequestContext<Object>(42L) {
            @Override
            public void close() {
                //NOOP
            }
        };
        final Answer closeRequestFutureAnswer = invocation -> {
            rqContext.setResult(rpcResult);
            rqContext.close();
            return null;
        };

        Mockito.when(rqContextStack.<Object>createRequestContext()).thenReturn(rqContext);
        Mockito.doAnswer(closeRequestFutureAnswer).when(multiMsgCollector).endCollecting(null);
    }
}
