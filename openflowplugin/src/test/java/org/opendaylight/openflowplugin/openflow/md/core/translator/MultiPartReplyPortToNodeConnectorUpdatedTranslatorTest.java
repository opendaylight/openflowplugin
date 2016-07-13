/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.translator;

import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.api.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.SessionContext;
import org.opendaylight.openflowplugin.openflow.md.util.OpenflowPortsUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnectorUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartRequestFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortState;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyPortDescCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.desc._case.MultipartReplyPortDescBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.desc._case.multipart.reply.port.desc.Ports;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.desc._case.multipart.reply.port.desc.PortsBuilder;
import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * @author michal.polkorab
 *
 */
public class MultiPartReplyPortToNodeConnectorUpdatedTranslatorTest {

    @Mock SwitchConnectionDistinguisher cookie;
    @Mock SessionContext sc;
    @Mock GetFeaturesOutput features;

    MultiPartReplyPortToNodeConnectorUpdatedTranslator translator = new MultiPartReplyPortToNodeConnectorUpdatedTranslator();

    /**
     * Initializes mocks
     */
    @Before
    public void startUp() {
        MockitoAnnotations.initMocks(this);
        when(sc.getFeatures()).thenReturn(features);
        when(features.getDatapathId()).thenReturn(new BigInteger("42"));
        OpenflowPortsUtil.init();
    }

    /**
     * Test {@link MultiPartReplyPortToNodeConnectorUpdatedTranslator#translate(SwitchConnectionDistinguisher, SessionContext, OfHeader)}
     * with wrong inputs
     */
    @Test
    public void testWrongInputs() {
        HelloMessageBuilder helloBuilder = new HelloMessageBuilder();
        HelloMessage helloMessage = helloBuilder.build();
        List<DataObject> list = translator.translate(cookie, sc, helloMessage);
        Assert.assertEquals("Wrong output", 0, list.size());
        
        MultipartReplyMessageBuilder builder = new MultipartReplyMessageBuilder();
        builder.setType(MultipartType.OFPMPFLOW);
        MultipartReplyMessage message = builder.build();
        list = translator.translate(cookie, sc, message);
        Assert.assertEquals("Wrong output", 0, list.size());
    }

    /**
     * Test {@link MultiPartReplyPortToNodeConnectorUpdatedTranslator#translate(SwitchConnectionDistinguisher, SessionContext, OfHeader)}
     * with correct input (no ports)
     */
    @Test
    public void testEmptyPortDescWithCorrectInput() {
        MultipartReplyMessageBuilder builder = new MultipartReplyMessageBuilder();
        builder.setVersion((short) EncodeConstants.OF13_VERSION_ID);
        builder.setXid(12345L);
        builder.setType(MultipartType.OFPMPPORTDESC);
        builder.setFlags(new MultipartRequestFlags(false));
        
        MultipartReplyPortDescCaseBuilder caseBuilder = new MultipartReplyPortDescCaseBuilder();
        MultipartReplyPortDescBuilder descBuilder = new MultipartReplyPortDescBuilder();
        List<Ports> ports = new ArrayList<>();
        descBuilder.setPorts(ports);
        caseBuilder.setMultipartReplyPortDesc(descBuilder.build());
        builder.setMultipartReplyBody(caseBuilder.build());
        MultipartReplyMessage message = builder.build();
        
        List<DataObject> list = translator.translate(cookie, sc, message);
        Assert.assertEquals("Wrong output", 0, list.size());
    }

    /**
     * Test {@link MultiPartReplyPortToNodeConnectorUpdatedTranslator#translate(SwitchConnectionDistinguisher, SessionContext, OfHeader)}
     * with correct input
     */
    @Test
    public void testPortDescWithCorrectInput() {
        MultipartReplyMessageBuilder builder = new MultipartReplyMessageBuilder();
        builder.setVersion((short) EncodeConstants.OF13_VERSION_ID);
        builder.setXid(12345L);
        builder.setType(MultipartType.OFPMPPORTDESC);
        builder.setFlags(new MultipartRequestFlags(false));
        
        MultipartReplyPortDescCaseBuilder caseBuilder = new MultipartReplyPortDescCaseBuilder();
        MultipartReplyPortDescBuilder descBuilder = new MultipartReplyPortDescBuilder();
        List<Ports> ports = new ArrayList<>();
        PortsBuilder portBuilder = new PortsBuilder();
        portBuilder.setPortNo(5L);
        portBuilder.setHwAddr(new MacAddress("06:07:08:09:0A:0B"));
        portBuilder.setName("MultipartPortDesc port");
        portBuilder.setConfig(new PortConfig(true, false, true, false));
        portBuilder.setState(new PortState(true, false, true));
        portBuilder.setCurrentFeatures(new PortFeatures(false, true, false, true,
                false, true, false, true, false, true, false, true, false, true, false, true));
        portBuilder.setAdvertisedFeatures(new PortFeatures(true, false, true, false, true,
                false, true, false, true, false, true, false, true, false, true, false));
        portBuilder.setSupportedFeatures(new PortFeatures(false, false, false, false, false,
                false, false, false, false, false, false, false, false, false, false, false));
        portBuilder.setPeerFeatures(new PortFeatures(true, true, true, true, true,
                true, true, true, true, true, true, true, true, true, true, true));
        portBuilder.setCurrSpeed(12L);
        portBuilder.setMaxSpeed(13L);
        ports.add(portBuilder.build());
        portBuilder = new PortsBuilder();
        portBuilder.setPortNo(50L);
        portBuilder.setHwAddr(new MacAddress("05:06:07:08:09:0A"));
        portBuilder.setName("MultipartPortDesc port");
        portBuilder.setConfig(new PortConfig(false, true, false, true));
        portBuilder.setState(new PortState(false, true, false));
        portBuilder.setCurrentFeatures(null);
        portBuilder.setAdvertisedFeatures(null);
        portBuilder.setSupportedFeatures(null);
        portBuilder.setPeerFeatures(null);
        portBuilder.setCurrSpeed(120L);
        portBuilder.setMaxSpeed(130L);
        ports.add(portBuilder.build());
        descBuilder.setPorts(ports);
        caseBuilder.setMultipartReplyPortDesc(descBuilder.build());
        builder.setMultipartReplyBody(caseBuilder.build());
        MultipartReplyMessage message = builder.build();
        
        List<DataObject> list = translator.translate(cookie, sc, message);
        Assert.assertEquals("Wrong output", 2, list.size());
        NodeConnectorUpdated nodeUpdated = (NodeConnectorUpdated) list.get(0);
        Assert.assertEquals("Wrong node-id", "openflow:42:5", nodeUpdated.getId().getValue());
        FlowCapableNodeConnectorUpdated flowCapable = nodeUpdated.getAugmentation(FlowCapableNodeConnectorUpdated.class);
        Assert.assertEquals("Wrong port number", 5, flowCapable.getPortNumber().getUint32().intValue());
        Assert.assertEquals("Wrong hardware address", new MacAddress("06:07:08:09:0A:0B"), flowCapable.getHardwareAddress());
        Assert.assertEquals("Wrong name", "MultipartPortDesc port", flowCapable.getName());
        Assert.assertEquals("Wrong config", true, flowCapable.getConfiguration().isNOFWD());
        Assert.assertEquals("Wrong config", false, flowCapable.getConfiguration().isNOPACKETIN());
        Assert.assertEquals("Wrong config", true, flowCapable.getConfiguration().isNORECV());
        Assert.assertEquals("Wrong config", false, flowCapable.getConfiguration().isPORTDOWN());
        Assert.assertEquals("Wrong state", true, flowCapable.getState().isBlocked());
        Assert.assertEquals("Wrong state", false, flowCapable.getState().isLinkDown());
        Assert.assertEquals("Wrong state", true, flowCapable.getState().isLive());
        Assert.assertEquals("Wrong current features", new org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port
                .rev130925.PortFeatures(false, true, false, true, false, true, false, false, true, false, true, false,
                        true, true, false, true), flowCapable.getCurrentFeature());
        Assert.assertEquals("Wrong advertised features", new org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port
                .rev130925.PortFeatures(true, false, true, false, true, false, true, true, false, true, false, true,
                        false, false, true, false), flowCapable.getAdvertisedFeatures());
        Assert.assertEquals("Wrong supported features", new org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port
                .rev130925.PortFeatures(false, false, false, false, false, false, false, false, false, false, false, false,
                        false, false, false, false), flowCapable.getSupported());
        Assert.assertEquals("Wrong peer features", new org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port
                .rev130925.PortFeatures(true, true, true, true, true, true, true, true, true, true, true, true,
                        true, true, true, true), flowCapable.getPeerFeatures());
        Assert.assertEquals("Wrong current speed", 12, flowCapable.getCurrentSpeed().intValue());
        Assert.assertEquals("Wrong max speed", 13, flowCapable.getMaximumSpeed().intValue());
        nodeUpdated = (NodeConnectorUpdated) list.get(1);
        Assert.assertEquals("Wrong node-id", "openflow:42:50", nodeUpdated.getId().getValue());
        flowCapable = nodeUpdated.getAugmentation(FlowCapableNodeConnectorUpdated.class);
        Assert.assertEquals("Wrong port number", 50, flowCapable.getPortNumber().getUint32().intValue());
        Assert.assertEquals("Wrong hardware address", new MacAddress("05:06:07:08:09:0A"), flowCapable.getHardwareAddress());
        Assert.assertEquals("Wrong name", "MultipartPortDesc port", flowCapable.getName());
        Assert.assertEquals("Wrong config", false, flowCapable.getConfiguration().isNOFWD());
        Assert.assertEquals("Wrong config", true, flowCapable.getConfiguration().isNOPACKETIN());
        Assert.assertEquals("Wrong config", false, flowCapable.getConfiguration().isNORECV());
        Assert.assertEquals("Wrong config", true, flowCapable.getConfiguration().isPORTDOWN());
        Assert.assertEquals("Wrong state", false, flowCapable.getState().isBlocked());
        Assert.assertEquals("Wrong state", true, flowCapable.getState().isLinkDown());
        Assert.assertEquals("Wrong state", false, flowCapable.getState().isLive());
        Assert.assertEquals("Wrong current features", null, flowCapable.getCurrentFeature());
        Assert.assertEquals("Wrong advertised features", null, flowCapable.getAdvertisedFeatures());
        Assert.assertEquals("Wrong supported features", null, flowCapable.getSupported());
        Assert.assertEquals("Wrong peer features", null, flowCapable.getPeerFeatures());
        Assert.assertEquals("Wrong current speed", 120, flowCapable.getCurrentSpeed().intValue());
        Assert.assertEquals("Wrong max speed", 130, flowCapable.getMaximumSpeed().intValue());
    }
}
