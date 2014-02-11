/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.it;

import java.util.Stack;

import org.opendaylight.openflowjava.protocol.impl.clients.ClientEvent;
import org.opendaylight.openflowjava.protocol.impl.clients.SendEvent;
import org.opendaylight.openflowjava.protocol.impl.clients.WaitForMessageEvent;
import org.opendaylight.openflowjava.protocol.impl.util.ByteBufUtils;

/**
 * provisioning of most common scenarios used by testing of integration between OFLibrary, OFPlugin and MD-SAL 
 */
public abstract class ScenarioFactory {
    
    /** version bitmap hex-string containing version 1.3 */
    public static final String VERSION_BITMAP_13 = "00 01 00 08 00 00 00 10";
    /** version bitmap hex-string containing versions: 1.0 + 1.3 */
    public static final String VERSION_BITMAP_10_13 = "00 01 00 08 00 00 00 12";

    /**
     * Creates stack with handshake needed messages.
     * <ol> XID of messages:
     *   <li> hello sent - 00000001
     *   <li> hello waiting - 00000015
     *   <li> featuresrequest waiting - 00000002
     *   <li> featuresreply sent - 00000002
     * </ol>
     * @param switchVersionBitmap
     * @param auxId
     * @param pluginVersionBitmap
     * @return stack filled with Handshake messages
     */
    public static Stack<ClientEvent> createHandshakeScenarioVBM(
            String switchVersionBitmap, short auxId, String pluginVersionBitmap) {
        Stack<ClientEvent> stack = new Stack<>();
        
        stack.add(0, new SendEvent(ByteBufUtils.hexStringToBytes("04 00 00 10 00 00 00 01 " 
                + switchVersionBitmap)));
        stack.add(0, new WaitForMessageEvent(ByteBufUtils.hexStringToBytes("04 00 00 10 00 00 00 15 "
                + pluginVersionBitmap)));
        stack.add(0, new WaitForMessageEvent(ByteBufUtils.hexStringToBytes("04 05 00 08 00 00 00 02")));
        stack.add(0, new SendEvent(ByteBufUtils.hexStringToBytes(
                "04 06 00 20 00 00 00 02 "
                + "00 01 02 03 04 05 06 07 "
                + "00 01 02 03 01 "
                + Integer.toHexString(auxId)
                + " 00 00 00 01 02 03 00 01 02 03")));
        return stack;
    }

    /**
     * @param auxId
     * @param pluginVersionBitmap 
     * @return handshake scenario without switch version bitmap
     */
    public static Stack<ClientEvent> createHandshakeScenario(short auxId, String pluginVersionBitmap) {
        Stack<ClientEvent> stack = new Stack<>();
        
        stack.add(0, new SendEvent(ByteBufUtils.hexStringToBytes("04 00 00 08 00 00 00 01")));
        stack.add(0, new WaitForMessageEvent(ByteBufUtils.hexStringToBytes("04 00 00 10 00 00 00 15 "
                + pluginVersionBitmap)));
        stack.add(0, new WaitForMessageEvent(ByteBufUtils.hexStringToBytes("04 05 00 08 00 00 00 02")));
        stack.add(0, new SendEvent(ByteBufUtils.hexStringToBytes(
                "04 06 00 20 00 00 00 02 "
                + "00 01 02 03 04 05 06 07 "
                + "00 01 02 03 01 "
                + Integer.toHexString(auxId)
                + " 00 00 00 01 02 03 00 01 02 03")));
        return stack;
    }
}
