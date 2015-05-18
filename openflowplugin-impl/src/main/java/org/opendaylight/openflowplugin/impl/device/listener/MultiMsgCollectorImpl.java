/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.device.listener;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.api.openflow.device.exception.DeviceDataException;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceReplyProcessor;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.MultiMsgCollector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <p>
 * openflowplugin-api
 * org.opendaylight.openflowplugin.impl.openflow.device
 *
 * Implementation for {@link MultiMsgCollector} interface
 *
 * @author <a href="mailto:vdemcak@cisco.com">Vaclav Demcak</a>
 * @author <a href="mailto:tkubas@cisco.com">Timotej Kubas</a>
 *         </p>
 *         Created: Mar 23, 2015
 */
@VisibleForTesting
public class MultiMsgCollectorImpl implements MultiMsgCollector {

    private static final Logger LOG = LoggerFactory.getLogger(MultiMsgCollectorImpl.class);

    private final Cache<Long, MultiCollectorObject> cache;
    private DeviceReplyProcessor deviceReplyProcessor;

    public MultiMsgCollectorImpl() {
        this(DEFAULT_TIME_OUT);
    }

    public MultiMsgCollectorImpl(final int timeout) {
        cache = initCacheBuilder(timeout).build();
    }

    private static RemovalListener<Long, MultiCollectorObject> getRemovalListener() {
        return new RemovalListener<Long, MultiCollectorObject>() {
            @Override
            public void onRemoval(final RemovalNotification<Long, MultiCollectorObject> notification) {
                LOG.trace("Removing data with XID {} from cache, cause: {}", notification.getKey(), notification.getCause());
                switch (notification.getCause()) {
                    case EXPIRED:
                        notification.getValue().invalidateFutureByTimeout(notification.getKey());
                }
            }
        };
    }

    private static CacheBuilder<Long, MultiCollectorObject> initCacheBuilder(final int timeout) {
        return CacheBuilder.newBuilder()
                .expireAfterAccess(timeout, TimeUnit.SECONDS)
                .removalListener(getRemovalListener())
                .initialCapacity(200)
                .maximumSize(500)
                .concurrencyLevel(1);
    }

    @Override
    public void registerMultipartRequestContext(final RequestContext requestContext) {
        cache.put(requestContext.getXid().getValue(), new MultiCollectorObject(requestContext));
    }

    @Override
    public void addMultipartMsg(final MultipartReply reply) {
        Preconditions.checkNotNull(reply);
        LOG.trace("Try to add Multipart reply msg with XID {}", reply.getXid());
        final long xid = reply.getXid();
        final MultiCollectorObject cachedRef = cache.getIfPresent(xid);
        if (cachedRef == null) {
            MultipartType multipartType = reply.getType();
            LOG.trace("Orphaned multipart msg with XID : {} of type {}", xid, multipartType);
            deviceReplyProcessor.processException(new Xid(xid),
                    new DeviceDataException("unknown xid received for multipart of type " + multipartType));
            return;
        }

        try {
            cachedRef.add(reply);
            LOG.trace("Multipart reply msg with XID {} added successfully.", reply.getXid());
            if (!reply.getFlags().isOFPMPFREQMORE()) {
                // flag OFPMFFREEQMORE false says "I'm a last one'
                cachedRef.publishCollection(xid); // settable future has now whole collection
                cache.invalidate(xid);              // we don't need a reference anymore - remove explicitly
            }
        } catch (DeviceDataException e) {
            deviceReplyProcessor.processException(new Xid(xid), e);
        }
    }

    @Override
    public void setDeviceReplyProcessor(final DeviceReplyProcessor deviceReplyProcessor) {
        this.deviceReplyProcessor = deviceReplyProcessor;
    }

    private class MultiCollectorObject {
        private final List<MultipartReply> replyCollection;
        private MultipartType msgType;
        private final RequestContext requestContext;

        MultiCollectorObject(final RequestContext requestContext) {
            replyCollection = new ArrayList<>();
            this.requestContext = requestContext;
        }

        void add(final MultipartReply reply) throws DeviceDataException {
            /* Rise possible exception if it possible */
            msgTypeValidation(reply.getType(), reply.getXid());
            replyCollection.add(reply);
        }

        void publishCollection(final long xid) {
            final RpcResult<List<MultipartReply>> rpcResult = RpcResultBuilder
                    .<List<MultipartReply>>success()
                    .withResult(replyCollection)
                    .build();
            requestContext.setResult(rpcResult);
            try {
                requestContext.close();
            } catch (final Exception e) {
                LOG.warn("Closing RequestContext failed: {}", e.getMessage());
                LOG.debug("Closing RequestContext failed.. ", e);
            }
            deviceReplyProcessor.processReply(new Xid(xid), replyCollection);
        }

        void invalidateFutureByTimeout(final long key) {
            final String msg = "MultiMsgCollector can not wait for last multipart any more";
            DeviceDataException deviceDataException = new DeviceDataException(msg);
            final RpcResult<List<OfHeader>> rpcResult = RpcResultBuilder
                    .<List<OfHeader>>failed()
                    .withError(RpcError.ErrorType.APPLICATION, String.format("Message processing failed : %s", deviceDataException.getError()), deviceDataException)
                    .build();
            requestContext.setResult(rpcResult);
            try {
                requestContext.close();
            } catch (final Exception e) {
                LOG.warn("Closing RequestContext failed: ", e);
                LOG.debug("Closing RequestContext failed..", e);
            }
            deviceReplyProcessor.processException(new Xid(key), deviceDataException);
        }

        public RequestContext getRequestContext() {
            return requestContext;
        }

        private void msgTypeValidation(final MultipartType type, final long key) throws DeviceDataException {
            if (msgType == null) {
                msgType = type;
                return;
            }
            if (!msgType.equals(type)) {
                final String msg = "MultiMsgCollector get incorrect multipart msg with type {}"
                        + " but expected type is {}";
                LOG.trace(msg, type, msgType);
                throw new DeviceDataException("multipart message type mismatch");
            }
        }
    }
}
