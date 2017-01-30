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
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

public abstract class AbstractSilentErrorService<I, O extends DataObject>
        extends AbstractSimpleService<I, O> {

    protected AbstractSilentErrorService(RequestContextStack requestContextStack, DeviceContext deviceContext, Class<O> clazz) {
        super(requestContextStack, deviceContext, clazz);
    }

    @Override
    public ListenableFuture<RpcResult<O>> handleServiceCall(@Nonnull I input,
                                                            @Nullable final Function<OfHeader, Boolean> isComplete) {
        return Futures.withFallback(
                super.handleServiceCall(input, isComplete),
                t -> RpcResultBuilder.<O>failed().buildFuture());
    }

}
