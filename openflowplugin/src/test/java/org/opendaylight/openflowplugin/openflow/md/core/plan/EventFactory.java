/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.plan;

import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.Callable;

import org.opendaylight.openflowjava.protocol.api.connection.ConnectionReadyListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.binding.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mirehak
 */
public abstract class EventFactory {

    private static final Logger LOG = LoggerFactory
            .getLogger(EventFactory.class);

    /** default protocol version */
    public static final Short DEFAULT_VERSION = 4;

    /**
     * @param xid
     *            transaction id
     * @param version
     *            version id
     * @param builder
     *            message builder instance
     * @return default notification event
     */
    public static SwitchTestNotificationEvent createDefaultNotificationEvent(
            long xid, short version, Object builder) {
        SwitchTestNotificationEventImpl event = new SwitchTestNotificationEventImpl();
        Notification notification = build(setupHeader(xid, version, builder));
        event.setNotification(notification);
        return event;
    }

    /**
     * @param xid
     *            transaction id
     * @param version
     *            version id
     * @param builder
     *            rpc response builder instance
     * @return default notification event
     */
    public static SwitchTestRcpResponseEvent createDefaultRpcResponseEvent(
            long xid, short version, Object builder) {
        SwitchTestRcpResponseEventImpl event = new SwitchTestRcpResponseEventImpl();
        OfHeader rpcResponse = build(setupHeader(xid, version, builder));
        event.setResponse(rpcResponse);
        event.setXid(xid);
        return event;
    }

    /**
     * @param setupHeader
     * @return
     */
    @SuppressWarnings("unchecked")
    private static <E> E build(Object builder) {
        E notification = null;
        try {
            Class<?> builderClazz = builder.getClass();
            notification = (E) builderClazz.getMethod("build", new Class[0])
                    .invoke(builder, new Object[0]);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return notification;
    }

    /**
     * @param xid
     *            transaction id
     * @param rpcName
     *            name of rpc method
     * @return default notification event
     */
    public static SwitchTestWaitForRpcEvent createDefaultWaitForRpcEvent(
            long xid, String rpcName) {
        SwitchTestWaitForRpcEventImpl event = new SwitchTestWaitForRpcEventImpl();
        event.setRpcName(rpcName);
        event.setXid(xid);
        return event;
    }
    
    /**
     * @param events
     * @return wait for all wrapper
     */
    public static SwitchTestWaitForAllEvent createDefaultWaitForAllEvent(
            SwitchTestWaitForRpcEvent... events) {
        SwitchTestWaitForAllEventImpl eventBag = new SwitchTestWaitForAllEventImpl();
        HashSet<SwitchTestWaitForRpcEvent> eventsSet = new HashSet<>(Arrays.asList(events));
        eventBag.setEventBag(eventsSet);
        return eventBag;
    }

    /**
     * @param xid
     * @param version
     * @param builder
     * @return original builder
     */
    public static <E> E setupHeader(long xid, short version, E builder) {
        try {
            Class<?> builderClazz = builder.getClass();
            builderClazz.getMethod("setXid", Long.class).invoke(builder, xid);
            builderClazz.getMethod("setVersion", Short.class).invoke(builder,
                    version);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }

        return builder;
    }

    /**
     * use {@link #DEFAULT_VERSION}
     * @param xid
     * @param builder
     * @return original builder
     */
    public static <E> E setupHeader(long xid, E builder) {
        return setupHeader(xid, DEFAULT_VERSION, builder);
    }

    /**
     * @param connectionConductor 
     * @return scenario callback
     */
    public static SwitchTestCallbackEventImpl createConnectionReadyCallback(
            final ConnectionReadyListener connectionConductor) {
        SwitchTestCallbackEventImpl connectionReadyCallback = new SwitchTestCallbackEventImpl();
        connectionReadyCallback.setCallable(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                connectionConductor.onConnectionReady();
                return null;
            }
        });
        return connectionReadyCallback;
    }

}
