/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.sal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.extension.api.ConverterMessageToOFJava;
import org.opendaylight.openflowplugin.extension.api.ExtensionConvertorData;
import org.opendaylight.openflowplugin.extension.api.TypeVersionKey;
import org.opendaylight.openflowplugin.extension.api.core.extension.ExtensionConverterProvider;
import org.opendaylight.openflowplugin.impl.services.ServiceMocking;
import org.opendaylight.yang.gen.v1.urn.opendaylight.experimenter.message.service.rev151020.SendExperimenterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.experimenter.message.service.rev151020.SendExperimenterInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ExperimenterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.experimenter.types.rev151020.experimenter.core.message.ExperimenterMessageOfChoice;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Test for {@link SendExperimenterImpl}.
 */
public class SendExperimenterImplTest extends ServiceMocking {
    private static final Uint32 DUMMY_XID_VALUE = Uint32.valueOf(100);

    @Mock
    private ExtensionConverterProvider extensionConverterProvider;
    @Mock
    private ConverterMessageToOFJava extensionConverter;

    private SendExperimenterImpl sendExperimenter;

    @Override
    protected void setup() {
        when(extensionConverterProvider.getMessageConverter(ArgumentMatchers.<TypeVersionKey>any()))
            .thenReturn(extensionConverter);
        when(extensionConverter.getExperimenterId()).thenReturn(new ExperimenterId(Uint32.valueOf(43)));
        when(extensionConverter.getType()).thenReturn(Uint32.valueOf(44));

        sendExperimenter = new SendExperimenterImpl(mockedRequestContextStack, mockedDeviceContext,
            extensionConverterProvider);
    }

    @Test
    public void testSendExperimenter() {
        sendExperimenter.invoke(buildSendExperimenterInput());
        verify(mockedRequestContextStack).createRequestContext();
    }

    @Test
    public void testBuildRequest() throws Exception {
        final var sendExperimenterInput = buildSendExperimenterInput();
        final var request = sendExperimenter.buildRequest(new Xid(DUMMY_XID_VALUE), sendExperimenterInput);
        assertEquals(DUMMY_XID_VALUE, request.getXid());
        assertTrue(request instanceof ExperimenterInput);
        final var input = (ExperimenterInput) request;
        assertEquals(43L, input.getExperimenter().getValue().longValue());
        assertEquals(44L, input.getExpType().longValue());
        verify(extensionConverter).convert(eq(sendExperimenterInput.getExperimenterMessageOfChoice()),
                any(ExtensionConvertorData.class));
    }

    private SendExperimenterInput buildSendExperimenterInput() {
        return new SendExperimenterInputBuilder()
            .setNode(new NodeRef(mockedDeviceInfo.getNodeInstanceIdentifier()))
            .setExperimenterMessageOfChoice(new DummyExperimenter())
            .build();
    }

    private static final class DummyExperimenter implements ExperimenterMessageOfChoice {
        @Override
        public Class<DummyExperimenter> implementedInterface() {
            return DummyExperimenter.class;
        }
    }
}
