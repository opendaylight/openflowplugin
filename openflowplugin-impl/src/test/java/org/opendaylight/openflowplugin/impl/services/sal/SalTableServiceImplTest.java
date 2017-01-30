/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.sal;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.SettableFuture;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.EventIdentifier;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.table.features.properties.grouping.TableFeatureProperties;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTableInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTableInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTableOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.table.update.UpdatedTableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeatures;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

public class SalTableServiceImplTest extends ServiceMocking {

    private static final BigInteger DUMMY_DATAPATH_ID = new BigInteger("444");
    private static final Short DUMMY_VERSION = OFConstants.OFP_VERSION_1_3;
    private static final int DUMMY_MAX_REQUEST = 88;

    @Mock
    RpcProviderRegistry mockedRpcProviderRegistry;

    private SettableFuture<Object> handleResultFuture;
    private SalTableServiceImpl salTableService;

    @Override
    public void setup() {
        handleResultFuture = SettableFuture.create();
        when(mockedRequestContext.getFuture()).thenReturn(handleResultFuture);
        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                final FutureCallback<OfHeader> callback = (FutureCallback<OfHeader>) invocation.getArguments()[2];
                callback.onSuccess(null);
                return null;
            }
        })
                .when(mockedOutboundQueue).commitEntry(
                Matchers.anyLong(), Matchers.<OfHeader>any(), Matchers.<FutureCallback<OfHeader>>any());

        final ConvertorManager convertorManager = ConvertorManagerFactory.createDefaultManager();
        salTableService = new SalTableServiceImpl(mockedRequestContextStack, mockedDeviceContext,
                convertorManager, MultipartWriterProviderFactory.createDefaultProvider(mockedDeviceContext));
    }

    @Test
    public void testUpdateTableFail1() throws ExecutionException, InterruptedException {
        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                final RpcResult<List<MultipartReply>> rpcResult = RpcResultBuilder.<List<MultipartReply>>failed().build();
                handleResultFuture.set(rpcResult);
                return null;
            }
        }).when(multiMessageCollector).endCollecting(Matchers.<EventIdentifier>any());

        final Future<RpcResult<UpdateTableOutput>> rpcResultFuture = salTableService.updateTable(prepareUpdateTable());
        Assert.assertNotNull(rpcResultFuture);
        verify(mockedRequestContextStack).createRequestContext();
    }

    @Test
    public void testUpdateTableFail2() throws ExecutionException, InterruptedException {
        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                final RpcResult<List<MultipartReply>> rpcResult = RpcResultBuilder.success(Collections.<MultipartReply>emptyList())
                        .build();
                handleResultFuture.set(rpcResult);
                return null;
            }
        }).when(multiMessageCollector).endCollecting(Matchers.<EventIdentifier>any());

        final Future<RpcResult<UpdateTableOutput>> rpcResultFuture = salTableService.updateTable(prepareUpdateTable());
        Assert.assertNotNull(rpcResultFuture);
        verify(mockedRequestContextStack).createRequestContext();
    }

    @Test
    public void testUpdateTableSuccess() throws ExecutionException, InterruptedException {
        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                TableFeaturesBuilder tableFeaturesBld = new TableFeaturesBuilder()
                        .setTableId((short) 0)
                        .setName("Zafod")
                        .setMaxEntries(42L)
                        .setTableFeatureProperties(Collections.<TableFeatureProperties>emptyList());
                MultipartReplyTableFeaturesBuilder mpTableFeaturesBld = new MultipartReplyTableFeaturesBuilder()
                        .setTableFeatures(Collections.singletonList(tableFeaturesBld.build()));
                MultipartReplyTableFeaturesCaseBuilder mpBodyBld = new MultipartReplyTableFeaturesCaseBuilder()
                        .setMultipartReplyTableFeatures(mpTableFeaturesBld.build());
                MultipartReplyMessageBuilder mpResultBld = new MultipartReplyMessageBuilder()
                        .setType(MultipartType.OFPMPTABLEFEATURES)
                        .setMultipartReplyBody(mpBodyBld.build())
                        .setXid(21L);
                final RpcResult<List<MultipartReply>> rpcResult = RpcResultBuilder
                        .success(Collections.singletonList((MultipartReply) mpResultBld.build()))
                        .build();
                handleResultFuture.set(rpcResult);
                return null;
            }
        }).when(multiMessageCollector).endCollecting(Matchers.<EventIdentifier>any());

        final Future<RpcResult<UpdateTableOutput>> rpcResultFuture = salTableService.updateTable(prepareUpdateTable());
        Assert.assertNotNull(rpcResultFuture);
        verify(mockedRequestContextStack).createRequestContext();
    }

    private UpdateTableInput prepareUpdateTable() {
        UpdateTableInputBuilder updateTableInputBuilder = new UpdateTableInputBuilder();
        UpdatedTableBuilder updatedTableBuilder = new UpdatedTableBuilder();
        updatedTableBuilder.setTableFeatures(Collections.<TableFeatures>emptyList());
        updateTableInputBuilder.setUpdatedTable(updatedTableBuilder.build());
        return updateTableInputBuilder.build();
    }

}
