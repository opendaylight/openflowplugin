/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.md.core;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.concurrent.Future;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.SessionContext;
import org.opendaylight.openflowplugin.api.openflow.md.queue.QueueProcessor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.binding.DataObject;


public interface ConnectionConductor {

    /** distinguished connection states. */
    @SuppressWarnings({"checkstyle:abbreviationaswordinname", "checkstyle:typename"})
    enum CONDUCTOR_STATE {
        /** initial phase of talking to switch. */
        HANDSHAKING,
        /** standard phase - interacting with switch. */
        WORKING,
        /** connection is idle, waiting for echo reply from switch. */
        TIMEOUTING,
        /** talking to switch is over - resting in pieces. */
        RIP
    }

    /** supported version ordered by height (highest version is at the beginning). */
    List<Short> VERSION_ORDER = Lists.newArrayList((short) 0x04, (short) 0x01);

    /**
     * initialize wiring around {@link ConnectionAdapter}.
     */
    void init();

    /**
     * return the negotiated version.
     */
    Short getVersion();

    /**
     * return the state of conductor.
     */
    CONDUCTOR_STATE getConductorState();

    /**
     * Setter.
     * @param conductorState state
     */
    void setConductorState(CONDUCTOR_STATE conductorState);

    /**
     * terminates owned connection.
     * @return future result of disconnect action
     */
    Future<Boolean> disconnect();

    /**
     * assign corresponding {@link SessionContext} to this conductor (to handle disconnect caused by switch).
     * @param context session context
     */
    void setSessionContext(SessionContext context);

    /**
     * assign corresponding {@link org.opendaylight.openflowplugin.api.openflow.md.core.SwitchConnectionDistinguisher}
     * to this conductor to handle disconnect caused by switch. This involves auxiliary conductors only.
     * @param auxiliaryKey key
     */
    void setConnectionCookie(SwitchConnectionDistinguisher auxiliaryKey);

    /**
     * return the sessionContext.
     */
    SessionContext getSessionContext();

    /**
     * return the auxiliaryKey (null if this is a primary connection).
     */
    SwitchConnectionDistinguisher getAuxiliaryKey();

    /**
     * return the connectionAdapter.
     */
    ConnectionAdapter getConnectionAdapter();

    /**
     * assign global queueKeeper.
     * @param queueKeeper keeper
     */
    void setQueueProcessor(QueueProcessor<OfHeader, DataObject> queueKeeper);

    /**
     * Setter.
     * @param errorHandler for internal exception handling
     */
    void setErrorHandler(ErrorHandler errorHandler);

    /**
     * Setter.
     * @param conductorId id
     */
    void setId(int conductorId);

}
