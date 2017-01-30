/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.device.listener;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceReplyProcessor;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.MultiMsgCollector;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.EventIdentifier;
import org.opendaylight.openflowplugin.impl.statistics.ofpspecific.EventsTimeCounter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <p>
 * Implementation for {@link MultiMsgCollector} interface
 *
 * @author <a href="mailto:vdemcak@cisco.com">Vaclav Demcak</a>
 * @author <a href="mailto:tkubas@cisco.com">Timotej Kubas</a>
 *         </p>
 *         Created: Mar 23, 2015
 */
public class MultiMsgCollectorImpl<T extends OfHeader> implements MultiMsgCollector<T> {
    private static final Logger LOG = LoggerFactory.getLogger(MultiMsgCollectorImpl.class);
    private final List<T> replyCollection = new ArrayList<>();
    private final RequestContext<List<T>> requestContext;
    private final DeviceReplyProcessor deviceReplyProcessor;

    public MultiMsgCollectorImpl(final DeviceReplyProcessor deviceReplyProcessor, final RequestContext<List<T>> requestContext) {
        this.deviceReplyProcessor = Preconditions.checkNotNull(deviceReplyProcessor);
        this.requestContext = Preconditions.checkNotNull(requestContext);
    }

    @Override
    public void addMultipartMsg(@Nonnull final T reply, final boolean reqMore, @Nullable final EventIdentifier eventIdentifier) {
        Preconditions.checkNotNull(reply);
        Preconditions.checkNotNull(requestContext.getXid());
        Preconditions.checkArgument(requestContext.getXid().getValue().equals(reply.getXid()));
        LOG.trace("Try to add Multipart reply msg with XID {}", reply.getXid());
        replyCollection.add(reply);

        if (!reqMore) {
            endCollecting(eventIdentifier);
        }
    }

    @Override
    public void endCollecting(@Nullable final EventIdentifier eventIdentifier) {
        final RpcResult<List<T>> rpcResult = RpcResultBuilder.success(replyCollection).build();

        if (Objects.nonNull(eventIdentifier)) {
            EventsTimeCounter.markEnd(eventIdentifier);
        }

        requestContext.setResult(rpcResult);
        requestContext.close();
        deviceReplyProcessor.processReply(requestContext.getXid(), replyCollection);
    }
}
