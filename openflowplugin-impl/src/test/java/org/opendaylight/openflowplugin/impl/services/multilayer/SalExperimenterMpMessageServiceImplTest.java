/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.services.multilayer;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.extension.api.ConvertorMessageToOFJava;
import org.opendaylight.openflowplugin.extension.api.TypeVersionKey;
import org.opendaylight.openflowplugin.extension.api.core.extension.ExtensionConverterProvider;
import org.opendaylight.openflowplugin.impl.services.ServiceMocking;
import org.opendaylight.openflowplugin.impl.services.sal.SalExperimenterMpMessageServiceImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.experimenter.mp.message.service.rev151020.SendExperimenterMpRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.experimenter.mp.message.service.rev151020.SendExperimenterMpRequestInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ExperimenterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestExperimenterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.experimenter._case.MultipartRequestExperimenter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.experimenter.types.rev151020.experimenter.core.message.ExperimenterMessageOfChoice;
import org.opendaylight.yangtools.yang.binding.DataContainer;

public class SalExperimenterMpMessageServiceImplTest extends ServiceMocking {
    private static final long DUMMY_ID = 42L;
    private SalExperimenterMpMessageServiceImpl salExperimenterMpMessageService;

    @Mock
    private ExtensionConverterProvider mockedExtensionConverterProvider;

    @Mock
    private ConvertorMessageToOFJava<ExperimenterMessageOfChoice, DataContainer> mockedExtensionConverter;

    @Override
    protected void setup() {
        when(mockedExtensionConverterProvider.getMessageConverter(Matchers.<TypeVersionKey>any()))
                .thenReturn(mockedExtensionConverter);

        when(mockedExtensionConverter.getExperimenterId())
                .thenReturn(new ExperimenterId(DUMMY_ID));

        when(mockedExtensionConverter.getType())
                .thenReturn((long) MultipartType.OFPMPEXPERIMENTER.getIntValue());

        salExperimenterMpMessageService = new SalExperimenterMpMessageServiceImpl(
                mockedRequestContextStack,
                mockedDeviceContext,
                mockedExtensionConverterProvider);
    }

    @Test
    public void sendExperimenterMpRequest() throws Exception {
        salExperimenterMpMessageService.sendExperimenterMpRequest(new SendExperimenterMpRequestInputBuilder()
                .setExperimenterMessageOfChoice(new DummyExperimenter())
                .setNode(new NodeRef(mockedDeviceInfo.getNodeInstanceIdentifier()))
                .build());

        verify(mockedRequestContextStack).createRequestContext();
    }

    @Test
    public void buildRequest() throws Exception {
        final SendExperimenterMpRequestInput data = new SendExperimenterMpRequestInputBuilder()
                .setExperimenterMessageOfChoice(new DummyExperimenter())
                .setNode(new NodeRef(mockedDeviceInfo.getNodeInstanceIdentifier()))
                .build();

        final OfHeader ofHeader = new MultiLayerExperimenterMultipartService(mockedDeviceContext, mockedDeviceContext, mockedExtensionConverterProvider)
            .buildRequestTest(new Xid(DUMMY_ID), data);
        verify(mockedExtensionConverter).convert(data.getExperimenterMessageOfChoice());
        assertEquals(DUMMY_ID, (long) ofHeader.getXid());
        assertEquals(mockedDeviceInfo.getVersion(), (short) ofHeader.getVersion());
        assertEquals(MultipartRequestInput.class, ofHeader.getImplementedInterface());

        final MultipartRequestInput input = MultipartRequestInput.class.cast(ofHeader);
        assertEquals(MultipartRequestExperimenterCase.class, input.getMultipartRequestBody().getImplementedInterface());

        final MultipartRequestExperimenter multipartRequestExperimenter =
                MultipartRequestExperimenterCase.class.cast(input.getMultipartRequestBody())
                        .getMultipartRequestExperimenter();

        assertEquals(DUMMY_ID, (long) multipartRequestExperimenter.getExperimenter().getValue());
        assertEquals(MultipartType.OFPMPEXPERIMENTER.getIntValue(), (long) multipartRequestExperimenter.getExpType());
    }

    private static class DummyExperimenter implements ExperimenterMessageOfChoice {
        @Override
        public Class<? extends DataContainer> getImplementedInterface() {
            return DummyExperimenter.class;
        }
    }
}
