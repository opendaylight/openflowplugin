/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.singlelayer;

import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.impl.services.AbstractSimpleService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.AsyncConfigMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.SetAsync;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.SetAsyncInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.SetAsyncOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.common.RpcResult;

public final class SetAsyncImpl extends AbstractSimpleService<SetAsyncInput, SetAsyncOutput> implements SetAsync {
    public SetAsyncImpl(final RequestContextStack requestContextStack,
                                            final DeviceContext deviceContext) {
        super(requestContextStack, deviceContext, SetAsyncOutput.class);
    }

    @Override
    public ListenableFuture<RpcResult<SetAsyncOutput>> invoke(final SetAsyncInput input) {
        return handleServiceCall(input);
    }

    @Override
    protected OfHeader buildRequest(final Xid xid, final SetAsyncInput input) {
        return new AsyncConfigMessageBuilder(input)
                .setVersion(getVersion())
                .setXid(xid.getValue())
                .build();
    }
}
