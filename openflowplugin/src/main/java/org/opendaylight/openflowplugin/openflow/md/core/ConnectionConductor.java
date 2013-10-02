/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core;

import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;

/**
 * @author mirehak
 */
/**
 * @author mirehak
 *
 */
public interface ConnectionConductor {

    /** distinguished connection states */
    public enum CONDUCTOR_STATE {
        /** initial phase of talking to switch */
        HANDSHAKING,
        /** standard phase - interacting with switch */
        WORKING,
        /** talking to switch is over - resting in pieces */
        RIP
    }

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
     * @return the switch features
     */
    public GetFeaturesOutput getFeatures();
}
