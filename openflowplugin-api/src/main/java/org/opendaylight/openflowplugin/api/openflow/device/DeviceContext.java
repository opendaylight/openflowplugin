/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.device;

import io.netty.util.Timeout;
import java.math.BigInteger;
import org.opendaylight.controller.md.sal.binding.api.ReadTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.openflowplugin.api.openflow.OpenFlowPluginTimer;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceContextClosedHandler;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceDisconnectedHandler;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceReplyProcessor;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.MessageHandler;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.OutstandingMessageExtractor;
import org.opendaylight.openflowplugin.api.openflow.device.listener.OpenflowMessageListenerFacade;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.DeviceFlowRegistry;
import org.opendaylight.openflowplugin.api.openflow.registry.group.DeviceGroupRegistry;
import org.opendaylight.openflowplugin.api.openflow.registry.meter.DeviceMeterRegistry;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.openflowplugin.api.openflow.translator.TranslatorLibrarian;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * <p>
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
 * </p>
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 25.2.2015.
 */
public interface DeviceContext extends AutoCloseable,
        OpenFlowPluginTimer,
        MessageHandler,
        TranslatorLibrarian,
        OutstandingMessageExtractor,
        DeviceReplyProcessor,
        DeviceDisconnectedHandler {


    /**
     * Method add auxiliary connection contexts to this context representing single device connection.
     *
     * @param connectionContext
     */
    void addAuxiliaryConenctionContext(ConnectionContext connectionContext);

    /**
     * Method removes auxiliary connection context from this context representing single device connection.
     *
     * @param connectionContext
     */
    void removeAuxiliaryConenctionContext(ConnectionContext connectionContext);


    /**
     * Method provides state of device represented by this device context.
     *
     * @return {@link DeviceState}
     */
    DeviceState getDeviceState();

    /**
     * Method creates put operation using provided data in underlying transaction chain.
     */
    <T extends DataObject> void writeToTransaction(final LogicalDatastoreType store, final InstanceIdentifier<T> path, final T data);

    /**
     * Method creates delete operation for provided path in underlying transaction chain.
     */
    <T extends DataObject> void addDeleteToTxChain(final LogicalDatastoreType store, final InstanceIdentifier<T> path);

    /**
     * Method exposes transaction created for device
     * represented by this context. This read only transaction has a fresh dataStore snapshot.
     * There is a possibility to get different data set from  DataStore
     * as write transaction in this context.
     */
    ReadTransaction getReadTransaction();


    /**
     * Method provides current devices connection context.
     *
     * @return
     */
    ConnectionContext getPrimaryConnectionContext();

    /**
     * Method provides current devices auxiliary connection contexts.
     *
     * @return
     */
    ConnectionContext getAuxiliaryConnectiobContexts(BigInteger cookie);

    /**
     * Method generates unique XID value.
     *
     * @return
     */
    Xid getNextXid();

    /**
     * @param xid key
     * @return request by xid
     */
    RequestContext lookupRequest(Xid xid);

    /**
     * @return number of outstanding requests in map
     */
    int getNumberOfOutstandingRequests();

    /**
     * Method writes request context into request context map. This method
     * is ment to be used by org.opendaylight.openflowplugin.impl.services.OFJResult2RequestCtxFuture#processResultFromOfJava.
     *
     * @param xid
     * @param requestFutureContext
     */
    void hookRequestCtx(Xid xid, RequestContext requestFutureContext);

    /**
     * Method removes request context from request context map.
     *
     * @param xid
     */
    RequestContext unhookRequestCtx(Xid xid);

    /**
     * Method that attaches anyMessageTypeListener to connection adapters as message listener.
     *
     * @param openflowMessageListenerFacade
     */
    void attachOpenflowMessageListener(OpenflowMessageListenerFacade openflowMessageListenerFacade);

    /**
     * Method returns registered {@link org.opendaylight.openflowplugin.api.openflow.device.listener.OpenflowMessageListenerFacade}
     *
     * @return
     */
    OpenflowMessageListenerFacade getOpenflowMessageListenerFacade();

    /**
     * Method exposes flow registry used for storing flow ids identified by calculated flow hash.
     *
     * @return
     */
    DeviceFlowRegistry getDeviceFlowRegistry();

    /**
     * Method exposes device group registry used for storing group ids.
     *
     * @return
     */
    DeviceGroupRegistry getDeviceGroupRegistry();

    /**
     * Method exposes device meter registry used for storing meter ids.
     *
     * @return
     */
    DeviceMeterRegistry getDeviceMeterRegistry();


    /**
     * store cancellable timeout handler of currently running barrier task
     */
    void setCurrentBarrierTimeout(Timeout timeout);

    /**
     * @return cancellable timeout handle of currently running barrier task
     */
    Timeout getBarrierTaskTimeout();

    /**
     * Sets notification service
     *
     * @param notificationService
     */
    void setNotificationService(NotificationProviderService notificationService);

    MessageSpy getMessageSpy();

    void setDeviceDisconnectedHandler(DeviceDisconnectedHandler deviceDisconnectedHandler);

    /**
     * Method sets reference to handler used for cleanup after device context about to be closed.
     */
    void addDeviceContextClosedHandler(DeviceContextClosedHandler deviceContextClosedHandler);

}

