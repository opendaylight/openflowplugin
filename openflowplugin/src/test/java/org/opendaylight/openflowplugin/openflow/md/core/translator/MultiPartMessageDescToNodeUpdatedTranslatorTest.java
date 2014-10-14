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
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.api.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.SessionContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartRequestFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyDescCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.desc._case.MultipartReplyDescBuilder;
import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * @author michal.polkorab
 *
 */
public class MultiPartMessageDescToNodeUpdatedTranslatorTest {

    @Mock SwitchConnectionDistinguisher cookie;
    @Mock SessionContext sc;
    @Mock GetFeaturesOutput features;

    MultiPartMessageDescToNodeUpdatedTranslator translator = new MultiPartMessageDescToNodeUpdatedTranslator();

    /**
     * Initializes mocks
     */
    @Before
    public void startUp() {
        MockitoAnnotations.initMocks(this);
        when(sc.getFeatures()).thenReturn(features);
        when(features.getDatapathId()).thenReturn(new BigInteger("42"));
    }

    /**
     * Test {@link MultiPartMessageDescToNodeUpdatedTranslator#translate(SwitchConnectionDistinguisher, SessionContext, OfHeader)}
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
     * Test {@link MultiPartMessageDescToNodeUpdatedTranslator#translate(SwitchConnectionDistinguisher, SessionContext, OfHeader)}
     * with correct input
     */
    @Test
    public void testCorrectInput() {
        MultipartReplyMessageBuilder builder = new MultipartReplyMessageBuilder();
        builder.setVersion((short) EncodeConstants.OF13_VERSION_ID);
        builder.setXid(12345L);
        builder.setType(MultipartType.OFPMPDESC);
        builder.setFlags(new MultipartRequestFlags(false));
        
        MultipartReplyDescCaseBuilder caseBuilder = new MultipartReplyDescCaseBuilder();
        MultipartReplyDescBuilder descBuilder = new MultipartReplyDescBuilder();
        descBuilder.setMfrDesc("Collaboration ltd.");
        descBuilder.setHwDesc("Simple switch");
        descBuilder.setSwDesc("Best software ever inside");
        descBuilder.setSerialNum("123456789");
        descBuilder.setDpDesc("0000000000000001");
        caseBuilder.setMultipartReplyDesc(descBuilder.build());
        builder.setMultipartReplyBody(caseBuilder.build());
        MultipartReplyMessage message = builder.build();
        
        List<DataObject> list = translator.translate(cookie, sc, message);
        Assert.assertEquals("Wrong output", 1, list.size());
        NodeUpdated nodeUpdated = (NodeUpdated) list.get(0);
        Assert.assertEquals("Wrong node-id", "openflow:42", nodeUpdated.getId().getValue());
        FlowCapableNodeUpdated flowCapable = nodeUpdated.getAugmentation(FlowCapableNodeUpdated.class);
        Assert.assertEquals("Wrong manufacturer", "Collaboration ltd.", flowCapable.getManufacturer());
        Assert.assertEquals("Wrong hardware", "Simple switch", flowCapable.getHardware());
        Assert.assertEquals("Wrong software", "Best software ever inside", flowCapable.getSoftware());
        Assert.assertEquals("Wrong serial number", "123456789", flowCapable.getSerialNumber());
        Assert.assertEquals("Wrong datapath description", "0000000000000001", flowCapable.getDescription());
    }
}