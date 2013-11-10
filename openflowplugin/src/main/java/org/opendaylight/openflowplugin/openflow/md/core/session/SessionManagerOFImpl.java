/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.session;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.opendaylight.openflowplugin.openflow.md.core.ConnectionConductor;
import org.opendaylight.openflowplugin.openflow.md.core.IMDMessageTranslator;
import org.opendaylight.openflowplugin.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.openflow.md.core.TranslatorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.concepts.util.ListenerRegistry;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mirehak
 */
public class SessionManagerOFImpl implements SessionManager {

    protected static final Logger LOG = LoggerFactory.getLogger(SessionManagerOFImpl.class);
    private static SessionManagerOFImpl instance;
    private ConcurrentHashMap<SwitchConnectionDistinguisher, SessionContext> sessionLot;
    private Map<TranslatorKey, Collection<IMDMessageTranslator<OfHeader, DataObject>>> translatorMapping;

    protected final ListenerRegistry<SessionListener> sessionListeners = new ListenerRegistry<>();

    /**
     * @return singleton instance
     */
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManagerOFImpl();
        }
        return instance;
    }

    private SessionManagerOFImpl() {
        sessionLot = new ConcurrentHashMap<>();
    }

    @Override
    public SessionContext getSessionContext(SwitchConnectionDistinguisher sessionKey) {
        return sessionLot.get(sessionKey);
    }

    @Override
    public void invalidateSessionContext(SwitchConnectionDistinguisher sessionKey) {
        SessionContext context = getSessionContext(sessionKey);
        if (context == null) {
            LOG.warn("context for invalidation not found");
        } else {
            for (Entry<SwitchConnectionDistinguisher, ConnectionConductor> auxEntry : context.getAuxiliaryConductors()) {
                invalidateAuxiliary(sessionKey, auxEntry.getKey());
            }
            context.getPrimaryConductor().disconnect();
            context.setValid(false);
            removeSessionContext(context);
            // TODO:: notify listeners
        }
    }

    private void invalidateDeadSessionContext(SessionContext sessionContext) {
        if (sessionContext == null) {
            LOG.warn("context for invalidation not found");
        } else {
            for (Entry<SwitchConnectionDistinguisher, ConnectionConductor> auxEntry : sessionContext
                    .getAuxiliaryConductors()) {
                invalidateAuxiliary(sessionContext, auxEntry.getKey(), true);
            }
            sessionContext.setValid(false);
            removeSessionContext(sessionContext);
            // TODO:: notify listeners
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
    public void addSessionContext(SwitchConnectionDistinguisher sessionKey, SessionContext context) {
        sessionLot.put(sessionKey, context);

        sessionNotifier.onSessionAdded(sessionKey, context);

    }

    @Override
    public void invalidateAuxiliary(SwitchConnectionDistinguisher sessionKey,
            SwitchConnectionDistinguisher connectionCookie) {
        SessionContext context = getSessionContext(sessionKey);
        invalidateAuxiliary(context, connectionCookie, true);
    }

    /**
     * @param context
     * @param connectionCookie
     * @param disconnect
     *            true if auxiliary connection is to be disconnected
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
    public void setTranslatorMapping(Map<TranslatorKey, Collection<IMDMessageTranslator<OfHeader, DataObject>>> translatorMapping) {
        this.translatorMapping = translatorMapping;
    }

    @Override
    public ListenerRegistration<SessionListener> registerSessionListener(SessionListener listener) {
        return sessionListeners.register(listener);
    }

    private final SessionListener sessionNotifier = new SessionListener() {

        @Override
        public void onSessionAdded(SwitchConnectionDistinguisher sessionKey, SessionContext context) {
            for (ListenerRegistration<SessionListener> listener : sessionListeners) {
                try {
                    listener.getInstance().onSessionAdded(sessionKey, context);
                } catch (Exception e) {
                    LOG.error("Unhandled exeption occured while invoking onSessionAdded on listener", e);
                }
            }
        }

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

    @Override
    public Map<TranslatorKey, Collection<IMDMessageTranslator<OfHeader, DataObject>>> getTranslatorMapping() {
        return this.translatorMapping;
    }

}
