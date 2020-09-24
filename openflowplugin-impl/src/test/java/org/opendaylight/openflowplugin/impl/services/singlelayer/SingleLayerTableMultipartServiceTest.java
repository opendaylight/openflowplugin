/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.singlelayer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.concurrent.Future;
import org.junit.Test;
import org.opendaylight.openflowplugin.impl.datastore.MultipartWriterProviderFactory;
import org.opendaylight.openflowplugin.impl.services.ServiceMocking;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.MultipartReplyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.MultipartRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTableInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTableInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTableOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.table.update.UpdatedTableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.multipart.reply.multipart.reply.body.MultipartReplyTableFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.multipart.request.multipart.request.body.MultipartRequestTableFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeaturesBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;

public class SingleLayerTableMultipartServiceTest extends ServiceMocking {
    private static final Uint32 MAX_ENTRIES = Uint32.valueOf(42);
    private static final Uint32 XID = Uint32.valueOf(43);
    private SingleLayerTableMultipartService service;

    @Override
    protected void setup() {
        service = new SingleLayerTableMultipartService(mockedRequestContextStack, mockedDeviceContext,
                MultipartWriterProviderFactory.createDefaultProvider(mockedDeviceContext));
    }

    @Test
    public void buildRequest() {
        final UpdateTableInput input = new UpdateTableInputBuilder()
                .setUpdatedTable(new UpdatedTableBuilder()
                        .setTableFeatures(Collections.singletonList(new TableFeaturesBuilder()
                                .setMaxEntries(MAX_ENTRIES)
                                .build()))
                        .build())
                .build();

        final OfHeader ofHeader = service.buildRequest(DUMMY_XID, input);
        assertEquals(MultipartRequest.class, ofHeader.implementedInterface());

        final MultipartRequestTableFeatures result = (MultipartRequestTableFeatures) ((MultipartRequest) ofHeader)
            .getMultipartRequestBody();

        assertEquals(MAX_ENTRIES, result.nonnullTableFeatures().values().iterator().next().getMaxEntries());
    }

    @Test
    public void handleAndReply() throws Exception {
        mockSuccessfulFuture(Collections.singletonList(new MultipartReplyBuilder()
                .setXid(XID)
                .setMultipartReplyBody(new MultipartReplyTableFeaturesBuilder()
                        .setTableFeatures(Collections.singletonList(new TableFeaturesBuilder()
                                .setMaxEntries(MAX_ENTRIES)
                                .build()))
                        .build())
                .build()));

        final UpdateTableInput input = new UpdateTableInputBuilder()
                .setUpdatedTable(new UpdatedTableBuilder()
                        .setTableFeatures(Collections.singletonList(new TableFeaturesBuilder()
                                .setMaxEntries(MAX_ENTRIES)
                                .build()))
                        .build())
                .build();

        final Future<RpcResult<UpdateTableOutput>> rpcResultFuture = service
                .handleAndReply(input);

        final RpcResult<UpdateTableOutput> result =
                rpcResultFuture.get();

        assertTrue(result.isSuccessful());
        assertEquals(Uint64.valueOf(XID), result.getResult().getTransactionId().getValue());
    }
}
