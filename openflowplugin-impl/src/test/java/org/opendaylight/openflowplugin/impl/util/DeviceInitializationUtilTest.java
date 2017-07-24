/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.util;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.util.concurrent.Futures;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.TxFacade;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodesBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

@RunWith(MockitoJUnitRunner.class)
public class DeviceInitializationUtilTest {
    @Mock
    private DataBroker dataBroker;
    @Mock
    private WriteTransaction writeTransaction;
    @Mock
    private TxFacade txFacade;
    @Mock
    private DeviceInfo deviceInfo;

    @Before
    public void setUp() throws Exception {
        when(deviceInfo.getNodeInstanceIdentifier()).thenReturn(DeviceStateUtil
                .createNodeInstanceIdentifier(new NodeId("openflow:1")));
        when(writeTransaction.submit()).thenReturn(Futures.immediateCheckedFuture(null));
        when(dataBroker.newWriteOnlyTransaction()).thenReturn(writeTransaction);
    }

    @Test
    public void makeEmptyNodes() throws Exception {
        DeviceInitializationUtil.makeEmptyNodes(dataBroker);
        verify(dataBroker).newWriteOnlyTransaction();
        verify(writeTransaction).merge(LogicalDatastoreType.OPERATIONAL, InstanceIdentifier
                .create(Nodes.class), new NodesBuilder()
                .setNode(Collections.emptyList())
                .build());
        verify(writeTransaction).submit();
    }

    @Test
    public void makeEmptyTables() throws Exception {
        DeviceInitializationUtil.makeEmptyTables(txFacade, deviceInfo, (short) 10);
        verify(txFacade, times(10)).writeToTransaction(
                eq(LogicalDatastoreType.OPERATIONAL), any(), any());
    }

    @Test
    public void getIpAddress() throws Exception {
    }

    @Test
    public void getPortNumber() throws Exception {
    }

    @Test
    public void getSwitchFeatures() throws Exception {
    }

}