/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.md.core.session;

import com.google.common.util.concurrent.ListeningExecutorService;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowplugin.api.openflow.md.core.ConnectionConductor;
import org.opendaylight.openflowplugin.api.openflow.md.core.IMDMessageTranslator;
import org.opendaylight.openflowplugin.api.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.api.openflow.md.core.TranslatorKey;
import org.opendaylight.openflowplugin.api.openflow.md.queue.PopListener;
import org.opendaylight.openflowplugin.api.openflow.statistics.MessageSpy;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;

public interface SessionManager extends AutoCloseable {

    /**
     * primary connection.
     * @param sessionKey session key
     * @return corresponding conductor, holding {@link ConnectionAdapter} to
     */
    SessionContext getSessionContext(SwitchSessionKeyOF sessionKey);

    /**
     * disconnect all underlying {@link ConnectionAdapter}s and notify listeners.
     *
     * @param sessionKey session key
     */
    void invalidateSessionContext(SwitchSessionKeyOF sessionKey);

    /**
     * register session context.
     *
     * @param sessionKey session key
     * @param context context
     */
    void addSessionContext(SwitchSessionKeyOF sessionKey, SessionContext context);

    void setRole(SessionContext context);

    /**
     * disconnect particular auxiliary {@link ConnectionAdapter}, identified by
     * sessionKey and connectionCookie.
     *
     * @param sessionKey  session key
     * @param connectionCookie cookie
     */
    void invalidateAuxiliary(SwitchSessionKeyOF sessionKey,
                                    SwitchConnectionDistinguisher connectionCookie);

    /**
     * Invalidate on disconnect.
     * @param connectionConductor connection conductor.
     */
    void invalidateOnDisconnect(ConnectionConductor connectionConductor);

    /**
     * Setter.
     * @param translatorMapping translators
     */
    void setTranslatorMapping(
            Map<TranslatorKey, Collection<IMDMessageTranslator<OfHeader, List<DataObject>>>> translatorMapping);

    /**
     * Getter.
     * @return translator mapping
     */
    Map<TranslatorKey, Collection<IMDMessageTranslator<OfHeader, List<DataObject>>>> getTranslatorMapping();

    /**
     * Setter.
     * @param notificationProviderService notofication provider
     */
    void setNotificationProviderService(NotificationProviderService notificationProviderService);

    /**
     * Getter.
     * @return notificationServiceProvider
     */
    DataBroker getDataBroker();

    /**
     * Setter.
     * @param dataBroker databroker
     */
    void setDataBroker(DataBroker dataBroker);

    /**
     * Gatter.
     * @return notificationServiceProvider
     */
    NotificationProviderService getNotificationProviderService();

    /**
     * Session listener registration.
     * @param listener listener
     * @return registration
     */
    ListenerRegistration<SessionListener> registerSessionListener(SessionListener listener);

    /**
     * Getter.
     * @return popListener mapping, key=message type; value=collection of listeners
     */
    Map<Class<? extends DataObject>, Collection<PopListener<DataObject>>> getPopListenerMapping();

    /**
     * Setter.
     * @param popListenerMapping the popListenerMapping to set
     */
    void setPopListenerMapping(
            Map<Class<? extends DataObject>, Collection<PopListener<DataObject>>> popListenerMapping);

    /**
     * Setter.
     * @param rpcPoolDelegator rpc pool delegator
     */
    void setRpcPool(ListeningExecutorService rpcPoolDelegator);

    /**
     * Getter.
     * @return the rpcPool instance
     */
    ListeningExecutorService getRpcPool();

    /**
     * Setter.
     * @param messageSpy message spy
     */
    void setMessageSpy(MessageSpy<DataContainer> messageSpy);

    /**
     * Getter.
     * @return the messageSpy
     */
    MessageSpy<DataContainer> getMessageSpy();

    /**
     * Getter.
     * @return collection of current sessions
     */
    Collection<SessionContext> getAllSessions();
}
