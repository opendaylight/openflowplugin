/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import java.util.List;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.MultiMsgCollector;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.EventIdentifier;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class MultipartRequestCallback extends AbstractRequestCallback<List<MultipartReply>> {
    private static final Logger LOG = LoggerFactory.getLogger(MultipartRequestCallback.class);
    private final MultiMsgCollector collector;

    public MultipartRequestCallback(final RequestContext<List<MultipartReply>> context, final Class<?> requestType, final MessageSpy messageSpy, final MultiMsgCollector collector) {
        super(context, requestType, messageSpy);
        this.collector = collector;
    }

    public MultipartRequestCallback(final RequestContext<List<MultipartReply>> context,
                                    final Class<?> requestType,
                                    final MessageSpy messageSpy,
                                    final EventIdentifier eventIdentifier,
                                    final MultiMsgCollector collector) {
        super(context, requestType, messageSpy, eventIdentifier);
        this.collector = collector;
    }

    @Override
    public void onSuccess(final OfHeader result) {
        if (result == null) {
            LOG.info("Ofheader was null.");
            collector.endCollecting(getEventIdentifier());
            return;
        }

        if (!(result instanceof MultipartReply)) {
            LOG.info("Unexpected response type received {}.", result.getClass());
            final RpcResultBuilder<List<MultipartReply>> rpcResultBuilder =
                    RpcResultBuilder.<List<MultipartReply>>failed().withError(RpcError.ErrorType.APPLICATION,
                            String.format("Unexpected response type received %s.", result.getClass()));
            setResult(rpcResultBuilder.build());
        } else {
            collector.addMultipartMsg((MultipartReply) result, getEventIdentifier());
        }
    }

}
