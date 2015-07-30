package org.opendaylight.openflowplugin.impl.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.EventIdentifier;
import org.opendaylight.openflowplugin.impl.rpc.AbstractRequestContext;
import org.opendaylight.openflowplugin.impl.statistics.ofpspecific.MessageIntelligenceAgencyImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.hello.Elements;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MultipartRequestOnTheFlyCallbackTest {


    private static final String DUMMY_NODE_ID = "dummyNodeId";
    private static final String DUMMY_EVENT_NAME = "dummy event name 1";
    private static final String DUMMY_DEVICE_ID = "dummy device id 1";
    private static final Long DUMMY_XID = 55L;
    @Mock
    private DeviceContext mockedDeviceContext;

    @Mock
    RequestContext<List<MultipartReply>> mockedRequestContext;
    @Mock
    ConnectionContext mockedPrimaryConnection;
    @Mock
    NodeId mockedNodeId;

    private AbstractRequestContext<List<MultipartReply>> dummyRequestContext = new AbstractRequestContext<List<MultipartReply>>(DUMMY_XID) {

        @Override
        public void close() {

        }
    };

    private EventIdentifier dummyEventIdentifier = new EventIdentifier(DUMMY_EVENT_NAME, DUMMY_DEVICE_ID);

    @Before
    public void initialization() {
        when(mockedDeviceContext.getMessageSpy()).thenReturn(new MessageIntelligenceAgencyImpl());
        when(mockedNodeId.toString()).thenReturn(DUMMY_NODE_ID);
        when(mockedPrimaryConnection.getNodeId()).thenReturn(mockedNodeId);
        when(mockedDeviceContext.getPrimaryConnectionContext()).thenReturn(mockedPrimaryConnection);
    }


    @Test
    public void testOnSuccessWithNull() throws Exception {
        final MultipartRequestOnTheFlyCallback multipartRequestOnTheFlyCallback = new MultipartRequestOnTheFlyCallback(dummyRequestContext, String.class, mockedDeviceContext, dummyEventIdentifier);
        multipartRequestOnTheFlyCallback.onSuccess(null);
        final RpcResult<List<MultipartReply>> expectedRpcResult = RpcResultBuilder.success(Collections.<MultipartReply>emptyList()).build();
        final RpcResult<List<MultipartReply>> actualResult = dummyRequestContext.getFuture().get();
        assertEquals(expectedRpcResult.getErrors(), actualResult.getErrors());
        assertEquals(expectedRpcResult.getResult(), actualResult.getResult());
        assertEquals(expectedRpcResult.isSuccessful(), actualResult.isSuccessful());
    }

    @Test
    public void testOnSuccessWithNotMultiNoMultipart() throws ExecutionException, InterruptedException {
        final MultipartRequestOnTheFlyCallback multipartRequestOnTheFlyCallback = new MultipartRequestOnTheFlyCallback(dummyRequestContext, String.class, mockedDeviceContext, dummyEventIdentifier);
        HelloMessage mockedHelloMessage = mock(HelloMessage.class);
        multipartRequestOnTheFlyCallback.onSuccess(mockedHelloMessage);

        final RpcResult<List<MultipartReply>> expectedRpcResult =
                RpcResultBuilder.<List<MultipartReply>>failed().withError(RpcError.ErrorType.APPLICATION,
                        String.format("Unexpected response type received %s.", mockedHelloMessage.getClass())).build();
        final RpcResult<List<MultipartReply>> actualResult = dummyRequestContext.getFuture().get();
        assertNotNull(actualResult.getErrors());
        assertEquals(1, actualResult.getErrors().size());

        final RpcError actualError = actualResult.getErrors().iterator().next();
        assertEquals(actualError.getMessage(), String.format("Unexpected response type received %s.", mockedHelloMessage.getClass()));
        assertEquals(actualError.getErrorType(),RpcError.ErrorType.APPLICATION);
        assertEquals(expectedRpcResult.getResult(), actualResult.getResult());
        assertEquals(expectedRpcResult.isSuccessful(), actualResult.isSuccessful());

    }
}