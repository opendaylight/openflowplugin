/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.sal;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.SettableFuture;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.openflowplugin.impl.datastore.MultipartWriterProviderFactory;
import org.opendaylight.openflowplugin.impl.services.ServiceMocking;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManager;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManagerFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyTableFeaturesCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table.features._case.MultipartReplyTableFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table.features._case.multipart.reply.table.features.TableFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTableInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTableInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTableOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.table.update.UpdatedTableBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint8;

public class SalTableServiceImplTest extends ServiceMocking {

    @Mock
    RpcProviderService mockedRpcProviderRegistry;

    private SettableFuture<Object> handleResultFuture;
    private SalTableServiceImpl salTableService;

    @Override
    public void setup() {
        handleResultFuture = SettableFuture.create();
        when(mockedRequestContext.getFuture()).thenReturn(handleResultFuture);
        Mockito.doAnswer((Answer<Void>) invocation -> {
            final FutureCallback<OfHeader> callback = (FutureCallback<OfHeader>) invocation.getArguments()[2];
            callback.onSuccess(null);
            return null;
        })
                .when(mockedOutboundQueue).commitEntry(
                anyLong(), ArgumentMatchers.any(), ArgumentMatchers.any());

        final ConvertorManager convertorManager = ConvertorManagerFactory.createDefaultManager();
        salTableService = new SalTableServiceImpl(mockedRequestContextStack, mockedDeviceContext,
                convertorManager, MultipartWriterProviderFactory.createDefaultProvider(mockedDeviceContext));
    }

    @Test
    public void testUpdateTableFail1() {
        Mockito.doAnswer((Answer<Void>) invocation -> {
            final RpcResult<List<MultipartReply>> rpcResult =
                    RpcResultBuilder.<List<MultipartReply>>failed().build();
            handleResultFuture.set(rpcResult);
            return null;
        }).when(multiMessageCollector).endCollecting(ArgumentMatchers.any());

        final Future<RpcResult<UpdateTableOutput>> rpcResultFuture = salTableService.updateTable(prepareUpdateTable());
        Assert.assertNotNull(rpcResultFuture);
        verify(mockedRequestContextStack).createRequestContext();
    }

    @Test
    public void testUpdateTableFail2() {
        Mockito.doAnswer((Answer<Void>) invocation -> {
            final RpcResult<List<MultipartReply>> rpcResult =
                    RpcResultBuilder.success(Collections.<MultipartReply>emptyList())
                    .build();
            handleResultFuture.set(rpcResult);
            return null;
        }).when(multiMessageCollector).endCollecting(ArgumentMatchers.any());

        final Future<RpcResult<UpdateTableOutput>> rpcResultFuture = salTableService.updateTable(prepareUpdateTable());
        Assert.assertNotNull(rpcResultFuture);
        verify(mockedRequestContextStack).createRequestContext();
    }

    @Test
    public void testUpdateTableSuccess() {
        Mockito.doAnswer((Answer<Void>) invocation -> {
            TableFeaturesBuilder tableFeaturesBld = new TableFeaturesBuilder()
                    .setTableId(Uint8.ZERO)
                    .setName("Zafod")
                    .setMaxEntries(Uint32.valueOf(42))
                    .setTableFeatureProperties(Collections.emptyList());
            MultipartReplyTableFeaturesBuilder mpTableFeaturesBld = new MultipartReplyTableFeaturesBuilder()
                    .setTableFeatures(Collections.singletonList(tableFeaturesBld.build()));
            MultipartReplyTableFeaturesCaseBuilder mpBodyBld = new MultipartReplyTableFeaturesCaseBuilder()
                    .setMultipartReplyTableFeatures(mpTableFeaturesBld.build());
            MultipartReplyMessageBuilder mpResultBld = new MultipartReplyMessageBuilder()
                    .setType(MultipartType.OFPMPTABLEFEATURES)
                    .setMultipartReplyBody(mpBodyBld.build())
                    .setXid(Uint32.valueOf(21));
            final RpcResult<List<MultipartReply>> rpcResult = RpcResultBuilder
                    .success(Collections.singletonList((MultipartReply) mpResultBld.build()))
                    .build();
            handleResultFuture.set(rpcResult);
            return null;
        }).when(multiMessageCollector).endCollecting(ArgumentMatchers.any());

        final Future<RpcResult<UpdateTableOutput>> rpcResultFuture = salTableService.updateTable(prepareUpdateTable());
        Assert.assertNotNull(rpcResultFuture);
        verify(mockedRequestContextStack).createRequestContext();
    }

    private static UpdateTableInput prepareUpdateTable() {
        UpdateTableInputBuilder updateTableInputBuilder = new UpdateTableInputBuilder();
        UpdatedTableBuilder updatedTableBuilder = new UpdatedTableBuilder();
        updatedTableBuilder.setTableFeatures(Collections.emptyMap());
        updateTableInputBuilder.setUpdatedTable(updatedTableBuilder.build());
        return updateTableInputBuilder.build();
    }

}
