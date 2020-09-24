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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.experimenter.types.rev151020.experimenter.core.message.ExperimenterMessageOfChoice;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Test for {@link org.opendaylight.openflowplugin.impl.services.sal.SalExperimenterMessageServiceImpl}.
 */
public class SalExperimenterMessageServiceImplTest extends ServiceMocking {

    private static final Uint32 DUMMY_XID_VALUE = Uint32.valueOf(100);
    private SalExperimenterMessageServiceImpl salExperimenterMessageService;
    @Mock
    private ExtensionConverterProvider extensionConverterProvider;
    @Mock
    private ConverterMessageToOFJava extensionConverter;

    @Override
    protected void setup() {
        salExperimenterMessageService = new SalExperimenterMessageServiceImpl(mockedRequestContextStack,
                                                                              mockedDeviceContext,
                                                                              extensionConverterProvider);
        Mockito.when(extensionConverterProvider.getMessageConverter(ArgumentMatchers.<TypeVersionKey>any()))
                .thenReturn(extensionConverter);
        Mockito.when(extensionConverter.getExperimenterId()).thenReturn(new ExperimenterId(Uint32.valueOf(43)));
        Mockito.when(extensionConverter.getType()).thenReturn(44L);
    }

    @Test
    public void testSendExperimenter() {
        SendExperimenterInput sendExperimenterInput = buildSendExperimenterInput();
        salExperimenterMessageService.sendExperimenter(sendExperimenterInput);
        verify(mockedRequestContextStack).createRequestContext();
    }

    @Test
    public void testBuildRequest() throws Exception {
        SendExperimenterInput sendExperimenterInput = buildSendExperimenterInput();
        final OfHeader request =
                salExperimenterMessageService.buildRequest(new Xid(DUMMY_XID_VALUE), sendExperimenterInput);
        assertEquals(DUMMY_XID_VALUE, request.getXid());
        assertTrue(request instanceof ExperimenterInput);
        final ExperimenterInput input = (ExperimenterInput) request;
        assertEquals(43L, input.getExperimenter().getValue().longValue());
        assertEquals(44L, input.getExpType().longValue());
        Mockito.verify(extensionConverter).convert(eq(sendExperimenterInput.getExperimenterMessageOfChoice()),
                ArgumentMatchers.any(ExtensionConvertorData.class));
    }

    private SendExperimenterInput buildSendExperimenterInput() {
        SendExperimenterInputBuilder sendExperimenterInputBld = new SendExperimenterInputBuilder()
                .setNode(new NodeRef(mockedDeviceInfo.getNodeInstanceIdentifier()))
                .setExperimenterMessageOfChoice(new DummyExperimenter());
        return sendExperimenterInputBld.build();
    }

    private static class DummyExperimenter implements ExperimenterMessageOfChoice {
        @Override
        public Class<DummyExperimenter> implementedInterface() {
            return DummyExperimenter.class;
        }
    }
}
