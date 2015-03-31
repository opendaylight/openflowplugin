package org.opendaylight.openflowplugin.impl.device;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import com.google.common.util.concurrent.SettableFuture;
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
import org.opendaylight.openflowplugin.api.openflow.device.XidGenerator;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetAsyncOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetAsyncOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetAsyncReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.async.body.grouping.FlowRemovedMask;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.async.body.grouping.PacketInMask;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.async.body.grouping.PortStatusMask;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.class)
public class DeviceContextImplTest {
    private static final Logger LOG = LoggerFactory
            .getLogger(DeviceContextImplTest.class);
    Xid xid;
    XidGenerator xidGen;
    DeviceContextImpl deviceContext;
    @Mock
    RequestContext<GetAsyncReply> requestContext;
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

    @Before
    public void setUp() {
        xidGen = new XidGenerator();
        Mockito.when(deviceState.getNextXid()).thenReturn(xidGen.generate());
        Mockito.when(txChainFactory.newWriteOnlyTransaction()).thenReturn(wTx);
        Mockito.when(dataBroker.createTransactionChain(Mockito.any(DeviceContextImpl.class))).thenReturn(txChainFactory);
        Mockito.when(dataBroker.newReadOnlyTransaction()).thenReturn(rTx);
        deviceContext = new DeviceContextImpl(connectionContext, deviceState, dataBroker);
        final SettableFuture<RpcResult<GetAsyncReply>> settableFuture = SettableFuture.create();
        Mockito.when(requestContext.getFuture()).thenReturn(settableFuture);
        deviceContext.hookRequestCtx(deviceContext.getNextXid(), requestContext);
    }

    @Test(expected=NullPointerException.class)
    public void testDeviceContextImplConstructorNullConnectionContext() {
        new DeviceContextImpl(null, deviceState, dataBroker);
    }

    @Test(expected=NullPointerException.class)
    public void testDeviceContextImplConstructorNullDataBroker() {
        new DeviceContextImpl(connectionContext, deviceState, null);
    }

    @Test(expected=NullPointerException.class)
    public void testDeviceContextImplConstructorNullDeviceState() {
        new DeviceContextImpl(connectionContext, null, dataBroker);
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

    @Test
    public void testGetWriteTransaction() {
        final WriteTransaction writeTx = deviceContext.getWriteTransaction();
        Assert.assertNotNull(writeTx);
        Assert.assertEquals(wTx, writeTx);
    }

    @Test
    public void testProcessReply() {
        final GetAsyncOutput asyncOutput = createAsyncOutput();
        LOG.info("Hooking RequestContext");
        deviceContext.hookRequestCtx(xid, requestContext);
        Assert.assertEquals(requestContext, deviceContext.getRequests().get(xid));

        Assert.assertFalse(requestContext.getFuture().isDone());
        LOG.info("Sending reply from device");
        deviceContext.processReply(xid, asyncOutput);
        Assert.assertTrue(requestContext.getFuture().isDone());

        LOG.info("Checking RequestContext.future");
        try {
            final Object object = requestContext.getFuture().get(1L, TimeUnit.SECONDS);
            final GetAsyncOutput getAsyncOutput = (GetAsyncOutput) object;
            assertEquals(asyncOutput.getVersion(), getAsyncOutput.getVersion());
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            LOG.error("Test failed when checking RequestContext.future", e);
            fail("fail");
        }
    }

    private GetAsyncOutput createAsyncOutput() {
        xid = xidGen.generate();
        final GetAsyncOutputBuilder asyncOutputBuilder = new GetAsyncOutputBuilder();
        asyncOutputBuilder.setFlowRemovedMask(Collections.<FlowRemovedMask> emptyList());
        asyncOutputBuilder.setPacketInMask(Collections.<PacketInMask> emptyList());
        asyncOutputBuilder.setPortStatusMask(Collections.<PortStatusMask> emptyList());
        asyncOutputBuilder.setVersion(OFConstants.OFP_VERSION_1_3);
        asyncOutputBuilder.setXid(xid.getValue());
        return asyncOutputBuilder.build();
    }

}
