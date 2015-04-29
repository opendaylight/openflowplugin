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
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.api.openflow.device.exception.DeviceDataException;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceReplyProcessor;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.MultiMsgCollector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * openflowplugin-api
 * org.opendaylight.openflowplugin.impl.openflow.device
 * <p>
 * Implementation for {@link MultiMsgCollector} interface
 *
 * @author <a href="mailto:vdemcak@cisco.com">Vaclav Demcak</a>
 * @author <a href="mailto:tkubas@cisco.com">Timotej Kubas</a>
 *         <p>
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

    private RemovalListener<Long, MultiCollectorObject> getRemovalListener() {
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

    private CacheBuilder<Long, MultiCollectorObject> initCacheBuilder(final int timeout) {
        return CacheBuilder.newBuilder()
                .expireAfterAccess(timeout, TimeUnit.SECONDS)
                .removalListener(getRemovalListener())
                .initialCapacity(200)
                .maximumSize(500)
                .concurrencyLevel(1);
    }

    @Override
    public void registerMultipartXid(final long xid) {
        cache.put(xid, new MultiCollectorObject());
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
                    new DeviceDataException("unknown xid received for multipart of type "+multipartType));
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
    public void setDeviceReplyProcessor(DeviceReplyProcessor deviceReplyProcessor) {
        this.deviceReplyProcessor = deviceReplyProcessor;
    }

    private class MultiCollectorObject {
        private final List<MultipartReply> replyCollection;
        private MultipartType msgType;

        MultiCollectorObject() {
            replyCollection = new ArrayList<>();
        }

        void add(final MultipartReply reply) throws DeviceDataException {
            /* Rise possible exception if it possible */
            msgTypeValidation(reply.getType(), reply.getXid());
            replyCollection.add(reply);
        }

        void publishCollection(long xid) {
            deviceReplyProcessor.processReply(new Xid(xid), replyCollection);
        }

        void invalidateFutureByTimeout(final long key) {
            final String msg = "MultiMsgCollector can not wait for last multipart any more";
            deviceReplyProcessor.processException(new Xid(key), new DeviceDataException(msg));
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
