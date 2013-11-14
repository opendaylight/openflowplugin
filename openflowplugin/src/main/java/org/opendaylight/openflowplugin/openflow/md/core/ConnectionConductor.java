/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core;

import java.util.List;
import java.util.concurrent.Future;

import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowplugin.openflow.md.core.session.SessionContext;
import org.opendaylight.openflowplugin.openflow.md.queue.QueueKeeper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.binding.DataObject;

import com.google.common.collect.Lists;


/**
 * @author mirehak
 */
public interface ConnectionConductor {

    /** distinguished connection states */
    public enum CONDUCTOR_STATE {
        /** initial phase of talking to switch */
        HANDSHAKING,
        /** standard phase - interacting with switch */
        WORKING,
        /** connection is idle, waiting for echo reply from switch */
        TIMEOUTING,
        /** talking to switch is over - resting in pieces */
        RIP
    }

    /** supported version ordered by height (highest version is at the beginning) */
    public static final List<Short> versionOrder = Lists.newArrayList((short) 0x04, (short) 0x01);

    /**
     * initialize wiring around {@link #connectionAdapter}
     */
    public void init();

    /**
     * @return the negotiated version
     */
    public Short getVersion();

    /**
     * @return the state of conductor
     */
    public CONDUCTOR_STATE getConductorState();

    /**
     * @param conductorState
     */
    public void setConductorState(CONDUCTOR_STATE conductorState);

    /**
     * terminates owned connection
     * @return future result of disconnect action
     */
    public Future<Boolean> disconnect();

    /**
     * assign corresponding {@link SessionContext} to this conductor (to handle disconnect caused by switch)
     * @param context
     */
    public void setSessionContext(SessionContext context);

    /**
     * assign corresponding {@link SwitchConnectionDistinguisher} to this conductor
     * to handle disconnect caused by switch. This involves auxiliary conductors only.
     * @param auxiliaryKey
     */
    public void setConnectionCookie(SwitchConnectionDistinguisher auxiliaryKey);

    /**
     * @return the sessionContext
     */
    public SessionContext getSessionContext();

    /**
     * @return the auxiliaryKey (null if this is a primary connection)
     */
    public SwitchConnectionDistinguisher getAuxiliaryKey();

    /**
     * @return the connectionAdapter
     */
    public ConnectionAdapter getConnectionAdapter();

    /**
     * assign global queueKeeper
     * @param queueKeeper
     */
    void setQueueKeeper(QueueKeeper<OfHeader, DataObject> queueKeeper);

    /**
     * @param errorHandler for internal exception handling
     */
    void setErrorHandler(ErrorHandler errorHandler);

}
