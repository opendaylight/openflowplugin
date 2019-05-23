/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.device;

import java.util.List;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.openflowplugin.api.openflow.OFPContext;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceReplyProcessor;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.MultiMsgCollector;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChainStateListener;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;

/**
 * The central entity of OFP is the Device Context, which encapsulate the logical state of a switch
 * as seen by the controller. Each OpenFlow session is tracked by a Connection Context.
 * These attach to a particular Device Context in such a way, that there is at most one primary
 * session associated with a Device Context. Whenever the controller needs to interact with a
 * particular switch, it will do so in the context of the calling thread, obtaining a lock on
 * the corresponding Device Context – thus the Device Context becomes the fine-grained point
 * of synchronization. The only two entities allowed to send requests towards the switch are
 * Statistics Manager and RPC Manager. Each of them allocates a Request Context for interacting
 * with a particular Device Context. The Request Contexts are the basic units of fairness,
 * which is enforced by keeping a cap on the number of outstanding requests a particular Request
 * Context can have at any point in time. Should this quota be exceeded, any further attempt to make
 * a request to the switch will fail immediately, with proper error indication.
 */
public interface DeviceContext extends
        OFPContext,
        DeviceReplyProcessor,
        TxFacade,
        DeviceRegistry,
        RequestContextStack,
        ContextChainStateListener {

    /**
     * Method provides state of device represented by this device context.
     *
     * @return {@link DeviceState}
     */
    DeviceState getDeviceState();

    /**
     * Getter.
     *
     * @return current devices connection context
     */
    ConnectionContext getPrimaryConnectionContext();

    /**
     * Getter.
     *
     * @return translator library
     */
    TranslatorLibrary oook();

    /**
     * Sets notification publish service.
     *
     * @param notificationPublishService the notification publish service
     */
    void setNotificationPublishService(NotificationPublishService notificationPublishService);

    /**
     * Gets message spy.
     *
     * @return the message spy
     */
    MessageSpy getMessageSpy();

    /**
     * Gets multi msg collector.
     *
     * @param <T>            the type parameter
     * @param requestContext the request context
     * @return the multi msg collector
     */
    <T extends OfHeader> MultiMsgCollector<T> getMultiMsgCollector(RequestContext<List<T>> requestContext);

    /**
     * Indicates that device context is fully published (e.g.: packetIn messages should be passed).
     */
    void onPublished();

    /**
     * change packetIn rate limiter borders.
     * @param upperBound max amount of outstanding packetIns
     */
    void updatePacketInRateLimit(long upperBound);

    /**
     * Checks if device and controller supports single layer serialization.
     * @return true if single layer serialization is supported
     */
    boolean canUseSingleLayerSerialization();

    /**
     * Method for initial submit transaction after successful initial gathering.
     */
    boolean initialSubmitTransaction();
}

