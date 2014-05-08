/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.session;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.controller.sal.binding.api.data.DataProviderService;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowplugin.openflow.md.core.ConnectionConductor;
import org.opendaylight.openflowplugin.openflow.md.core.IMDMessageTranslator;
import org.opendaylight.openflowplugin.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.openflow.md.core.TranslatorKey;
import org.opendaylight.openflowplugin.openflow.md.queue.PopListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * @author mirehak
 */
public interface SessionManager extends AutoCloseable {

    /**
     * @param sessionKey
     * @return corresponding conductor, holding {@link ConnectionAdapter} to
     *         primary connection
     */
    public SessionContext getSessionContext(SwitchSessionKeyOF sessionKey);

    /**
     * disconnect all underlying {@link ConnectionAdapter}s and notify listeners
     *
     * @param sessionKey
     */
    public void invalidateSessionContext(SwitchSessionKeyOF sessionKey);

    /**
     * register session context
     *
     * @param sessionKey
     * @param context
     */
    public void addSessionContext(SwitchSessionKeyOF sessionKey, SessionContext context);

    /**
     * disconnect particular auxiliary {@link ConnectionAdapter}, identified by
     * sessionKey and connectionCookie
     *
     * @param sessionKey
     * @param connectionCookie
     */
    public void invalidateAuxiliary(SwitchSessionKeyOF sessionKey,
            SwitchConnectionDistinguisher connectionCookie);

    /**
     * @param connectionConductor
     */
    public void invalidateOnDisconnect(ConnectionConductor connectionConductor);

    /**
     * @param translatorMapping
     */
    public void setTranslatorMapping(Map<TranslatorKey, Collection<IMDMessageTranslator<OfHeader, List<DataObject>>>> translatorMapping);

    /**
     * @return translator mapping
     */
    public Map<TranslatorKey, Collection<IMDMessageTranslator<OfHeader, List<DataObject>>>> getTranslatorMapping();

    /**
     * @param notificationProviderService
     */
    public void setNotificationProviderService(NotificationProviderService notificationProviderService);

    /**
     * @return notificationServiceProvider
     */
    public DataProviderService getDataProviderService();

    /**
     * @param dataServiceProvider
     */
    public void setDataProviderService(DataProviderService dataServiceProvider);

    /**
     * @return notificationServiceProvider
     */
    public NotificationProviderService getNotificationProviderService();

    /**
     * @param listener
     * @return registration
     */
    public ListenerRegistration<SessionListener> registerSessionListener(SessionListener listener);

    /**
     * @return popListener mapping, key=message type; value=collection of listeners
     */
    public Map<Class<? extends DataObject>, Collection<PopListener<DataObject>>> getPopListenerMapping();

    /**
     * @param popListenerMapping the popListenerMapping to set
     */
    void setPopListenerMapping(Map<Class<? extends DataObject>, Collection<PopListener<DataObject>>> popListenerMapping);

    /**
     * @param newFixedThreadPool
     */
    void setRpcPool(ExecutorService newFixedThreadPool);

    /**
     * @return the rpcPool instance
     */
    ExecutorService getRpcPool();
}
