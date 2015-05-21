package org.opendaylight.openflowplugin.impl.device;

import static org.mockito.Matchers.any;
import com.google.common.util.concurrent.SettableFuture;
import io.netty.util.HashedWheelTimer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.opendaylight.controller.md.sal.binding.api.BindingTransactionChain;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.ReadTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.connection.OutboundQueueProvider;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageIntelligenceAgency;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartRequestFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.Error;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetAsyncOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetAsyncOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetAsyncReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.async.body.grouping.FlowRemovedMask;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.async.body.grouping.PacketInMask;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.async.body.grouping.PortStatusMask;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyDescCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyDescCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.desc._case.MultipartReplyDesc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.desc._case.MultipartReplyDescBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.class)
public class DeviceContextImplTest {
    private static final Logger LOG = LoggerFactory
            .getLogger(DeviceContextImplTest.class);
    Xid xid;
    Xid xidMulti;
    DeviceContextImpl deviceContext;
    TransactionChainManager txChainManager;
    @Mock
    RequestContext<GetAsyncReply> requestContext;
    @Mock
    RequestContext<MultipartReply> requestContextMultiReply;

    @Mock
    ConnectionContext connectionContext;
    @Mock
    DeviceState deviceState;
    @Mock
    DataBroker dataBroker;
    @Mock
    WriteTransaction wTx;
    @Mock
    ReadOnlyTransaction rTx;
    @Mock
    BindingTransactionChain txChainFactory;
    @Mock
    HashedWheelTimer timer;
    @Mock
    MessageIntelligenceAgency messageIntelligenceAgency;
    @Mock
    OutboundQueueProvider outboundQueueProvider;

    private final AtomicLong atomicLong = new AtomicLong(0);

    @Before
    public void setUp() {
        Mockito.when(dataBroker.createTransactionChain(Mockito.any(TransactionChainManager.class))).thenReturn(txChainFactory);
        txChainManager = new TransactionChainManager(dataBroker, deviceState);
        final SettableFuture<RpcResult<GetAsyncReply>> settableFuture = SettableFuture.create();
        final SettableFuture<RpcResult<MultipartReply>> settableFutureMultiReply = SettableFuture.create();
        Mockito.when(requestContext.getFuture()).thenReturn(settableFuture);
        Mockito.doAnswer(new Answer<Object>() {
            @SuppressWarnings("unchecked")
            @Override
            public Object answer(final InvocationOnMock invocation) {
                settableFuture.set((RpcResult<GetAsyncReply>) invocation.getArguments()[0]);
                return null;
            }
        }).when(requestContext).setResult(any(RpcResult.class));

        Mockito.when(requestContextMultiReply.getFuture()).thenReturn(settableFutureMultiReply);
        Mockito.doAnswer(new Answer<Object>() {
            @SuppressWarnings("unchecked")
            @Override
            public Object answer(final InvocationOnMock invocation) {
                settableFutureMultiReply.set((RpcResult<MultipartReply>) invocation.getArguments()[0]);
                return null;
            }
        }).when(requestContextMultiReply).setResult(any(RpcResult.class));
        Mockito.when(txChainFactory.newWriteOnlyTransaction()).thenReturn(wTx);
        Mockito.when(dataBroker.newReadOnlyTransaction()).thenReturn(rTx);
        Mockito.when(connectionContext.getOutboundQueueProvider()).thenReturn(outboundQueueProvider);
        deviceContext = new DeviceContextImpl(connectionContext, deviceState, dataBroker, timer, messageIntelligenceAgency);

        xid = new Xid(atomicLong.incrementAndGet());
        xidMulti = new Xid(atomicLong.incrementAndGet());
    }

    @Test(expected = NullPointerException.class)
    public void testDeviceContextImplConstructorNullConnectionContext() {
        new DeviceContextImpl(null, deviceState, dataBroker, timer, messageIntelligenceAgency).close();
    }

    @Test(expected = NullPointerException.class)
    public void testDeviceContextImplConstructorNullDataBroker() {
        new DeviceContextImpl(connectionContext, deviceState, null, timer, messageIntelligenceAgency).close();
    }

    @Test(expected = NullPointerException.class)
    public void testDeviceContextImplConstructorNullDeviceState() {
        new DeviceContextImpl(connectionContext, null, dataBroker, timer, messageIntelligenceAgency).close();
    }

    @Test(expected = NullPointerException.class)
    public void testDeviceContextImplConstructorNullTimer() {
        new DeviceContextImpl(null, deviceState, dataBroker, null, messageIntelligenceAgency).close();
    }

    @Test
    public void testGetDeviceState() {
        final DeviceState deviceSt = deviceContext.getDeviceState();
        Assert.assertNotNull(deviceSt);
        Assert.assertEquals(deviceState, deviceSt);
    }

    @Test
    public void testGetReadTransaction() {
        final ReadTransaction readTx = deviceContext.getReadTransaction();
        Assert.assertNotNull(readTx);
        Assert.assertEquals(rTx, readTx);
    }

    private static GetAsyncOutput createAsyncOutput(final Xid xid) {
        final GetAsyncOutputBuilder asyncOutputBuilder = new GetAsyncOutputBuilder();
        asyncOutputBuilder.setFlowRemovedMask(Collections.<FlowRemovedMask>emptyList());
        asyncOutputBuilder.setPacketInMask(Collections.<PacketInMask>emptyList());
        asyncOutputBuilder.setPortStatusMask(Collections.<PortStatusMask>emptyList());
        asyncOutputBuilder.setVersion(OFConstants.OFP_VERSION_1_3);
        asyncOutputBuilder.setXid(xid.getValue());
        return asyncOutputBuilder.build();
    }


    private static Error createError(final Xid xid) {
        final ErrorMessageBuilder errorMessageBuilder = new ErrorMessageBuilder();
        errorMessageBuilder.setCode(42);
        errorMessageBuilder.setCodeString("42");
        errorMessageBuilder.setXid(xid.getValue());
        return errorMessageBuilder.build();
    }

    private static List<MultipartReply> createMultipartReplyList(final Xid xid) {
        final MultipartReplyDesc descValue = new MultipartReplyDescBuilder().setHwDesc("hw-test-value").build();
        final MultipartReplyDescCase replyBody = new MultipartReplyDescCaseBuilder()
                .setMultipartReplyDesc(descValue).build();
        final List<MultipartReply> multipartReplies = new ArrayList<>();
        multipartReplies.add(new MultipartReplyMessageBuilder()
                .setMultipartReplyBody(replyBody)
                .setXid(xid.getValue())
                .setFlags(new MultipartRequestFlags(false))
                .build());
        multipartReplies.add(new MultipartReplyMessageBuilder()
                .setMultipartReplyBody(replyBody)
                .setXid(xid.getValue())
                .setFlags(new MultipartRequestFlags(true))
                .build());
        return multipartReplies;
    }


}
