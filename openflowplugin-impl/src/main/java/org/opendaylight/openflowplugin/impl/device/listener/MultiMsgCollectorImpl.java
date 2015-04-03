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
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.api.openflow.device.exception.DeviceDataException;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceReplyProcessor;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.MultiMsgCollector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * openflowplugin-api
 * org.opendaylight.openflowplugin.impl.openflow.device
 * <p/>
 * Implementation for {@link MultiMsgCollector} interface
 *
 * @author <a href="mailto:vdemcak@cisco.com">Vaclav Demcak</a>
 * @author <a href="mailto:tkubas@cisco.com">Timotej Kubas</a>
 *         <p/>
 *         Created: Mar 23, 2015
 */
@VisibleForTesting
public class MultiMsgCollectorImpl implements MultiMsgCollector {

    private static final Logger LOG = LoggerFactory.getLogger(MultiMsgCollectorImpl.class);

    private final Cache<Long, MultiCollectorObject> cache;
    private DeviceReplyProcessor deviceReplyProcessor;

    public MultiMsgCollectorImpl() {
        cache = initCacheBuilder(DEFAULT_TIME_OUT).build();
    }

    public MultiMsgCollectorImpl(final int timeout) {
        cache = initCacheBuilder(timeout).build();
    }

    private RemovalListener<Long, MultiCollectorObject> getRemovalListener() {
        return new RemovalListener<Long, MultiCollectorObject>() {
            @Override
            public void onRemoval(final RemovalNotification<Long, MultiCollectorObject> notification) {
                LOG.warn("Removing data with XID {} from cache", notification.getKey());
                deviceReplyProcessor.processException(new Xid(notification.getKey()), new DeviceDataException("Data removed from cache"));
                notification.getValue().invalidateFutureByTimeout(notification.getKey());
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
            LOG.trace("Orphaned multipart msg with XID : {}", xid);
            deviceReplyProcessor.processException(new Xid(xid), new DeviceDataException("unknown xid received"));
            return;
        }
        cachedRef.add(reply);
        if (!reply.getFlags().isOFPMPFREQMORE()) {
            // flag OFPMFFREEQMORE false says "I'm a last one'
            cachedRef.populateSettableFuture(xid); // settable futue has now whole collection
            cache.invalidate(xid);              // we don't need a reference anymore
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

        void add(final MultipartReply reply) {
            /* Rise possible exception if it possible */
            msgTypeValidation(reply.getType(), reply.getXid());
            replyCollection.add(reply);
        }

        void populateSettableFuture(long xid) {
            deviceReplyProcessor.processReply(new Xid(xid), replyCollection);
        }

        void invalidateFutureByTimeout(final long key) {
            final String msg = "MultiMsgCollector can not wait for last multipart any more";
            deviceReplyProcessor.processException(new Xid(key), new DeviceDataException(msg));
        }

        void invalidateFutureByInputType(final MultipartType type, final long key) {
            final String msg = "MultiMsgCollector get incorrect multipart msg with type " + type
                    + " but expected type is " + msgType;
            deviceReplyProcessor.processException(new Xid(key), new DeviceDataException(msg));
        }

        private void msgTypeValidation(final MultipartType type, final long key) {
            if (msgType == null) {
                msgType = type;
                return;
            }
            if (!msgType.equals(type)) {
                invalidateFutureByInputType(type, key);
            }
        }
    }
}
