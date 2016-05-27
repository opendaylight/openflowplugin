/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frsync.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.RpcConsumerRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.SalGroupService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.SalMeterService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.SalTableService;
import org.opendaylight.yangtools.yang.binding.RpcService;

/**
 * Test for {@link ForwardingRulesSyncProvider}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ForwardingRulesSyncProviderTest {

    private ForwardingRulesSyncProvider provider;
    @Mock
    private DataBroker dataBroker;
    @Mock
    private RpcConsumerRegistry rpcRegistry;
    @Mock
    private BindingAwareBroker broker;
    @Mock
    private BindingAwareBroker.ProviderContext providerContext;

    @Before
    public void setUp() throws Exception {
        Mockito.when(rpcRegistry.getRpcService(Matchers.<Class<? extends RpcService>>any()))
                .thenAnswer(new Answer<RpcService>() {
                    @Override
                    public RpcService answer(final InvocationOnMock invocation) throws Throwable {
                        Class<? extends RpcService> serviceType = (Class<? extends RpcService>) invocation.getArguments()[0];
                        return Mockito.mock(serviceType);
                    }
                });

        provider = new ForwardingRulesSyncProvider(broker, dataBroker, rpcRegistry);

        Mockito.verify(rpcRegistry).getRpcService(SalFlowService.class);
        Mockito.verify(rpcRegistry).getRpcService(SalGroupService.class);
        Mockito.verify(rpcRegistry).getRpcService(SalMeterService.class);
        Mockito.verify(rpcRegistry).getRpcService(SalTableService.class);
//        Mockito.verify(rpcRegistry).getRpcService(FlowCapableTransactionService.class);

        Mockito.verify(broker).registerProvider(provider);
    }

    @Test
    public void testOnSessionInitiated() throws Exception {
        provider.onSessionInitiated(providerContext);

        Mockito.verify(dataBroker, Mockito.times(2)).registerDataTreeChangeListener(
                Matchers.<DataTreeIdentifier<FlowCapableNode>>any(),
                Matchers.<DataTreeChangeListener<FlowCapableNode>>any());
    }
}