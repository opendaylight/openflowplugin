/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.util;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.util.concurrent.Futures;
import java.net.InetSocketAddress;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.TxFacade;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IetfInetUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.flow.node.SwitchFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.Capabilities;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

@RunWith(MockitoJUnitRunner.class)
public class DeviceInitializationUtilTest {
    private static final KeyedInstanceIdentifier<Node, NodeKey> NODE_II = DeviceStateUtil
                .createNodeInstanceIdentifier(new NodeId("openflow:1"));
    private static final int PORT = 2017;
    private static final InetSocketAddress INET_SOCKET_ADDRESS = new InetSocketAddress("192.168.0.1", PORT);
    private static final short TABLES = 25;

    @Mock
    private DataBroker dataBroker;
    @Mock
    private WriteTransaction writeTransaction;
    @Mock
    private TxFacade txFacade;
    @Mock
    private DeviceInfo deviceInfo;
    @Mock
    private ConnectionAdapter connectionAdapter;
    @Mock
    private ConnectionContext connectionContext;
    @Mock
    private FeaturesReply featuresReply;

    @Before
    public void setUp() throws Exception {
        when(deviceInfo.getNodeInstanceIdentifier()).thenReturn(NODE_II);
        when(writeTransaction.submit()).thenReturn(Futures.immediateCheckedFuture(null));
        when(dataBroker.newWriteOnlyTransaction()).thenReturn(writeTransaction);
        when(connectionAdapter.getRemoteAddress()).thenReturn(INET_SOCKET_ADDRESS);
        when(featuresReply.getTables()).thenReturn(TABLES);
        when(featuresReply.getVersion()).thenReturn(OFConstants.OFP_VERSION_1_3);
        when(featuresReply.getCapabilities()).thenReturn(new Capabilities(false, false,
                false, false, false, false, false));
        when(connectionContext.getFeatures()).thenReturn(featuresReply);
        when(connectionContext.getConnectionAdapter()).thenReturn(connectionAdapter);
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
        final IpAddress ipAddress = DeviceInitializationUtil.getIpAddress(connectionContext, NODE_II);
        assertEquals(ipAddress, IetfInetUtil.INSTANCE.ipAddressFor(INET_SOCKET_ADDRESS.getAddress()));
    }

    @Test
    public void getPortNumber() throws Exception {
        final PortNumber portNumber = DeviceInitializationUtil.getPortNumber(connectionContext, NODE_II);
        assertEquals(portNumber, new PortNumber(PORT));
    }

    @Test
    public void getSwitchFeatures() throws Exception {
        final SwitchFeatures switchFeatures = DeviceInitializationUtil.getSwitchFeatures(connectionContext);
        assertEquals(TABLES, switchFeatures.getMaxTables().shortValue());
    }

}