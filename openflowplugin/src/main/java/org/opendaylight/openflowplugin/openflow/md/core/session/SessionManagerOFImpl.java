/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.session;

import java.util.concurrent.ConcurrentHashMap;

import org.opendaylight.openflowplugin.openflow.md.core.ConnectionConductor;
import org.opendaylight.openflowplugin.openflow.md.core.SwitchConnectionDestinguisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mirehak
 */
public class SessionManagerOFImpl implements SessionManager {

    private static final Logger LOG = LoggerFactory
            .getLogger(SessionManagerOFImpl.class);
    private static SessionManagerOFImpl instance;
    private ConcurrentHashMap<SwitchConnectionDestinguisher, SessionContext> sessionLot;

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
    public SessionContext getSessionContext(
            SwitchConnectionDestinguisher sessionKey) {
        return sessionLot.get(sessionKey);
    }

    @Override
    public void invalidateSessionContext(SwitchConnectionDestinguisher fullKey) {
        // TODO:: do some invalidating and disconnecting and notifying
    }

    @Override
    public void addSessionContext(SwitchConnectionDestinguisher sessionKey,
            SessionContextOFImpl context) {
        sessionLot.put(sessionKey, context);
        // TODO:: notify listeners
    }

    @Override
    public void invalidateAuxiliary(SwitchConnectionDestinguisher sessionKey,
            SwitchConnectionDestinguisher connectionCookie) {
        SessionContext context = getSessionContext(sessionKey);
        if (context == null) {
            LOG.warn("context for invalidation not found");
        } else {
            ConnectionConductor auxiliaryConductor = context
                    .removeAuxiliaryConductor(connectionCookie);
            if (auxiliaryConductor == null) {
                LOG.warn("auxiliary conductor not found");
            } else {
                // TODO:: disconnect, notify
            }
        }

    }

}
