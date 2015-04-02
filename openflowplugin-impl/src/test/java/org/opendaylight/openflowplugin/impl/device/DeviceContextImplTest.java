package org.opendaylight.openflowplugin.impl.device;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import com.google.common.util.concurrent.SettableFuture;
import io.netty.util.HashedWheelTimer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.binding.api.BindingTransactionChain;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.ReadTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.api.openflow.device.exception.DeviceDataException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartRequestFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.Error;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetAsyncOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetAsyncOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetAsyncReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.async.body.grouping.FlowRemovedMask;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.async.body.grouping.PacketInMask;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.async.body.grouping.PortStatusMask;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyDescCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyDescCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.desc._case.MultipartReplyDesc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.desc._case.MultipartReplyDescBuilder;
import org.opendaylight.yangtools.yang.common.RpcError;
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

    @Before
    public void setUp() {
        Mockito.when(dataBroker.createTransactionChain(Mockito.any(TransactionChainManager.class))).thenReturn(txChainFactory);
        txChainManager = new TransactionChainManager(dataBroker, 5L);
        final SettableFuture<RpcResult<GetAsyncReply>> settableFuture = SettableFuture.create();
        final SettableFuture<RpcResult<MultipartReply>> settableFutureMultiReply = SettableFuture.create();
        Mockito.when(requestContext.getFuture()).thenReturn(settableFuture);
        Mockito.when(requestContextMultiReply.getFuture()).thenReturn(settableFutureMultiReply);
        Mockito.when(txChainFactory.newWriteOnlyTransaction()).thenReturn(wTx);
        Mockito.when(dataBroker.newReadOnlyTransaction()).thenReturn(rTx);

        deviceContext = new DeviceContextImpl(connectionContext, deviceState, dataBroker, timer);
        xid = deviceContext.getNextXid();
        xidMulti = deviceContext.getNextXid();
    }

    @Test(expected = NullPointerException.class)
    public void testDeviceContextImplConstructorNullConnectionContext() {
        new DeviceContextImpl(null, deviceState, dataBroker, timer);
    }

    @Test(expected = NullPointerException.class)
    public void testDeviceContextImplConstructorNullDataBroker() {
        new DeviceContextImpl(connectionContext, deviceState, null, timer);
    }

    @Test(expected = NullPointerException.class)
    public void testDeviceContextImplConstructorNullDeviceState() {
        new DeviceContextImpl(connectionContext, null, dataBroker, timer);
    }

    @Test(expected = NullPointerException.class)
    public void testDeviceContextImplConstructorNullTimer() {
        new DeviceContextImpl(null, deviceState, dataBroker, null);
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
        asyncOutputBuilder.setFlowRemovedMask(Collections.<FlowRemovedMask> emptyList());
        asyncOutputBuilder.setPacketInMask(Collections.<PacketInMask> emptyList());
        asyncOutputBuilder.setPortStatusMask(Collections.<PortStatusMask> emptyList());
        asyncOutputBuilder.setVersion(OFConstants.OFP_VERSION_1_3);
        asyncOutputBuilder.setXid(xid.getValue());
        return asyncOutputBuilder.build();
    }

    @Test
    public void testProcessReply() {
        final GetAsyncOutput asyncOutput = createAsyncOutput(xid);
        LOG.info("Hooking RequestContext");
        deviceContext.hookRequestCtx(xid, requestContext);
        Assert.assertEquals(requestContext, deviceContext.getRequests().get(xid.getValue()));

        Assert.assertFalse(requestContext.getFuture().isDone());
        LOG.info("Sending reply from device");
        deviceContext.processReply(asyncOutput);
        Assert.assertTrue(requestContext.getFuture().isDone());

        LOG.info("Checking RequestContext.future");
        try {
            final Object object = requestContext.getFuture().get(1L, TimeUnit.SECONDS);
            final RpcResult<OfHeader> rpcResult = (RpcResult<OfHeader>) object;
            final GetAsyncOutput getAsyncOutput = (GetAsyncOutput) rpcResult.getResult();
            assertEquals(asyncOutput.getVersion(), getAsyncOutput.getVersion());
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            LOG.error("Test failed when checking RequestContext.future", e);
            fail("fail");
        }
        Assert.assertTrue(deviceContext.getRequests().isEmpty());
    }

    private static Error createError(final Xid xid) {
        final ErrorMessageBuilder errorMessageBuilder = new ErrorMessageBuilder();
        errorMessageBuilder.setCode(42);
        errorMessageBuilder.setCodeString("42");
        errorMessageBuilder.setXid(xid.getValue());
        return errorMessageBuilder.build();
    }

    @Test
    public void testProcessReplyError() {
        LOG.info("Hooking RequestContext");
        deviceContext.hookRequestCtx(xid, requestContext);
        Assert.assertEquals(requestContext, deviceContext.getRequests().get(xid.getValue()));

        Assert.assertFalse(requestContext.getFuture().isDone());
        LOG.info("Sending error reply from device");
        final Error error = createError(xid);
        deviceContext.processReply(error);
        Assert.assertTrue(requestContext.getFuture().isDone());

        LOG.info("Checking RequestContext.future");
        try {
            final Object object = requestContext.getFuture().get(1L, TimeUnit.SECONDS);
            final RpcResult<OfHeader> rpcResult = (RpcResult<OfHeader>) object;
            Assert.assertFalse(rpcResult.isSuccessful());
            final List<RpcError> errors = (List<RpcError>) rpcResult.getErrors();
            Assert.assertTrue(errors.get(0).getCause() instanceof DeviceDataException);
            final DeviceDataException cause = (DeviceDataException) errors.get(0).getCause();
            Assert.assertEquals(error, cause.getError());
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            LOG.error("Test failed when checking RequestContext.future", e);
            fail("fail");
        }
        Assert.assertTrue(deviceContext.getRequests().isEmpty());
    }

    @Test
    public void testProcessReplyList() {
        LOG.info("Hooking RequestContext");
        deviceContext.hookRequestCtx(xidMulti, requestContextMultiReply);
        Assert.assertEquals(requestContextMultiReply, deviceContext.getRequests().get(xidMulti.getValue()));

        Assert.assertFalse(requestContextMultiReply.getFuture().isDone());
        LOG.info("Sending reply from device");
        deviceContext.processReply(xidMulti, createMultipartReplyList(xidMulti));
        Assert.assertTrue(requestContextMultiReply.getFuture().isDone());

        LOG.info("Checking RequestContext.future");
        try {
            final Object object = requestContextMultiReply.getFuture().get(1L, TimeUnit.SECONDS);
            final RpcResult<List<OfHeader>> rpcResult = (RpcResult<List<OfHeader>>) object;
            final List<OfHeader> multipartReplies = rpcResult.getResult();
            final List<OfHeader> expectedMpReplies = createMultipartReplyList(xidMulti);
            assertEquals(expectedMpReplies, multipartReplies);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            LOG.error("Test failed when checking RequestContext.future", e);
            fail("fail");
        }
        Assert.assertTrue(deviceContext.getRequests().isEmpty());
    }

    private static List<OfHeader> createMultipartReplyList(final Xid xid) {
        final MultipartReplyDesc descValue = new MultipartReplyDescBuilder().setHwDesc("hw-test-value").build();
        final MultipartReplyDescCase replyBody = new MultipartReplyDescCaseBuilder()
                                                        .setMultipartReplyDesc(descValue).build();
        final List<OfHeader> multipartReplies = new ArrayList<OfHeader>();
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

    @Test
    public void testProcessException() {
        LOG.info("Hooking RequestContext");
        deviceContext.hookRequestCtx(xid, requestContext);
        Assert.assertEquals(requestContext, deviceContext.getRequests().get(xid.getValue()));

        Assert.assertFalse(requestContext.getFuture().isDone());

        LOG.info("Sending reply from device");
        deviceContext.processException(xid, new DeviceDataException("Some freakin' error", new NullPointerException()));
        Assert.assertTrue(requestContext.getFuture().isDone());

        LOG.info("Checking RequestContext.future");
        try {
                final Object object = requestContext.getFuture().get(1L, TimeUnit.SECONDS);
                final RpcResult<OfHeader> rpcResult = (RpcResult<OfHeader>) object;
                Assert.assertFalse(rpcResult.isSuccessful());
                final List<RpcError> errors = (List<RpcError>) rpcResult.getErrors();
                Assert.assertTrue(errors.get(0).getCause() instanceof DeviceDataException);
                final DeviceDataException cause = (DeviceDataException) errors.get(0).getCause();
                Assert.assertTrue(cause.getCause() instanceof NullPointerException);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            LOG.error("Test failed when checking RequestContext.future", e);
            fail("fail");
        }
        Assert.assertTrue(deviceContext.getRequests().isEmpty());
    }

}
