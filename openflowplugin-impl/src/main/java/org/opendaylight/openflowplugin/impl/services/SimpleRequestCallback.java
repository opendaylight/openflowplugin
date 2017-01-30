/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.FutureCallback;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SimpleRequestCallback<T extends DataObject> extends AbstractRequestCallback<T> {
    private static final Logger LOG = LoggerFactory.getLogger(SimpleRequestCallback.class);
    private final Class<T> clazz;

    private SimpleRequestCallback(final RequestContext<T> context, final Class<?> requestType, final MessageSpy spy, final Class<T> clazz) {
        super(context, requestType, spy, null);
        this.clazz = Preconditions.checkNotNull(clazz);
    }

    public static <T extends DataObject> FutureCallback<OfHeader> create(final RequestContext<T> context, final Class<?> requestType, final MessageSpy spy, final Class<T> clazz) {
        return new SimpleRequestCallback<>(context, requestType, spy, clazz);
    }

    @Override
    public final void onSuccess(final OfHeader result) {
        spyMessage(MessageSpy.STATISTIC_GROUP.TO_SWITCH_SUBMIT_SUCCESS);

        if (result == null) {
            setResult(RpcResultBuilder.<T>success().build());
            return;
        }
        if (!clazz.isInstance(result)) {
            LOG.info("Expected response type {}, got {}, result is empty", clazz, result.getClass());
            setResult(RpcResultBuilder.<T>success().build());
            return;

        }

        setResult(RpcResultBuilder.success(clazz.cast(result)).build());
    }
}
