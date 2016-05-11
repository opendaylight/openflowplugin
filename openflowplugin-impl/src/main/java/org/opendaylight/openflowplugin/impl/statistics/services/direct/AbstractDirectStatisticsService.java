package org.opendaylight.openflowplugin.impl.statistics.services.direct;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.impl.services.AbstractMultipartService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.StoreStatsGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.Future;

public abstract class AbstractDirectStatisticsService<I extends StoreStatsGrouping, O> extends AbstractMultipartService<I> {

    private final Function<RpcResult<List<MultipartReply>>, RpcResult<O>> resultTransformFunction =
            new Function<RpcResult<List<MultipartReply>>, RpcResult<O>>() {
                @Nullable
                @Override
                public RpcResult<O> apply(@Nullable RpcResult<List<MultipartReply>> input) {
                    Preconditions.checkNotNull(input);
                    Preconditions.checkArgument(input.isSuccessful());
                    Builder<O> result = buildReply(input.getResult());
                    return RpcResultBuilder.success(result).build();
                }
            };

    private final AsyncFunction<RpcResult<O>, RpcResult<O>> resultStoreFunction = new AsyncFunction<RpcResult<O>, RpcResult<O>>() {
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

    protected AbstractDirectStatisticsService(RequestContextStack requestContextStack, DeviceContext deviceContext) {
        super(requestContextStack, deviceContext);
    }

    public Future<RpcResult<O>> handleAndReply(final I input) {
        final ListenableFuture<RpcResult<List<MultipartReply>>> rpcReply = handleServiceCall(input);
        ListenableFuture<RpcResult<O>> rpcResult = Futures.transform(rpcReply, resultTransformFunction);

        if (input.isStoreStats()) {
            rpcResult = Futures.transform(rpcResult, resultStoreFunction);
        }

        return rpcResult;
    }

    protected abstract Builder<O> buildReply(@Nullable List<MultipartReply> input);

    protected abstract void storeStatistics(@Nullable O input) throws Exception;
}
