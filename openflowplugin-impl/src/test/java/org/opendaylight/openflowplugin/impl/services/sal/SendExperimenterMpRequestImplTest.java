/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.sal;

import static org.mockito.Mockito.verify;

import java.util.List;
import org.junit.Test;
import org.opendaylight.openflowplugin.impl.services.ServiceMocking;
import org.opendaylight.yang.gen.v1.urn.opendaylight.experimenter.mp.message.service.rev151020.SendExperimenterMpRequestInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;

public class SendExperimenterMpRequestImplTest extends ServiceMocking {
    private SendExperimenterMpRequestImpl sendExperimenterMpRequest;

    @Override
    protected void setup() {
        this.<List<MultipartReply>>mockSuccessfulFuture();

        sendExperimenterMpRequest = new SendExperimenterMpRequestImpl(mockedRequestContextStack, mockedDeviceContext,
            mockedExtensionConverterProvider);
    }

    @Test
    public void sendExperimenterMpRequest() {
        sendExperimenterMpRequest.invoke(new SendExperimenterMpRequestInputBuilder()
            .setExperimenterMessageOfChoice(mockExperimenter())
            .setNode(new NodeRef(mockedDeviceInfo.getNodeInstanceIdentifier().toIdentifier()))
            .build());
        verify(mockedRequestContextStack).createRequestContext();
    }
}