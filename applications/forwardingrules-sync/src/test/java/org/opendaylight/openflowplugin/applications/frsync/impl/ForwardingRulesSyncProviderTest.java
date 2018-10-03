/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frsync.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.sal.binding.api.RpcConsumerRegistry;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.SalFlatBatchService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
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
    private ClusterSingletonServiceProvider clusterSingletonService;

    @Before
    public void setUp() throws Exception {
        Mockito.when(rpcRegistry.getRpcService(ArgumentMatchers.<Class<? extends RpcService>>any()))
                .thenAnswer(invocation -> {
                    Class<? extends RpcService> serviceType =
                            (Class<? extends RpcService>) invocation.getArguments()[0];
                    return Mockito.mock(serviceType);
                });

        provider = new ForwardingRulesSyncProvider(dataBroker, rpcRegistry, clusterSingletonService);
        Mockito.verify(rpcRegistry).getRpcService(SalTableService.class);
        Mockito.verify(rpcRegistry).getRpcService(SalFlatBatchService.class);
    }

    @Test
    public void testInit() throws Exception {
        provider.init();

        Mockito.verify(dataBroker, Mockito.times(2)).registerDataTreeChangeListener(
                ArgumentMatchers.<DataTreeIdentifier<FlowCapableNode>>any(),
                ArgumentMatchers.<DataTreeChangeListener<FlowCapableNode>>any());
    }

    @After
    public void tearDown() {
        provider.close();
    }

}
