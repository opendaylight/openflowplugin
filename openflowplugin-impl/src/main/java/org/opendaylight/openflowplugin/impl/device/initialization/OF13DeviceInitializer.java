/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.device.initialization;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.impl.common.MultipartReplyTranslatorUtil;
import org.opendaylight.openflowplugin.impl.datastore.MultipartWriterProvider;
import org.opendaylight.openflowplugin.impl.services.multilayer.MultiLayerMultipartCollectorService;
import org.opendaylight.openflowplugin.impl.services.singlelayer.SingleLayerMultipartCollectorService;
import org.opendaylight.openflowplugin.impl.util.DeviceInitializationUtil;
import org.opendaylight.openflowplugin.impl.util.DeviceStateUtil;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.Capabilities;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OF13DeviceInitializer extends AbstractDeviceInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(OF13DeviceInitializer.class);

    @Override
    protected Future<Void> initializeNodeInformation(@Nonnull final DeviceContext deviceContext,
                                                     final boolean switchFeaturesMandatory,
                                                     final boolean skipTableFeatures,
                                                     @Nullable final MultipartWriterProvider multipartWriterProvider,
                                                     @Nullable final ConvertorExecutor convertorExecutor) {
        final ConnectionContext connectionContext = Preconditions.checkNotNull(deviceContext.getPrimaryConnectionContext());
        final DeviceState deviceState = Preconditions.checkNotNull(deviceContext.getDeviceState());
        final DeviceInfo deviceInfo = Preconditions.checkNotNull(deviceContext.getDeviceInfo());
        final Capabilities capabilities = connectionContext.getFeatures().getCapabilities();
        LOG.debug("Setting capabilities for device {}", deviceInfo.getLOGValue());
        DeviceStateUtil.setDeviceStateBasedOnV13Capabilities(deviceState, capabilities);

        // First process description reply, write data to DS and write consequent data if successful
        return  Futures.transform(
            requestMultipart(MultipartType.OFPMPDESC, deviceContext),
            (AsyncFunction<RpcResult<List<OfHeader>>, Void>) input -> {
                translateAndWriteResult(
                    MultipartType.OFPMPDESC,
                    input.getResult(),
                    deviceContext,
                    multipartWriterProvider,
                    convertorExecutor);

                final List<ListenableFuture<RpcResult<List<OfHeader>>>> futures = new ArrayList<>();
                futures.add(requestAndProcessMultipart(MultipartType.OFPMPMETERFEATURES, deviceContext, skipTableFeatures, multipartWriterProvider, convertorExecutor));
                futures.add(requestAndProcessMultipart(MultipartType.OFPMPGROUPFEATURES, deviceContext, skipTableFeatures, multipartWriterProvider, convertorExecutor));
                futures.add(requestAndProcessMultipart(MultipartType.OFPMPTABLEFEATURES, deviceContext, skipTableFeatures, multipartWriterProvider, convertorExecutor));
                futures.add(requestAndProcessMultipart(MultipartType.OFPMPPORTDESC, deviceContext, skipTableFeatures, multipartWriterProvider, convertorExecutor));

                return Futures.transform(
                    (switchFeaturesMandatory ? Futures.allAsList(futures) : Futures.successfulAsList(futures)),
                    new Function<List<RpcResult<List<OfHeader>>>, Void>() {
                        @Nullable
                        @Override
                        public Void apply(@Nullable final List<RpcResult<List<OfHeader>>> input) {
                            LOG.info("Static node {} successfully finished collecting", deviceContext.getDeviceInfo().getLOGValue());
                            return null;
                        }
                    });
            });

    }

    /**
     * Request multipart of specified type and then run some processing on it
     * @param type multipart type
     * @param deviceContext device context
     * @param skipTableFeatures skip collecting of table features
     * @param multipartWriterProvider multipart writer provider
     * @param convertorExecutor convertor executor
     * @return list of multipart messages unified to parent interface
     */
    private static ListenableFuture<RpcResult<List<OfHeader>>> requestAndProcessMultipart(final MultipartType type,
                                                                                          final DeviceContext deviceContext,
                                                                                          final boolean skipTableFeatures,
                                                                                          final MultipartWriterProvider multipartWriterProvider,
                                                                                          @Nullable final ConvertorExecutor convertorExecutor) {
        final ListenableFuture<RpcResult<List<OfHeader>>> rpcResultListenableFuture =
            MultipartType.OFPMPTABLEFEATURES.equals(type) && skipTableFeatures
                ? RpcResultBuilder.<List<OfHeader>>success().buildFuture()
                : requestMultipart(type, deviceContext);

        createCallback(type, rpcResultListenableFuture, deviceContext, multipartWriterProvider, convertorExecutor);
        return rpcResultListenableFuture;
    }

    /**
     * Inject callback ti future for specified multipart type. This callback will translate and write
     * result of multipart messages
     * @param type multipart type
     * @param future multipart collection future
     * @param deviceContext device context
     * @param multipartWriterProvider multipart writer provider
     * @param convertorExecutor convertor executor
     */
    private static void createCallback(final MultipartType type,
                                       final ListenableFuture<RpcResult<List<OfHeader>>> future,
                                       final DeviceContext deviceContext,
                                       @Nullable final MultipartWriterProvider multipartWriterProvider,
                                       @Nullable final ConvertorExecutor convertorExecutor) {
        Futures.addCallback(future, new FutureCallback<RpcResult<List<OfHeader>>>() {
            @Override
            public void onSuccess(final RpcResult<List<OfHeader>> result) {
                if (Objects.nonNull(result.getResult())) {
                    LOG.info("Static node {} info: {} collected", deviceContext.getDeviceInfo().getLOGValue(), type);
                    translateAndWriteResult(
                        type,
                        result.getResult(),
                        deviceContext,
                        multipartWriterProvider,
                        convertorExecutor);
                } else {
                    result.getErrors().forEach(rpcError -> {
                        LOG.warn("Failed to retrieve static node {} info: {}", type, rpcError.getMessage());

                        if (LOG.isTraceEnabled() && Objects.nonNull(rpcError.getCause())) {
                            LOG.trace("Detailed error:", rpcError.getCause());
                        }
                    });

                    // If table features are disabled or returned nothing, at least make empty tables
                    if (MultipartType.OFPMPTABLEFEATURES.equals(type)) {
                        DeviceInitializationUtil.makeEmptyTables(
                            deviceContext,
                            deviceContext.getDeviceInfo(),
                            deviceContext.getPrimaryConnectionContext().getFeatures().getTables());
                    }
                }
            }

            @Override
            public void onFailure(@Nonnull final Throwable t) {
                LOG.warn("Request of type {} for static info of node {} failed.", type, deviceContext.getDeviceInfo().getLOGValue());
            }
        });
    }

    /**
     * Translate and write multipart messages from OpenflowJava
     * @param type multipart type
     * @param result multipart messages
     * @param deviceContext device context
     * @param multipartWriterProvider multipart writer provider
     * @param convertorExecutor convertor executor
     */
    private static void translateAndWriteResult(final MultipartType type,
                                                final List<OfHeader> result,
                                                final DeviceContext deviceContext,
                                                @Nullable final MultipartWriterProvider multipartWriterProvider,
                                                @Nullable final ConvertorExecutor convertorExecutor) {
        if (Objects.nonNull(result)) {
            try {
                result.forEach(reply -> {
                    // First, translate collected data to proper openflowplugin representation
                    MultipartReplyTranslatorUtil
                        .translate(
                            reply,
                            deviceContext.getDeviceInfo(),
                            convertorExecutor,
                            deviceContext.oook())
                        .ifPresent(translatedReply -> {
                            // If we collected meter features, check if we have support for meters
                            // and pass this information to device context
                            if (MultipartType.OFPMPMETERFEATURES.equals(type) &&
                                translatedReply instanceof MeterFeatures) {
                                final MeterFeatures meterFeatures = (MeterFeatures) translatedReply;

                                if (meterFeatures.getMaxMeter().getValue() > 0) {
                                    deviceContext.getDeviceState().setMeterAvailable(true);
                                }
                            }

                            // Now. try to write translated collected features
                            Optional.ofNullable(multipartWriterProvider)
                                .flatMap(provider -> provider.lookup(type))
                                .ifPresent(writer -> writer.write(translatedReply, false));
                        });
                });
            } catch (final Exception e) {
                LOG.warn("Failed to write node {} to DS ", deviceContext.getDeviceInfo().getLOGValue(), e);
            }
        } else {
            LOG.warn("Failed to write node {} to DS because we failed to gather device info.",
                deviceContext.getDeviceInfo().getLOGValue());
        }
    }

    /**
     * Send request to device and unify different possible reply types from OpenflowJava to common parent interface
     * @param multipartType multipart type
     * @param deviceContext device context
     * @return unified replies
     */
    private static ListenableFuture<RpcResult<List<OfHeader>>> requestMultipart(final MultipartType multipartType,
                                                                                final DeviceContext deviceContext) {
        if (deviceContext.canUseSingleLayerSerialization()) {
            final SingleLayerMultipartCollectorService service =
                new SingleLayerMultipartCollectorService(deviceContext, deviceContext);

            return Futures.transform(service.handleServiceCall(multipartType), new Function<RpcResult<List<MultipartReply>>, RpcResult<List<OfHeader>>>() {
                @Nonnull
                @Override
                public RpcResult<List<OfHeader>> apply(final RpcResult<List<MultipartReply>> input) {
                    if (Objects.isNull(input.getResult()) && input.isSuccessful()) {
                        final List<OfHeader> temp = null;
                        return RpcResultBuilder.success(temp).build();
                    }

                    return input.isSuccessful()
                        ? RpcResultBuilder.success(input
                            .getResult()
                        .stream()
                        .map(OfHeader.class::cast)
                        .collect(Collectors.toList()))
                        .build()
                        : RpcResultBuilder.<List<OfHeader>>failed()
                        .withRpcErrors(input.getErrors())
                        .build();
                }
            });
        }

        final MultiLayerMultipartCollectorService service =
            new MultiLayerMultipartCollectorService(deviceContext, deviceContext);

        return Futures.transform(service.handleServiceCall(multipartType), new Function<RpcResult<List<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply>>, RpcResult<List<OfHeader>>>() {
            @Nonnull
            @Override
            public RpcResult<List<OfHeader>> apply(final RpcResult<List<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply>> input) {
                if (Objects.isNull(input.getResult()) && input.isSuccessful()) {
                    final List<OfHeader> temp = null;
                    return RpcResultBuilder.success(temp).build();
                }

                return input.isSuccessful()
                    ? RpcResultBuilder.success(input
                    .getResult()
                    .stream()
                    .map(OfHeader.class::cast)
                    .collect(Collectors.toList()))
                    .build()
                    : RpcResultBuilder.<List<OfHeader>>failed()
                    .withRpcErrors(input.getErrors())
                    .build();
            }
        });
    }

}
