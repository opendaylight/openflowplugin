/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import java.util.List;
import java.util.Objects;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.MultiMsgCollector;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.EventIdentifier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractMultipartRequestCallback<T extends OfHeader> extends AbstractRequestCallback<List<T>> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractMultipartRequestCallback.class);
    private final MultiMsgCollector<T> collector;

    public AbstractMultipartRequestCallback(
            final RequestContext<List<T>> context,
            final Class<?> requestType,
            final DeviceContext deviceContext,
            final EventIdentifier eventIdentifier) {
        super(context, requestType, deviceContext.getMessageSpy(), eventIdentifier);
        collector = deviceContext.getMultiMsgCollector(context);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onSuccess(final OfHeader result) {
        if (Objects.isNull(result)) {
            LOG.info("Response received was null.");
            collector.endCollecting(getEventIdentifier());
            return;
        }

        if (!isMultipart(result)) {
            LOG.info("Unexpected response type received: {}.", result.getClass());

            setResult(RpcResultBuilder
                    .<List<T>>failed()
                    .withError(RpcError.ErrorType.APPLICATION,
                            String.format("Unexpected response type received: %s.", result.getClass()))
                    .build());
        } else {
            final T resultCast = (T) result;
            collector.addMultipartMsg(resultCast, isReqMore(resultCast), getEventIdentifier());
        }
    }

    /**
     * Check if result is multipart
     * @param result result
     * @return true if result is multipart
     */
    protected abstract boolean isMultipart(final OfHeader result);

    /**
     * Check if result requests more multiparts
     * @param result result
     * @return true if result requests more multiparts
     */
    protected abstract boolean isReqMore(final T result);

}
