/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.session;

import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowplugin.openflow.md.core.ConnectionConductor;
import org.opendaylight.openflowplugin.openflow.md.core.SwitchConnectionDistinguisher;

/**
 * @author mirehak
 */
public interface SessionManager {

    /**
     * @param sessionKey
     * @return corresponding conductor, holding {@link ConnectionAdapter} to
     *         primary connection
     */
    public SessionContext getSessionContext(
            SwitchConnectionDistinguisher sessionKey);

    /**
     * disconnect all underlying {@link ConnectionAdapter}s and notify listeners
     *
     * @param sessionKey
     */
    public void invalidateSessionContext(SwitchConnectionDistinguisher sessionKey);

    /**
     * register session context
     *
     * @param sessionKey
     * @param context
     */
    public void addSessionContext(SwitchConnectionDistinguisher sessionKey,
            SessionContext context);

    /**
     * disconnect particular auxiliary {@link ConnectionAdapter}, identified by
     * sessionKey and connectionCookie
     *
     * @param sessionKey
     * @param connectionCookie
     */
    public void invalidateAuxiliary(SwitchConnectionDistinguisher sessionKey,
            SwitchConnectionDistinguisher connectionCookie);

    /**
     * @param connectionConductor
     */
    public void invalidateOnDisconnect(ConnectionConductor connectionConductor);
}
