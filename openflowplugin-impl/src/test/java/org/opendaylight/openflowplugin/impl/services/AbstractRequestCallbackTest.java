package org.opendaylight.openflowplugin.impl.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.connection.DeviceRequestFailedException;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.EventIdentifier;
import org.opendaylight.openflowplugin.impl.rpc.AbstractRequestContext;
import org.opendaylight.openflowplugin.impl.statistics.ofpspecific.MessageIntelligenceAgencyImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;

public class AbstractRequestCallbackTest {

    private static final Class<?> DUMMY_REQUEST_TYPE = String.class;
    private static final String DUMMY_DEVICE_ID = "DEVICE ID";
    private static final String DUMMY_EVENT_NAME = "EVENT NAME";
    private static final EventIdentifier DUMMY_EVENT_IDENTIFIER = new EventIdentifier(DUMMY_EVENT_NAME, DUMMY_DEVICE_ID);
    private static final String DUMMY_EXCEPTION_DESCRIPTION = "dummy exception description";
    private static final Long DUMMY_XID = 100L;
    private static final String DUMMY_MESSAGE_ILLEGAL_STATE_EXCEPTION = "dummy illegal state exception";

    AbstractRequestContext<String> dummyRequestContext;
    AbstractRequestCallback<String> abstractRequestCallback;

    @Before
    public void initialization() {
        dummyRequestContext = new AbstractRequestContext<String>(DUMMY_XID) {
            @Override
            public void close() {

            }
        };

        abstractRequestCallback = new AbstractRequestCallback<String>(dummyRequestContext,
                DUMMY_REQUEST_TYPE, new MessageIntelligenceAgencyImpl(),
                DUMMY_EVENT_IDENTIFIER) {
            @Override
            public void onSuccess(final OfHeader result) {
                // TODO Auto-generated method stub

            }
        };
    }

    @Test
    public void testOnFailureWithDeviceRequestFailedException() throws Exception {
        ErrorMessage dummyErrorMessage = new ErrorMessageBuilder().build();
        abstractRequestCallback.onFailure(new DeviceRequestFailedException(DUMMY_EXCEPTION_DESCRIPTION, dummyErrorMessage));
        final ListenableFuture<RpcResult<String>> futureResult = dummyRequestContext.getFuture();

        RpcError rpcError = provideRpcError(futureResult);
        assertEquals("Device reported error type null code null", rpcError.getMessage());
    }

    @Test
    public void testOnFailure() throws Exception {
        ErrorMessage dummyErrorMessage = new ErrorMessageBuilder().build();
        abstractRequestCallback.onFailure(new IllegalStateException(DUMMY_MESSAGE_ILLEGAL_STATE_EXCEPTION));
        final ListenableFuture<RpcResult<String>> futureResult = dummyRequestContext.getFuture();

        RpcError rpcError = provideRpcError(futureResult);
        assertEquals(DUMMY_MESSAGE_ILLEGAL_STATE_EXCEPTION, rpcError.getMessage());
    }

    private static RpcError provideRpcError(final ListenableFuture<RpcResult<String>> futureResult)
            throws InterruptedException, ExecutionException {
        final RpcResult<?> result = futureResult.get();
        final Collection<?> errors = result.getErrors();
        assertEquals(1, errors.size());
        final Object error = errors.iterator().next();
        assertTrue(error instanceof RpcError);
        return (RpcError) error;
    }
}