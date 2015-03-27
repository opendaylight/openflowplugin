package org.opendaylight.openflowplugin.impl.device;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Collections;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetAsyncOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetAsyncOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetAsyncReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.async.body.grouping.FlowRemovedMask;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.async.body.grouping.PacketInMask;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.async.body.grouping.PortStatusMask;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.SettableFuture;

@RunWith(MockitoJUnitRunner.class)
public class DeviceContextImplTest {
    private static final Logger LOG = LoggerFactory
            .getLogger(DeviceContextImplTest.class);
    Xid xid;
    DeviceContextImpl deviceContext;
    @Mock
    RequestContext<GetAsyncReply> requestContext;

    @Before
    public void setUp() {
        deviceContext = new DeviceContextImpl();
        xid = deviceContext.getNextXid();
        SettableFuture<RpcResult<GetAsyncReply>> settableFuture = SettableFuture.create();
        Mockito.when(requestContext.getFuture()).thenReturn(settableFuture);
        deviceContext.hookRequestCtx(deviceContext.getNextXid(), requestContext);
    }

    private GetAsyncOutput createAsyncOutput() {
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
        GetAsyncOutput asyncOutput = createAsyncOutput();
        LOG.info("Hooking RequestContext");
        deviceContext.hookRequestCtx(xid, requestContext);
        Assert.assertEquals(requestContext, deviceContext.getRequests().get(xid));

        Assert.assertFalse(requestContext.getFuture().isDone());
        LOG.info("Sending reply from device");
        deviceContext.processReply(xid, asyncOutput);
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

}
