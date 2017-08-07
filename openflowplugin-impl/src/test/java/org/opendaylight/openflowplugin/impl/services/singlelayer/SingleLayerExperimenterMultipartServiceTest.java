/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.services.singlelayer;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.concurrent.Future;
import org.junit.Test;
import org.opendaylight.openflowplugin.impl.services.ServiceMocking;
import org.opendaylight.yang.gen.v1.urn.opendaylight.experimenter.mp.message.service.rev151020.SendExperimenterMpRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.experimenter.mp.message.service.rev151020.SendExperimenterMpRequestInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.experimenter.mp.message.service.rev151020.SendExperimenterMpRequestOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.MultipartReplyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.MultipartRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.experimenter.types.rev151020.multipart.reply.multipart.reply.body.MultipartReplyExperimenterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.experimenter.types.rev151020.multipart.request.multipart.request.body.MultipartRequestExperimenter;
import org.opendaylight.yangtools.yang.common.RpcResult;

public class SingleLayerExperimenterMultipartServiceTest extends ServiceMocking {
    private SingleLayerExperimenterMultipartService service;

    @Override
    protected void setup() throws Exception {
        service = new SingleLayerExperimenterMultipartService(
                mockedRequestContextStack, mockedDeviceContext,
                mockedExtensionConverterProvider);
    }

    @Test
    public void buildRequest() throws Exception {
        final SendExperimenterMpRequestInput input = new SendExperimenterMpRequestInputBuilder()
                .setExperimenterMessageOfChoice(mockExperimenter())
                .build();

        final OfHeader ofHeader = service.buildRequest(DUMMY_XID, input);
        assertEquals(MultipartRequest.class, ofHeader.getImplementedInterface());

        final MultipartRequestExperimenter result = MultipartRequestExperimenter.class.cast(
                MultipartRequest.class.cast(ofHeader)
                        .getMultipartRequestBody());

        assertEquals(DummyExperimenter.class, result.getExperimenterMessageOfChoice().getImplementedInterface());
    }

    @Test
    public void handleAndReply() throws Exception {
        mockSuccessfulFuture(Collections.singletonList(new MultipartReplyBuilder()
                .setMultipartReplyBody(new MultipartReplyExperimenterBuilder()
                        .setExperimenterMessageOfChoice(mockExperimenter())
                        .build())
                .build()));

        final SendExperimenterMpRequestInput input = new SendExperimenterMpRequestInputBuilder()
                .setExperimenterMessageOfChoice(mockExperimenter())
                .build();

        final Future<RpcResult<SendExperimenterMpRequestOutput>> rpcResultFuture = service
                .handleAndReply(input);

        final RpcResult<SendExperimenterMpRequestOutput> sendExperimenterMpRequestOutputRpcResult =
                rpcResultFuture.get();

        assertEquals(DummyExperimenter.class, sendExperimenterMpRequestOutputRpcResult
                .getResult()
                .getExperimenterCoreMessageItem()
                .get(0)
                .getExperimenterMessageOfChoice().getImplementedInterface());
    }
}