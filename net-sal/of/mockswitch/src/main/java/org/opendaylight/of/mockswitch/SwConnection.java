/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.mockswitch;

import org.opendaylight.of.common.HandshakeLogic;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.msg.OfmFeaturesReply;
import org.opendaylight.of.lib.msg.OfmHello;

/**
 * Embodies information about a connection to the controller. Used to keep
 * track of the handshake sequence.
 *
 * @author Simon Hunt
 */
class SwConnection {

    private OfmHello helloFromSw;
    private OfmHello helloFromCtrl;
    private OfmFeaturesReply features;


    /** Advance handshake state machine now that we have sent out a HELLO.
     *
     * @param hello the out-bound hello message
     * @param memo the mock-switch context
     */
    public synchronized void outBoundHello(OfmHello hello,
                                           MockOpenflowSwitch.Memo memo) {
        if (helloFromSw != null)
            throw new IllegalStateException("hello already sent");
        helloFromSw = hello;

        if (helloFromCtrl != null)
            negotiate(memo);
    }

    /** Advance handshake state machine now that we have received a HELLO.
     *
     * @param hello the in-bound hello message
     * @param memo the mock-switch context
     */
    public synchronized void inBoundHello(OfmHello hello,
                                          MockOpenflowSwitch.Memo memo) {
        if (helloFromCtrl != null)
            throw new IllegalStateException("hello already received");
        helloFromCtrl = hello;

        if (helloFromSw != null)
            negotiate(memo);
    }

    /** Switch can now determine the Protocol version to speak.
     *
     * @param memo the switch context
     */
    private void negotiate(MockOpenflowSwitch.Memo memo) {
        ProtocolVersion pv = null;
        switch (memo.helloMode()) {
            case DEFAULT:
                pv = doNegotiate();
                break;

            case NOT_10_RETURN_OFM_ERROR:
                if (helloFromCtrl.getVersion() == ProtocolVersion.V_1_0)
                    pv = doNegotiate();
                // else leave pv as null to show we didn't agree
                break;

            case NOT_8_BYTES_NO_RESPONSE:
                // TODO:
                break;
        }
        memo.negotiatedAs(pv);
    }

    private ProtocolVersion doNegotiate() {
        return HandshakeLogic.negotiateVersion(helloFromCtrl, helloFromSw);
    }


    /** Advance handshake state machine now that we have sent the
     * features reply.
     *
     * @param frep out-bound features reply
     * @param memo switch context
     */
    public synchronized void outBoundFeatures(OfmFeaturesReply frep,
                                 MockOpenflowSwitch.Memo memo) {
        if (features != null)
            throw new IllegalStateException("features already sent");
        features = frep;

        memo.handshakeComplete();
    }
}
