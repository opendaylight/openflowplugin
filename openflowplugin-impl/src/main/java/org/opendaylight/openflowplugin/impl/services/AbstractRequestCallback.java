/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.FutureCallback;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.openflowjava.protocol.api.connection.DeviceRequestFailedException;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.EventIdentifier;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy.StatisticsGroup;
import org.opendaylight.openflowplugin.impl.services.util.RequestContextUtil;
import org.opendaylight.openflowplugin.impl.statistics.ofpspecific.EventTimeCountersImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.Error;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.common.ErrorType;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

public abstract class AbstractRequestCallback<T> implements FutureCallback<OfHeader> {
    private final RequestContext<T> context;
    private final Class<?> requestType;
    private final MessageSpy spy;
    private final EventIdentifier eventIdentifier;

    AbstractRequestCallback(final RequestContext<T> context,
                            final Class<?> requestType,
                            final MessageSpy spy,
                            final EventIdentifier eventIdentifier) {
        this.context = requireNonNull(context);
        this.requestType = requireNonNull(requestType);
        this.spy = requireNonNull(spy);
        this.eventIdentifier = eventIdentifier;
    }

    protected final void setResult(@Nullable final RpcResult<T> result) {
        context.setResult(result);
        context.close();
    }

    protected final void spyMessage(@NonNull final StatisticsGroup group) {
        spy.spyMessage(requestType, requireNonNull(group));
    }

    public EventIdentifier getEventIdentifier() {
        return eventIdentifier;
    }

    @Override
    public final void onFailure(final Throwable throwable) {
        final RpcResultBuilder<T> builder;
        if (null != eventIdentifier) {
            EventTimeCountersImpl.markEnd(eventIdentifier);
        }
        if (throwable instanceof DeviceRequestFailedException) {
            final Error err = ((DeviceRequestFailedException) throwable).getError();
            final String errorString = String.format("Device reported error type %s code %s",
                                                     err.getTypeString(),
                                                     err.getCodeString());

            builder = RpcResultBuilder.<T>failed().withError(ErrorType.APPLICATION, errorString, throwable);
            spyMessage(StatisticsGroup.TO_SWITCH_SUBMIT_FAILURE);
        } else {
            if (throwable != null) {
                builder = RpcResultBuilder.<T>failed()
                        .withError(ErrorType.APPLICATION, throwable.getMessage(), throwable);
            } else {
                Throwable deviceReadFailedThrowable = new Throwable("Failed to read from device.");
                builder = RpcResultBuilder.<T>failed()
                        .withError(ErrorType.APPLICATION, deviceReadFailedThrowable.getMessage(),
                                deviceReadFailedThrowable);
            }
            spyMessage(StatisticsGroup.TO_SWITCH_SUBMIT_ERROR);
        }

        context.setResult(builder.build());
        RequestContextUtil.closeRequestContext(context);
    }
}
