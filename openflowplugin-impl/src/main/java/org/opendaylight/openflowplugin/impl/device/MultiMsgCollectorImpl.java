/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.device;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.opendaylight.openflowplugin.api.openflow.device.MultiMsgCollector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * openflowplugin-api
 * org.opendaylight.openflowplugin.impl.openflow.device
 *
 * Implementation for {@link MultiMsgCollector} interface
 *
 * @author <a href="mailto:vdemcak@cisco.com">Vaclav Demcak</a>
 * @author <a href="mailto:tkubas@cisco.com">Timotej Kubas</a>
 *
 * Created: Mar 23, 2015
 */
@VisibleForTesting
class MultiMsgCollectorImpl implements MultiMsgCollector {

    private static final Logger LOG = LoggerFactory.getLogger(MultiMsgCollectorImpl.class);

    private final Cache<Long, MultiCollectorObject> cache;

    public MultiMsgCollectorImpl () {
        cache = initCacheBuilder(DEFAULT_TIME_OUT).build();
    }

    public MultiMsgCollectorImpl (final int timeout) {
        cache = initCacheBuilder(timeout).build();
    }

    private RemovalListener<Long, MultiCollectorObject> getRemovalListener() {
        return new RemovalListener<Long, MultiCollectorObject>() {
            @Override
            public void onRemoval(final RemovalNotification<Long, MultiCollectorObject> notification) {
                notification.getValue().invalidateFutureByTimeout();
            }
        };
    }

    private CacheBuilder<Long, MultiCollectorObject> initCacheBuilder(final int timeout) {
        return CacheBuilder.newBuilder()
                .expireAfterWrite(timeout, TimeUnit.SECONDS)
                .removalListener(getRemovalListener())
                .initialCapacity(200)
                .maximumSize(500);
    }

    @Override
    public ListenableFuture<Collection<MultipartReply>> registerMultipartMsg(final long xid) {
        final SettableFuture<Collection<MultipartReply>> future = SettableFuture.create();
        cache.put(xid, new MultiCollectorObject(future));
        return future;
    }

    @Override
    public void addMultipartMsg(final MultipartReply reply) {
        Preconditions.checkNotNull(reply);
        final Long xid = reply.getXid();
        final MultiCollectorObject cachedRef = cache.getIfPresent(xid);
        if (cachedRef == null) {
            LOG.info("Orphaned multipart msg with XID : {}", xid);
            return;
        }
        cachedRef.add(reply);
        if ( ! reply.getFlags().isOFPMPFREQMORE()) {
            // flag OFPMFFREEQMORE false says "I'm a last one'
            cachedRef.populateSettableFuture(); // settable futue has now whole collection
            cache.invalidate(xid);              // we don't need a reference anymore
        }
    }

    private class MultiCollectorObject {
        private final SettableFuture<Collection<MultipartReply>> future;
        private final Collection<MultipartReply> replyCollection;

        MultiCollectorObject (final SettableFuture<Collection<MultipartReply>> future) {
            this.future = future;
            replyCollection = new ArrayList<>();
        }

        void add(final MultipartReply reply) {
            replyCollection.add(reply);
        }

        void populateSettableFuture() {
            future.set(replyCollection);
        }

        void invalidateFutureByTimeout() {
            final String msg = "MultiMsgCollector can not wait for last multipart any more";
            future.setException(new TimeoutException(msg));
        }
    }
}
