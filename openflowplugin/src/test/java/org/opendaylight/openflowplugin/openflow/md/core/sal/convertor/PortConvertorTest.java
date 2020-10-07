/*
 * Copyright (c) 2014 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import java.util.Optional;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionConvertorData;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnectorBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.FlowCapablePort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortNumberUni;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.flow.capable.port.State;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.flow.capable.port.StateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.port.mod.port.PortBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortConfigV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortFeaturesV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortState;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortModInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.desc._case.multipart.reply.port.desc.Ports;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.desc._case.multipart.reply.port.desc.PortsBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;

public class PortConvertorTest {

    private static final String DEFAULT_MAC_ADDRESS = "01:02:03:04:05:06";

    private final PortFeatures features = new PortFeatures(true, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null);

    private final PortConfig config = new PortConfig(false, false, false, false);

    private final org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortFeatures portf31 =
            new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortFeatures(
                    null, null, null, null, null, null, null, null, null, null, true, null, null, null, null, null);

    private final org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortConfig config31 =
            new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortConfig(
                    false, false, false, false);

    private final org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortConfig
        configMask31 = new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortConfig(
            true, true, true, true);

    private final PortConfigV10 portConfMaskV10 = new PortConfigV10(true, true, true, true, true, true, true);

    /**
     * test of {@link PortConvertor#convert(Port, VersionConvertorData)} }.
     */
    @Test
    public void testtoPortModInputwithAllParameters() {
        PortBuilder portBld = new PortBuilder()
            .setPortModOrder(Uint32.ZERO)
            .setAdvertisedFeatures(features)
            .setConfiguration(config)
            .setPortNumber(new PortNumberUni(Uint32.valueOf(42)))
            .setHardwareAddress(new MacAddress(DEFAULT_MAC_ADDRESS));

        VersionConvertorData data = new VersionConvertorData(OFConstants.OFP_VERSION_1_3);
        final ConvertorManager convertorManager = ConvertorManagerFactory.createDefaultManager();
        Optional<PortModInput> portOutOptional = convertorManager.convert(portBld.build(), data);
        final PortModInput portOut = portOutOptional.orElse(PortConvertor.defaultResult(OFConstants.OFP_VERSION_1_3));

        PortConfigV10 portConfV10 = new PortConfigV10(false, false, false, false, true, true, false);

        PortModInputBuilder portModInputBld = new PortModInputBuilder();

        portModInputBld.setConfig(config31);
        portModInputBld.setMask(configMask31);
        portModInputBld.setPortNo(new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731
            .PortNumber(Uint32.valueOf(42)));
        portModInputBld.setHwAddress(new MacAddress(DEFAULT_MAC_ADDRESS));
        portModInputBld.setAdvertise(portf31);

        portModInputBld.setConfigV10(portConfV10);
        portModInputBld.setMaskV10(portConfMaskV10);
        portModInputBld.setAdvertiseV10(
                new PortFeaturesV10(null, null, null, null, null, null, null, true, null, null, null, null));

        portModInputBld.setVersion(EncodeConstants.OF_VERSION_1_3);

        Assert.assertEquals(portModInputBld.build(), portOut);
    }

    /**
     * test of {@link PortConvertor#toPortDesc(FlowCapablePort , short)}.
     */
    @Test
    public void testtoPortDescwithAllParameters() {

        State state = new StateBuilder().setBlocked(false).setLinkDown(false).setLive(false).build();

        FlowCapableNodeConnectorBuilder flowCapableNodeConnectorBuilder = new FlowCapableNodeConnectorBuilder();

        flowCapableNodeConnectorBuilder.setAdvertisedFeatures(features);
        flowCapableNodeConnectorBuilder.setConfiguration(config);
        flowCapableNodeConnectorBuilder.setCurrentFeature(features);
        flowCapableNodeConnectorBuilder.setCurrentSpeed((Uint32) null);
        flowCapableNodeConnectorBuilder.setHardwareAddress(new MacAddress(DEFAULT_MAC_ADDRESS));
        flowCapableNodeConnectorBuilder.setMaximumSpeed((Uint32) null);
        flowCapableNodeConnectorBuilder.setName("foo");
        flowCapableNodeConnectorBuilder.setPeerFeatures(features);
        flowCapableNodeConnectorBuilder.setPortNumber(new PortNumberUni(Uint32.valueOf(42L)));
        flowCapableNodeConnectorBuilder.setState(state);
        flowCapableNodeConnectorBuilder.setSupported(features);

        final Ports portsOut = PortConvertor.toPortDesc(
                flowCapableNodeConnectorBuilder.build(), EncodeConstants.OF13_VERSION_ID);

        PortsBuilder portsB = new PortsBuilder();

        portsB.setAdvertisedFeatures(portf31);
        portsB.setConfig(config31);
        portsB.setCurrentFeatures(portf31);
        portsB.setCurrSpeed((Uint32) null);
        portsB.setHwAddr(new MacAddress(DEFAULT_MAC_ADDRESS));
        portsB.setMaxSpeed((Uint32) null);
        portsB.setName("foo");
        portsB.setPeerFeatures(portf31);
        portsB.setPortNo(Uint32.valueOf(42));
        portsB.setState(new PortState(false, false, false));
        portsB.setSupportedFeatures(portf31);

        Assert.assertEquals(portsB.build(), portsOut);
    }
}
