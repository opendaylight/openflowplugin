/*
 * (c) Copyright 2013,2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.impl;

import org.opendaylight.of.lib.OpenflowException;
import org.opendaylight.of.lib.instr.ActOutput;
import org.opendaylight.of.lib.msg.ConfigFlag;
import org.opendaylight.of.lib.msg.OfmMutableSetConfig;
import org.opendaylight.of.lib.msg.OfmSetConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;
import java.util.ResourceBundle;
import java.util.Set;

import static org.opendaylight.of.lib.msg.ConfigFlag.FRAG_REASM;
import static org.opendaylight.of.lib.msg.MessageFactory.create;
import static org.opendaylight.of.lib.msg.MessageType.SET_CONFIG;
import static org.opendaylight.util.ResourceUtils.getBundledResource;

/**
 * Encapsulates the logic for sending the intial {@link OfmSetConfig} message
 * to a newly connected datapath.
 * <p>
 *
 * @author Simon Hunt
 * @author Radhika Hegde
 */
class BasicSwitchConfig {

    private static final ResourceBundle RES = 
            getBundledResource(BasicSwitchConfig.class, "basicSwitchConfig");
    private static final String E_FAILED_SEND = RES.getString("e_failed_send");

    private static final Set<ConfigFlag> SC_FLAGS = EnumSet.of(FRAG_REASM);

    private static final Logger LOG = LoggerFactory.getLogger(OpenflowController.class);

    /** 
     * Formulates an appropriate <em>SetConfig</em> message and sends it to 
     * the datapath represented by the given connection.
     *
     * @param controller the controller 
     * @param conn the datapath connection
     */
    static void sendConfig(OpenflowController controller,
                                  OpenflowConnection conn) {
        OfmMutableSetConfig msg = (OfmMutableSetConfig)
                create(conn.negotiated, SET_CONFIG);
        
        // We are not yet ready for dealing with IP fragments in the controller.
        // Request switch to reassemble them before checking for flow
        // rules in the table pipeline, and then pass it on to the controller
        // for action.
        msg.setConfigFlags(SC_FLAGS);
        
        // Our equivalent of OFPCML_NO_BUFFER; Page 57 of 1.3.1 specification.
        // For 1.0 the vendor documentation should specify if buffering is
        // supported or not. ProVision switches don't support.
        msg.setMissSendLength(ActOutput.CONTROLLER_NO_BUFFER);
        
        try {
            controller.send(msg.toImmutable(), conn);
        } catch (OpenflowException e) {
            LOG.error(E_FAILED_SEND, conn.dpid, conn.auxId, e);
        }
    }
}
