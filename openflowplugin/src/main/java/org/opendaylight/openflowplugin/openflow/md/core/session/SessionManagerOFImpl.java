/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.session;

import com.google.common.util.concurrent.ListeningExecutorService;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.openflowplugin.api.openflow.md.core.ConnectionConductor;
import org.opendaylight.openflowplugin.api.openflow.md.core.IMDMessageTranslator;
import org.opendaylight.openflowplugin.api.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.api.openflow.md.core.TranslatorKey;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.SessionContext;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.SessionListener;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.SwitchSessionKeyOF;
import org.opendaylight.openflowplugin.api.openflow.md.queue.PopListener;
import org.opendaylight.openflowplugin.api.openflow.statistics.MessageSpy;
import org.opendaylight.openflowplugin.extension.api.core.extension.ExtensionConverterProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.util.ListenerRegistry;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mirehak
 */
public class SessionManagerOFImpl implements ConjunctSessionManager {

    protected static final Logger LOG = LoggerFactory.getLogger(SessionManagerOFImpl.class);
    private static SessionManagerOFImpl instance;
    private ConcurrentHashMap<SwitchSessionKeyOF, SessionContext> sessionLot;
    private Map<TranslatorKey, Collection<IMDMessageTranslator<OfHeader, List<DataObject>>>> translatorMapping;
    private Map<Class<? extends DataObject>, Collection<PopListener<DataObject>>> popListenerMapping;

    protected ListenerRegistry<SessionListener> sessionListeners;
    private NotificationProviderService notificationProviderService;

    private DataBroker dataBroker;
    private ListeningExecutorService rpcPool;


    /**
     * @return singleton instance
     */
    public static ConjunctSessionManager getInstance() {
        if (instance == null) {
            synchronized (SessionContextOFImpl.class) {
                if (instance == null) {
                    instance = new SessionManagerOFImpl();
                }
            }
        }
        return instance;
    }

    /**
     * close and release singleton instance
     */
    public static void releaseInstance() {
        if (instance != null) {
            synchronized (SessionManagerOFImpl.class) {
                if (instance != null) {
                    instance.close();
                    instance = null;
                }
            }
        }
    }

    private SessionManagerOFImpl() {
        LOG.debug("singleton creating");
        sessionLot = new ConcurrentHashMap<>();
        sessionListeners = new ListenerRegistry<>();
    }

    @Override
    public SessionContext getSessionContext(SwitchSessionKeyOF sessionKey) {
        return sessionLot.get(sessionKey);
    }

    @Override
    public void invalidateSessionContext(SwitchSessionKeyOF sessionKey) {
        SessionContext context = getSessionContext(sessionKey);
        if (context == null) {
            LOG.warn("context for invalidation not found");
        } else {
            synchronized (context) {
                for (Entry<SwitchConnectionDistinguisher, ConnectionConductor> auxEntry : context.getAuxiliaryConductors()) {
                    invalidateAuxiliary(sessionKey, auxEntry.getKey());
                }
                context.getPrimaryConductor().disconnect();
                context.setValid(false);
                removeSessionContext(context);
                // TODO:: notify listeners
            }
        }
    }

    private void invalidateDeadSessionContext(SessionContext sessionContext) {
        if (sessionContext == null) {
            LOG.warn("context for invalidation not found");
        } else {
            synchronized (sessionContext) {
                for (Entry<SwitchConnectionDistinguisher, ConnectionConductor> auxEntry : sessionContext
                        .getAuxiliaryConductors()) {
                    invalidateAuxiliary(sessionContext, auxEntry.getKey(), true);
                }
                sessionContext.setValid(false);
                removeSessionContext(sessionContext);
                // TODO:: notify listeners
            }
        }
    }

    private void removeSessionContext(SessionContext sessionContext) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("removing session: {}", Arrays.toString(sessionContext.getSessionKey().getId()));
        }
        sessionLot.remove(sessionContext.getSessionKey(), sessionContext);
        sessionNotifier.onSessionRemoved(sessionContext);
    }

    @Override
    public void addSessionContext(SwitchSessionKeyOF sessionKey, SessionContext context) {
        synchronized (context) {
            sessionLot.put(sessionKey, context);
            sessionNotifier.onSessionAdded(sessionKey, context);
            context.setValid(true);
        }
    }

    @Override
    public void setRole(SessionContext context) {
	sessionNotifier.setRole(context);
    }
    @Override
    public void invalidateAuxiliary(SwitchSessionKeyOF sessionKey,
                                    SwitchConnectionDistinguisher connectionCookie) {
        SessionContext context = getSessionContext(sessionKey);
        invalidateAuxiliary(context, connectionCookie, true);
    }

    /**
     * @param context
     * @param connectionCookie
     * @param disconnect       true if auxiliary connection is to be disconnected
     */
    private static void invalidateAuxiliary(SessionContext context, SwitchConnectionDistinguisher connectionCookie,
                                            boolean disconnect) {
        if (context == null) {
            LOG.warn("context for invalidation not found");
        } else {
            ConnectionConductor auxiliaryConductor = context.removeAuxiliaryConductor(connectionCookie);
            if (auxiliaryConductor == null) {
                LOG.warn("auxiliary conductor not found");
            } else {
                if (disconnect) {
                    auxiliaryConductor.disconnect();
                }
            }
        }
    }

    @Override
    public void invalidateOnDisconnect(ConnectionConductor conductor) {
        if (conductor.getAuxiliaryKey() == null) {
            invalidateDeadSessionContext(conductor.getSessionContext());
            // TODO:: notify listeners
        } else {
            invalidateAuxiliary(conductor.getSessionContext(), conductor.getAuxiliaryKey(), false);
        }
    }

    @Override
    public void setTranslatorMapping(Map<TranslatorKey, Collection<IMDMessageTranslator<OfHeader, List<DataObject>>>> translatorMapping) {
        this.translatorMapping = translatorMapping;
    }

    @Override
    public ListenerRegistration<SessionListener> registerSessionListener(SessionListener listener) {
        LOG.debug("registerSessionListener");
        return sessionListeners.register(listener);
    }

    private final SessionListener sessionNotifier = new SessionListener() {

        @Override
        public void onSessionAdded(SwitchSessionKeyOF sessionKey, SessionContext context) {
            for (ListenerRegistration<SessionListener> listener : sessionListeners) {
                try {
                    listener.getInstance().onSessionAdded(sessionKey, context);
                } catch (Exception e) {
                    LOG.error("Unhandled exeption occured while invoking onSessionAdded on listener", e);
                }
            }
        }

        @Override
        public void setRole(SessionContext context) {
            for (ListenerRegistration<SessionListener> listener : sessionListeners) {
                try {
                    listener.getInstance().setRole(context);
                } catch (Exception e) {
                    LOG.error("Unhandled exeption occured while invoking setRole on listener", e);
                }
            }
        }

        @Override
        public void onSessionRemoved(SessionContext context) {
            for (ListenerRegistration<SessionListener> listener : sessionListeners) {
                try {
                    listener.getInstance().onSessionRemoved(context);
                } catch (Exception e) {
                    LOG.error("Unhandled exeption occured while invoking onSessionRemoved on listener", e);
                }
            }
        }
    };
    private MessageSpy<DataContainer> messageSpy;
    private ExtensionConverterProvider extensionConverterProvider;


    @Override
    public Map<TranslatorKey, Collection<IMDMessageTranslator<OfHeader, List<DataObject>>>> getTranslatorMapping() {
        return this.translatorMapping;
    }

    @Override
    public void setNotificationProviderService(
            NotificationProviderService notificationProviderService) {
        this.notificationProviderService = notificationProviderService;

    }

    @Override
    public DataBroker getDataBroker() {
        return dataBroker;
    }

    @Override
    public void setDataBroker(DataBroker dataBroker) {
        this.dataBroker = dataBroker;

    }

    @Override
    public NotificationProviderService getNotificationProviderService() {
        return notificationProviderService;
    }

    @Override
    public Map<Class<? extends DataObject>, Collection<PopListener<DataObject>>> getPopListenerMapping() {
        return popListenerMapping;
    }

    @Override
    public void setPopListenerMapping(
            Map<Class<? extends DataObject>, Collection<PopListener<DataObject>>> popListenerMapping) {
        this.popListenerMapping = popListenerMapping;
    }

    @Override
    public void close() {
        LOG.debug("close");
        synchronized (sessionLot) {
            for (SessionContext sessionContext : sessionLot.values()) {
                sessionContext.getPrimaryConductor().disconnect();
            }
            // TODO: handle timeouted shutdown
            rpcPool.shutdown();
        }

        for (ListenerRegistration<SessionListener> listenerRegistration : sessionListeners) {
            SessionListener listener = listenerRegistration.getInstance();
            if (listener instanceof AutoCloseable) {
                try {
                    ((AutoCloseable) listener).close();
                } catch (Exception e) {
                    LOG.warn("closing of sessionListenerRegistration failed", e);
                }
            }
        }
    }

    @Override
    public void setRpcPool(ListeningExecutorService rpcPool) {
        this.rpcPool = rpcPool;
    }

    @Override
    public ListeningExecutorService getRpcPool() {
        return rpcPool;
    }

    @Override
    public void setMessageSpy(MessageSpy<DataContainer> messageSpy) {
        this.messageSpy = messageSpy;
    }

    @Override
    public MessageSpy<DataContainer> getMessageSpy() {
        return messageSpy;
    }

    @Override
    public void setExtensionConverterProvider(
            ExtensionConverterProvider extensionConverterProvider) {
        this.extensionConverterProvider = extensionConverterProvider;
    }

    /**
     * @return the extensionConverterProvider
     */
    @Override
    public ExtensionConverterProvider getExtensionConverterProvider() {
        return extensionConverterProvider;
    }

    @Override
    public Collection<SessionContext> getAllSessions() {
        return sessionLot.values();
    }
}
