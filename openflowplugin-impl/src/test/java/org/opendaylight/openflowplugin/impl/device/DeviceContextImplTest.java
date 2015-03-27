package org.opendaylight.openflowplugin.impl.device;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

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
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartRequestFlags;
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
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.SettableFuture;

@RunWith(MockitoJUnitRunner.class)
public class DeviceContextImplTest {
    private static final Logger LOG = LoggerFactory
            .getLogger(DeviceContextImplTest.class);
    DeviceContextImpl deviceContext; // = new DeviceContextImpl();
    Xid xid; //= deviceContext.getNextXid();
    Xid xidMulti;
    @Mock
    RequestContext<GetAsyncReply> requestContext;
    @Mock
    RequestContext<MultipartReply> requestContextMultiReply;

    @Before
    public void setUp() {
        deviceContext = new DeviceContextImpl();
        xid = deviceContext.getNextXid();
        xidMulti = deviceContext.getNextXid();
        SettableFuture<RpcResult<GetAsyncReply>> settableFuture = SettableFuture.create();
        SettableFuture<RpcResult<MultipartReply>> settableFutureMultiReply = SettableFuture.create();
        Mockito.when(requestContext.getFuture()).thenReturn(settableFuture);
        Mockito.when(requestContextMultiReply.getFuture()).thenReturn(settableFutureMultiReply);
        deviceContext.hookRequestCtx(deviceContext.getNextXid(), requestContext);
    }

    private static GetAsyncOutput createAsyncOutput(Xid xid) {
        GetAsyncOutputBuilder asyncOutputBuilder = new GetAsyncOutputBuilder();
        asyncOutputBuilder.setFlowRemovedMask(Collections.<FlowRemovedMask> emptyList());
        asyncOutputBuilder.setPacketInMask(Collections.<PacketInMask> emptyList());
        asyncOutputBuilder.setPortStatusMask(Collections.<PortStatusMask> emptyList());
        asyncOutputBuilder.setVersion(OFConstants.OFP_VERSION_1_3);
        asyncOutputBuilder.setXid(xid.getValue());
        return asyncOutputBuilder.build();
    }

    @Test
    public void testProcessReply() {
        GetAsyncOutput asyncOutput = createAsyncOutput(xid);
        LOG.info("Hooking RequestContext");
        deviceContext.hookRequestCtx(xid, requestContext);
        Assert.assertEquals(requestContext, deviceContext.getRequests().get(xid.getValue()));

        Assert.assertFalse(requestContext.getFuture().isDone());
        LOG.info("Sending reply from device");
        deviceContext.processReply(asyncOutput);
        Assert.assertTrue(requestContext.getFuture().isDone());

        LOG.info("Checking RequestContext.future");
        try {
            Object object = requestContext.getFuture().get(1L, TimeUnit.SECONDS);
            GetAsyncOutput getAsyncOutput = (GetAsyncOutput) object;
            assertEquals(asyncOutput.getVersion(), getAsyncOutput.getVersion());
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            LOG.error("Test failed when checking RequestContext.future", e);
            fail("fail");
        }
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
            Object object = requestContextMultiReply.getFuture().get(1L, TimeUnit.SECONDS);
            List<OfHeader> multipartReplies = (List<OfHeader>) object;
            List<OfHeader> expectedMpReplies = createMultipartReplyList(xidMulti);
            assertEquals(expectedMpReplies, multipartReplies);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            LOG.error("Test failed when checking RequestContext.future", e);
            fail("fail");
        }
    }

    private static List<OfHeader> createMultipartReplyList(Xid xid) {
        final MultipartReplyDesc descValue = new MultipartReplyDescBuilder().setHwDesc("hw-test-value").build();
        final MultipartReplyDescCase replyBody = new MultipartReplyDescCaseBuilder()
                                                        .setMultipartReplyDesc(descValue).build();
        List<OfHeader> multipartReplies = new ArrayList<OfHeader>();
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
