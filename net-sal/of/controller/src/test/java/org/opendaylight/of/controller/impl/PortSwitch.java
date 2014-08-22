/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.impl;

import junit.framework.Assert;
import org.opendaylight.of.lib.IncompleteStructureException;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.err.ECodeBadRequest;
import org.opendaylight.of.lib.err.ECodePortModFailed;
import org.opendaylight.of.lib.err.ErrorType;
import org.opendaylight.of.lib.mp.MBodyMutablePortStats;
import org.opendaylight.of.lib.mp.MBodyPortStats;
import org.opendaylight.of.lib.mp.MBodyPortStatsRequest;
import org.opendaylight.of.lib.msg.*;
import org.opendaylight.util.net.BigPortNumber;

import java.io.IOException;
import java.util.Set;

import static org.opendaylight.of.lib.CommonUtils.cSize;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.mp.MultipartType.PORT_STATS;
import static org.opendaylight.of.lib.msg.MessageFactory.create;
import static org.opendaylight.of.lib.msg.MessageType.MULTIPART_REPLY;

/**
 * A mock switch tailored for testing the PortStateTracker.
 *
 * @author Simon Hunt
 */
public class PortSwitch extends BasicSwitch {

    private static final String E_EXP = "Expect<";
    private static final String E_EXP2 = ">: ";
    private static final String E_UNEX_MSG = "Unexpected msg from controller: ";

    private static final long[] BASE_STATS = {
            100,    // rx packets
            200,    // tx packets
            1000,   // rx bytes
            2000,   // tx bytes
            17,     // rx dropped
            27,     // tx dropped
            13,     // rx errors
            23,     // tx errors
            14,     // rx frame errors
            15,     // rx over errors
            16,     // rx CRC errors
            33,     // collisions
    };
    private static final long BASE_DUR_S = 500;
    private static final long BASE_DUR_NS = 5005;

    private static final int RX_PKT = 0;
    private static final int TX_PKT = 1;
    private static final int RX_BYTE = 2;
    private static final int TX_BYTE = 3;
    private static final int RX_DROP = 4;
    private static final int TX_DROP = 5;
    private static final int RX_ERR = 6;
    private static final int TX_ERR = 7;
    private static final int RX_FRM_ERR = 8;
    private static final int RX_OVR_ERR = 9;
    private static final int RX_CRC_ERR = 10;
    private static final int COLLISION = 11;


    // Switch response modes
    static enum Mode {
        DEFAULT, EXP_ENABLE, EXP_DISABLE
    }

    // defines the response mode
    private Mode mode;

    // captures a test failure
    private String testFail;

    /**
     * Constructs a mock port-related interactions switch with the given
     * datapath ID, using the specified definition file for configuration.
     *
     * @param dpid    the datapath ID
     * @param defPath the switch definition file
     * @throws IOException if there was an issue reading switch configuration
     */
    public PortSwitch(DataPathId dpid, String defPath) throws IOException {
        super(dpid, defPath);
        mode = Mode.DEFAULT;
    }

    /** Sets the response mode of the switch.
     *
     * @param mode the mode
     */
    public void mode(Mode mode) {
        this.mode = mode;
    }

    /** To be called from the test thread; does a destructive read of the
     * testFail string, failing the test if it is not null.
     */
    public void checkForTestFail() {
        if (testFail != null)
            Assert.fail(testFail);
        testFail = null;
    }

    private int portCount() {
        return getDefn().getCfgFeat().getPortCount();
    }

    private boolean weHaveThatPort(BigPortNumber port) {
        long p = port.toLong();
        return p >= 1 && p <= portCount();
    }

    private void unhandled(OpenflowMessage msg) {
        throw new RuntimeException(E_EXP + mode + E_EXP2 + E_UNEX_MSG + msg);
    }

    @Override
    protected void msgRx(OpenflowMessage msg) {
        switch (msg.getType()) {
            case MULTIPART_REQUEST:
                if (!handleMpRequest((OfmMultipartRequest) msg))
                    unhandled(msg);
                break;
            case PORT_MOD:
                handlePortMod((OfmPortMod) msg);
                break;
            case BARRIER_REQUEST:
                handleBarrier((OfmBarrierRequest) msg);
                break;
        }
    }

    private void handlePortMod(OfmPortMod portMod) {
        if (weHaveThatPort(portMod.getPort())) {
            switch (mode) {
                case EXP_ENABLE:
                    checkPortModContents(portMod, true);
                    break;
                case EXP_DISABLE:
                    checkPortModContents(portMod, false);
                    break;
            }
        } else {
            sendError(ErrorType.PORT_MOD_FAILED, ECodePortModFailed.BAD_PORT,
                    portMod);
        }
    }

    private void checkPortModContents(OfmPortMod portMod, boolean expEnable) {
        // TODO : add check for expected HW address
        Set<PortConfig> cfg = portMod.getConfig();
        Set<PortConfig> mask = portMod.getConfigMask();
        Set<PortFeature> adv = portMod.getAdvertise();
        if (cSize(adv) != 0)
            testFail = "advertise set is not empty";
        if (cSize(mask) != 1 || !mask.contains(PortConfig.PORT_DOWN))
            testFail = "config mask set not {PORT_DOWN}";
        if (expEnable && cSize(cfg) != 0)
                testFail = "config set is not empty";
        if (!expEnable &&
                (cSize(cfg) != 1 || !cfg.contains(PortConfig.PORT_DOWN)))
            testFail = "config set not {PORT_DOWN}";
    }

    private boolean handleMpRequest(OfmMultipartRequest request) {
        switch (request.getMultipartType()) {
            case PORT_STATS:
                return handlePortStats(request);
            case PORT_DESC:
                // placeholder for now
                break;
        }
        return false;
    }

    private boolean handlePortStats(OfmMultipartRequest request) {
        MBodyPortStatsRequest psr = (MBodyPortStatsRequest) request.getBody();
        switch (mode) {
            case DEFAULT:
                sendPortStats(request, psr.getPort());
                return true;
        }
        return false;
    }


    private void sendPortStats(OfmMultipartRequest req, BigPortNumber port) {
        if (port.equals(Port.ANY))
            sendAllPorts(req);
        else
            sendOnePort(req, port);
    }

    private void sendOnePort(OfmMultipartRequest request, BigPortNumber port) {
        final int nPorts = getDefn().getCfgFeat().getPortCount();
        final int pnum = (int) port.toLong();
        if (pnum > nPorts) {
            // NOTE: BAD_PORT only valid in 1.2 and higher
            ECodeBadRequest code = (negotiated == V_1_0)
                    ? ECodeBadRequest.BAD_STAT : ECodeBadRequest.BAD_PORT;
            sendError(ErrorType.BAD_REQUEST, code, request);
            return;
        }

        OfmMutableMultipartReply rep = (OfmMutableMultipartReply)
                create(request, MULTIPART_REPLY, PORT_STATS);
        MBodyPortStats.MutableArray array =
                (MBodyPortStats.MutableArray) rep.getBody();
        try {
            array.addPortStats(createStats(negotiated, pnum));
        } catch (IncompleteStructureException e) {
            throw new IllegalStateException(E_PROG_ERR, e);
        }
        send(rep.toImmutable());
    }

    // build a picture of the ports
    private void sendAllPorts(OfmMultipartRequest request) {
        OfmMutableMultipartReply rep = (OfmMutableMultipartReply)
                create(request, MULTIPART_REPLY, PORT_STATS);
        MBodyPortStats.MutableArray array =
                (MBodyPortStats.MutableArray) rep.getBody();
        for (int pnum = 1; pnum <= portCount(); pnum++)
            try {
                array.addPortStats(createStats(negotiated, pnum));
            } catch (IncompleteStructureException e) {
                throw new IllegalStateException(E_PROG_ERR, e);
            }
        send(rep.toImmutable());
    }

    private MBodyPortStats createStats(ProtocolVersion pv, int pnum) {
        MBodyMutablePortStats ps = new MBodyMutablePortStats(pv);
        ps.port(BigPortNumber.valueOf(pnum))
                .rxPackets(BASE_STATS[RX_PKT] + pnum)
                .txPackets(BASE_STATS[TX_PKT] + pnum)
                .rxBytes(BASE_STATS[RX_BYTE] + pnum)
                .txBytes(BASE_STATS[TX_BYTE] + pnum)
                .rxDropped(BASE_STATS[RX_DROP] + pnum)
                .txDropped(BASE_STATS[TX_DROP] + pnum)
                .rxErrors(BASE_STATS[RX_ERR] + pnum)
                .txErrors(BASE_STATS[TX_ERR] + pnum)
                .rxFrameErr(BASE_STATS[RX_FRM_ERR] + pnum)
                .rxOverErr(BASE_STATS[RX_OVR_ERR] + pnum)
                .rxCrcErr(BASE_STATS[RX_CRC_ERR] + pnum)
                .collisions(BASE_STATS[COLLISION] + pnum);
        if (pv.ge(V_1_3))
            ps.duration(BASE_DUR_S, BASE_DUR_NS);
        return (MBodyPortStats) ps.toImmutable();
    }

}
