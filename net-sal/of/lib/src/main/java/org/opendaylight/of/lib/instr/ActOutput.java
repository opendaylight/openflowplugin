/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.instr;

import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.msg.Port;
import org.opendaylight.util.net.BigPortNumber;

import java.util.HashMap;
import java.util.Map;

/**
 * Flow action {@code OUTPUT}.
 *
 * @author Simon Hunt
 */
public class ActOutput extends Action {

    /** Maximum maxLen value which can be used to request a specific
     * byte length to be sent to the controller.
     */
    public static final int CONTROLLER_MAX = 0xffe5;

    /** Indicates that no buffering should be applied and the whole
     * packet is to be sent to the controller.
     */
    public static final int CONTROLLER_NO_BUFFER = 0xffff;

    BigPortNumber port;
    int maxLen;

    /**
     * Constructs an action.
     *
     * @param pv the protocol version
     * @param header the action header
     */
    ActOutput(ProtocolVersion pv, Header header) {
        super(pv, header);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        int len = sb.length();
        sb.replace(len-1, len, ",port=").append(Port.portNumberToString(port))
                .append(",maxLen=").append(maxLenToString(maxLen)).append("}");
        return sb.toString();
    }

    /** Returns the number of the port through which the packet should be sent.
     *
     * @return the port number
     */
    public BigPortNumber getPort() {
        return port;
    }

    /** Returns the max length value.
     * When the port is {@link org.opendaylight.of.lib.msg.Port#CONTROLLER}, this value
     * indicates the maximum number of bytes to send.
     * A value of zero means no bytes of the packet should be sent.
     * A value of {@link #CONTROLLER_MAX} is the maximum that can be
     * used to request a specific byte length.
     * A value of {@link #CONTROLLER_NO_BUFFER} means that the packet is
     * not buffered and the complete packet is to be sent to the controller.
     *
     * @return the max length value
     */
    public int getMaxLen() {
        return maxLen;
    }

    //==== logical name lookup ====
    private static final Map<Integer,String> MAX_LEN_MAP =
            new HashMap<Integer, String>(2);
    static {
        MAX_LEN_MAP.put(CONTROLLER_MAX, "MAX");
        MAX_LEN_MAP.put(CONTROLLER_NO_BUFFER, "NO_BUFFER");
    }

    /** Returns a string representation of the maxLen value. Reserved
     * values will include the logical name.
     *
     * @param ml the maxLen value
     * @return a string representation
     */
    static String maxLenToString(int ml) {
        StringBuilder sb = new StringBuilder(Integer.toString(ml));
        String name = MAX_LEN_MAP.get(ml);
        if (name != null)
            sb.append("(").append(name).append(")");
        return sb.toString();
    }
}
