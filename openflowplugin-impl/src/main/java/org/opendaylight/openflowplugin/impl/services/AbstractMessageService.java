/**
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.services;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

abstract class AbstractMessageService<R extends DataObject, I extends Builder<? extends R>, O extends DataObject>
        extends AbstractSimpleService<R, O> {
    private final boolean useSingleLayerSerialization;

    protected AbstractMessageService(RequestContextStack requestContextStack, DeviceContext deviceContext, Class<O> clazz) {
        super(requestContextStack, deviceContext, clazz);
        useSingleLayerSerialization = deviceContext.isUseSingleLayerSerialization();
    }

    @Override
    public ListenableFuture<RpcResult<O>> handleServiceCall(R input) {
        return Futures.withFallback(super.handleServiceCall(input), t -> RpcResultBuilder.<O>failed().buildFuture());
    }

    /**
     * Check if this service is supported in current OpenFlowPlugin configuration
     * @return true if supported and single layer serialization is turned on
     */
    public boolean isSupported() {
        return useSingleLayerSerialization;
    }
}
