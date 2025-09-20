/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frsync.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.RpcService;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.singleton.api.ClusterSingletonServiceProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.ProcessFlatBatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTable;

/**
 * Test for {@link ForwardingRulesSyncProvider}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ForwardingRulesSyncProviderTest {
    @Mock
    private DataBroker dataBroker;
    @Mock
    private RpcService rpcRegistry;
    @Mock
    private ClusterSingletonServiceProvider clusterSingletonService;

    @Test
    public void testInit() {
        when(rpcRegistry.getRpc(any())).thenAnswer(invocation -> mock(invocation.<Class<?>>getArgument(0)));

        try (var provider = new ForwardingRulesSyncProvider(dataBroker, rpcRegistry, clusterSingletonService)) {
            verify(rpcRegistry).getRpc(UpdateTable.class);
            verify(rpcRegistry).getRpc(ProcessFlatBatch.class);
            verify(dataBroker).registerTreeChangeListener(eq(LogicalDatastoreType.CONFIGURATION), any(), any());
            verify(dataBroker).registerTreeChangeListener(eq(LogicalDatastoreType.OPERATIONAL), any(), any());
        }
    }
}
