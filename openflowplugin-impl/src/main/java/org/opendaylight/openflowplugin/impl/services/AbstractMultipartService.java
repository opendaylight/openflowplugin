/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Nonnull;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.impl.services.multilayer.MultiLayerMultipartRequestCallback;
import org.opendaylight.openflowplugin.impl.services.singlelayer.SingleLayerMultipartRequestCallback;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.common.RpcResult;

public abstract class AbstractMultipartService<I, T extends OfHeader> extends AbstractService<I, List<T>> {

    private static final Function<OfHeader, Boolean> ALTERNATE_IS_COMPLETE = message ->
        !(message instanceof MultipartReply) || !((MultipartReply) message).isRequestMore();

    protected AbstractMultipartService(final RequestContextStack requestContextStack, final DeviceContext deviceContext) {
        super(requestContextStack, deviceContext);
    }

    @Override
    protected FutureCallback<OfHeader> createCallback(RequestContext<List<T>> context, Class<?> requestType) {
        return canUseSingleLayerSerialization()
            ? new SingleLayerMultipartRequestCallback<>(context, requestType, getDeviceContext(), getEventIdentifier())
            : new MultiLayerMultipartRequestCallback<>(context, requestType, getDeviceContext(), getEventIdentifier());
    }

    @Override
    public final ListenableFuture<RpcResult<List<T>>> handleServiceCall(@Nonnull final I input) {
        return canUseSingleLayerSerialization()
            ? super.handleServiceCall(input, ALTERNATE_IS_COMPLETE)
            : super.handleServiceCall(input);
    }

}
