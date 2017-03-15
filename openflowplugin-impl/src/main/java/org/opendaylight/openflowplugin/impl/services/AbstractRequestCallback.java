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
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.openflowjava.protocol.api.connection.DeviceRequestFailedException;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.EventIdentifier;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy.STATISTIC_GROUP;
import org.opendaylight.openflowplugin.impl.services.util.RequestContextUtil;
import org.opendaylight.openflowplugin.impl.statistics.ofpspecific.EventsTimeCounter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.Error;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

public abstract class AbstractRequestCallback<T> implements FutureCallback<OfHeader> {
    private final RequestContext<T> context;
    private final Class<?> requestType;
    private final MessageSpy spy;
    private EventIdentifier eventIdentifier;

    AbstractRequestCallback(final RequestContext<T> context,
                            final Class<?> requestType,
                            final MessageSpy spy,
                            final EventIdentifier eventIdentifier) {
        this.context = Preconditions.checkNotNull(context);
        this.requestType = Preconditions.checkNotNull(requestType);
        this.spy = Preconditions.checkNotNull(spy);
        this.eventIdentifier = eventIdentifier;
    }

    protected final void setResult(@Nullable final RpcResult<T> result) {
        context.setResult(result);
        context.close();
    }

    protected final void spyMessage(@Nonnull final STATISTIC_GROUP group) {
        spy.spyMessage(requestType, Preconditions.checkNotNull(group));
    }

    public EventIdentifier getEventIdentifier() {
        return eventIdentifier;
    }

    @Override
    public final void onFailure(@Nonnull final Throwable t) {
        final RpcResultBuilder<T> builder;
        if (null != eventIdentifier) {
            EventsTimeCounter.markEnd(eventIdentifier);
        }
        if (t instanceof DeviceRequestFailedException) {
            final Error err = ((DeviceRequestFailedException) t).getError();
            final String errorString = String.format("Device reported error type %s code %s", err.getTypeString(), err.getCodeString());

            builder = RpcResultBuilder.<T>failed().withError(RpcError.ErrorType.APPLICATION, errorString, t);
            spyMessage(MessageSpy.STATISTIC_GROUP.TO_SWITCH_SUBMIT_FAILURE);
        } else {
            builder = RpcResultBuilder.<T>failed().withError(RpcError.ErrorType.APPLICATION, t.getMessage(), t);
            spyMessage(MessageSpy.STATISTIC_GROUP.TO_SWITCH_SUBMIT_ERROR);
        }

        context.setResult(builder.build());
        RequestContextUtil.closeRequestContext(context);
    }
}
