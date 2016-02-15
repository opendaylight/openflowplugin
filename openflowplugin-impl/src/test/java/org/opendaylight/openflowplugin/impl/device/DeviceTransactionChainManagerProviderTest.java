/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.device;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;

@RunWith(MockitoJUnitRunner.class)
public class DeviceTransactionChainManagerProviderTest {


    @Mock
    DataBroker dataBroker;
    @Mock
    ConnectionContext connectionContext;
    @Mock
    ConnectionContext concurrentConnectionContex;

    @Mock
    DeviceManager deviceManager;

    private static final NodeId nodeId = new NodeId("OPF:TEST");
    private DeviceTransactionChainManagerProvider deviceTransactionChainManagerProvider;

    @Before
    public void setup() {
        deviceTransactionChainManagerProvider = new DeviceTransactionChainManagerProvider(dataBroker);
        Mockito.when(connectionContext.getNodeId()).thenReturn(nodeId);
        Mockito.when(concurrentConnectionContex.getNodeId()).thenReturn(nodeId);
    }

    /**
     * This test verifies code path for registering new connection when no {@link org.opendaylight.openflowplugin.impl.device.TransactionChainManager}
     * is present in registry.
     *
     * @throws Exception
     */
    @Test
    public void testProvideTransactionChainManagerOrWaitForNotification1() throws Exception {
        final DeviceTransactionChainManagerProvider.TransactionChainManagerRegistration transactionChainManagerRegistration = deviceTransactionChainManagerProvider.provideTransactionChainManager(connectionContext);
        Assert.assertTrue(transactionChainManagerRegistration.ownedByInvokingConnectionContext());
    }

    /**
     * This test verifies code path for registering new connection when {@link org.opendaylight.openflowplugin.impl.device.TransactionChainManager}
     * is present in registry.
     *
     * @throws Exception
     */
    @Test
    @Ignore
    public void testProvideTransactionChainManagerOrWaitForNotification2() throws Exception {
        final DeviceTransactionChainManagerProvider.TransactionChainManagerRegistration transactionChainManagerRegistration_1 = deviceTransactionChainManagerProvider.provideTransactionChainManager(connectionContext);
        Assert.assertTrue(TransactionChainManager.TransactionChainManagerStatus.WORKING.equals(transactionChainManagerRegistration_1.getTransactionChainManager().getTransactionChainManagerStatus()));
        final DeviceTransactionChainManagerProvider.TransactionChainManagerRegistration transactionChainManagerRegistration_2 = deviceTransactionChainManagerProvider.provideTransactionChainManager(concurrentConnectionContex);
        Assert.assertFalse(transactionChainManagerRegistration_2.ownedByInvokingConnectionContext());
    }

}