package org.opendaylight.openflowplugin.impl.statistics.services.direct;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.impl.services.AbstractMultipartService;
import org.opendaylight.openflowplugin.impl.services.RequestInputUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.StoreStatsGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.MultipartRequestBody;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.Future;

/**
 *
 * @param <I> RPC input
 * @param <O> RPC output
 */
public abstract class AbstractDirectStatisticsService<I extends StoreStatsGrouping, O> extends AbstractMultipartService<I> {

    private final Function<RpcResult<List<MultipartReply>>, RpcResult<O>> resultTransformFunction =
            new Function<RpcResult<List<MultipartReply>>, RpcResult<O>>() {
                @Nullable
                @Override
                public RpcResult<O> apply(@Nullable RpcResult<List<MultipartReply>> input) {
                    Preconditions.checkNotNull(input);
                    Preconditions.checkArgument(input.isSuccessful());
                    final O reply = buildReply(input.getResult());
                    return RpcResultBuilder.success(reply).build();
                }
            };

    private final AsyncFunction<RpcResult<O>, RpcResult<O>> resultStoreFunction =
            new AsyncFunction<RpcResult<O>, RpcResult<O>>() {
                @Nullable
                @Override
                public ListenableFuture<RpcResult<O>> apply(@Nullable RpcResult<O> input) throws Exception {
                    Preconditions.checkNotNull(input);
                    Preconditions.checkArgument(input.isSuccessful());
                    storeStatistics(input.getResult());
                    getDeviceContext().submitTransaction();
                    //TODO: Chain future from transaction submit
                    return Futures.immediateFuture(input);
                }
            };

    private final MultipartType multipartType;
    private final OpenflowVersion ofVersion = OpenflowVersion.get(getVersion());

    /**
     *
     * @param multipartType
     * @param requestContextStack
     * @param deviceContext
     */
    protected AbstractDirectStatisticsService(MultipartType multipartType, RequestContextStack requestContextStack, DeviceContext deviceContext) {
        super(requestContextStack, deviceContext);
        this.multipartType = multipartType;
    }

    /**
     *
     * @param input
     * @return
     */
    public Future<RpcResult<O>> handleAndReply(final I input) {
        final ListenableFuture<RpcResult<List<MultipartReply>>> rpcReply = handleServiceCall(input);
        ListenableFuture<RpcResult<O>> rpcResult = Futures.transform(rpcReply, resultTransformFunction);

        if (input.isStoreStats()) {
            rpcResult = Futures.transform(rpcResult, resultStoreFunction);
        }

        return rpcResult;
    }

    /**
     *
     * @param xid
     * @param input
     * @return
     * @throws Exception
     */
    @Override
    protected OfHeader buildRequest(Xid xid, I input) throws Exception {
        return RequestInputUtils.createMultipartHeader(multipartType, xid.getValue(), getVersion())
                .setMultipartRequestBody(buildRequestBody(input))
                .build();
    }

    /**
     *
     * @return version converted to OpenflowVersion
     */
    protected OpenflowVersion getOfVersion() {
        return ofVersion;
    }

    /**
     *
     * @param input
     * @return multipart request body
     */
    protected abstract MultipartRequestBody buildRequestBody(I input);

    /**
     *
     * @param input
     * @return multipart reply input converted to RPC output
     */
    protected abstract O buildReply(List<MultipartReply> input);

    /**
     *
     * @param output
     * @throws Exception
     */
    protected abstract void storeStatistics(O output) throws Exception;
}
