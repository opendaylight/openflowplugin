/*
 * Copyright (c) 2016 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.services;

/**
 * Created by hyy on 2016/9/27.
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.extension.api.ConvertorMessageToOFJava;
import org.opendaylight.openflowplugin.extension.api.TypeVersionKey;
import org.opendaylight.openflowplugin.extension.api.core.extension.ExtensionConverterProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.experimenter.mp.message.service.rev151020.SendExperimenterMpRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.experimenter.mp.message.service.rev151020.SendExperimenterMpRequestInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ExperimenterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.MultipartRequestBody;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestExperimenterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.experimenter.types.rev151020.experimenter.core.message.ExperimenterMessageOfChoice;
import org.opendaylight.yangtools.yang.binding.DataContainer;

/**
 * Test for {@link SalExperimenterMpMessageServiceImpl}.
 */
public class SalExperimenterMpMessageServiceImplTest extends ServiceMocking {

    private static final Long DUMMY_XID_VALUE = 100L;
    private SalExperimenterMpMessageServiceImpl salExperimenterMpMessageService;
    @Mock
    private ExtensionConverterProvider extensionConverterProvider;
    @Mock
    private ConvertorMessageToOFJava extensionConverter;

    @Override
    protected void setup() {
        salExperimenterMpMessageService = new SalExperimenterMpMessageServiceImpl(mockedRequestContextStack, mockedDeviceContext);
        Mockito.when(mockedDeviceContext.getExtensionConverterProvider()).thenReturn(extensionConverterProvider);
        Mockito.when(extensionConverterProvider.getMessageConverter(Matchers.<TypeVersionKey>any()))
                .thenReturn(extensionConverter);
        Mockito.when(extensionConverter.getExperimenterId()).thenReturn(new ExperimenterId(43L));
        Mockito.when(extensionConverter.getType()).thenReturn(44L);
    }

    @Test
    public void testsendExperimenterMpRequest() throws Exception {
        SendExperimenterMpRequestInput sendExperimenterMpRequestInput = buildSendExperimenterMpRequestInput();
        this.<List<MultipartReply>>mockSuccessfulFuture();
        salExperimenterMpMessageService.sendExperimenterMpRequest(sendExperimenterMpRequestInput);
        Mockito.verify(mockedRequestContextStack).createRequestContext();
    }

    @Test
    public void testBuildRequest() throws Exception {
        SendExperimenterMpRequestInput sendExperimenterMpRequestInput = buildSendExperimenterMpRequestInput();
        final OfHeader request = salExperimenterMpMessageService.buildRequest(new Xid(DUMMY_XID_VALUE), sendExperimenterMpRequestInput);
        assertEquals(DUMMY_XID_VALUE, request.getXid());
        assertTrue(request instanceof MultipartRequestInput);
        final MultipartRequestInput input = (MultipartRequestInput) request;
        assertEquals(MultipartType.OFPMPEXPERIMENTER, input.getType());
        final MultipartRequestBody mpRequestBody = input.getMultipartRequestBody();
        assertTrue(mpRequestBody instanceof MultipartRequestExperimenterCase);
        final MultipartRequestExperimenterCase mpExperimenterCase = (MultipartRequestExperimenterCase)mpRequestBody;
        assertEquals(43L, mpExperimenterCase.getMultipartRequestExperimenter().getExperimenter().getValue().longValue());
        assertEquals(44L, mpExperimenterCase.getMultipartRequestExperimenter().getExpType().longValue());
        Mockito.verify(extensionConverter).convert(sendExperimenterMpRequestInput.getExperimenterMessageOfChoice());
    }

    private SendExperimenterMpRequestInput buildSendExperimenterMpRequestInput() {
        SendExperimenterMpRequestInputBuilder sendExperimenterMpRequestInputBld = new SendExperimenterMpRequestInputBuilder()
                .setNode(new NodeRef(mockedDeviceState.getNodeInstanceIdentifier()))
                .setExperimenterMessageOfChoice(new SalExperimenterMpMessageServiceImplTest.DummyExperimenter());
        return sendExperimenterMpRequestInputBld.build();
    }

    private static class DummyExperimenter implements ExperimenterMessageOfChoice {
        @Override
        public Class<? extends DataContainer> getImplementedInterface() {
            return SalExperimenterMpMessageServiceImplTest.DummyExperimenter.class;
        }
    }
}
