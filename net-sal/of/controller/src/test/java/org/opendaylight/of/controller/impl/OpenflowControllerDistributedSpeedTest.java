/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.impl;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.of.common.MessageSink;
import org.opendaylight.of.controller.AlertSink;
import org.opendaylight.of.lib.OpenflowException;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.instr.ActOutput;
import org.opendaylight.of.lib.instr.Action;
import org.opendaylight.of.lib.instr.ActionType;
import org.opendaylight.of.lib.msg.*;
import org.opendaylight.util.ByteUtils;
import org.opendaylight.util.Task;
import org.opendaylight.util.net.BigPortNumber;
import org.opendaylight.util.net.IpAddress;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assume.assumeTrue;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.instr.ActionFactory.createAction;
import static org.opendaylight.util.NamedThreadFactory.namedThreads;
import static org.opendaylight.util.ProcessUtils.exec;
import static org.opendaylight.util.ProcessUtils.execute;
import static org.opendaylight.util.StringUtils.isEmpty;
import static org.opendaylight.util.junit.TestTools.*;


/**
 * Unit tests to verify speed of processing of the OpenFlow (core) controller
 * using remotely located mock switches.
 *
 * @author Thomas Vachuska
 */
public class OpenflowControllerDistributedSpeedTest extends AbstractControllerTest {

    /**
     * Primed from an environment variable {@code REMOTE_TEST_TARGET}; value
     * is expected to be <code>user@host</code>.
     */
    static String target = System.getenv("REMOTE_TEST_TARGET");
    static String setupEnv = System.getenv("REMOTE_TEST_SETUP");

    // Other, miscellaneous parameters
    static String switchCount = "12";
    static String packetCount = "1000000";
    static String batchPermits = "5";
    static String batchSize = "1000";
    static String ctlAddress = "local";

    private static String mode = "pio";
    private static boolean ackRequired = true;
    private static boolean ackOnly = false;

    static final String DST_COMMAND = "src/test/bin/distributedSpeedSwitches";

    static final Action FLOOD =
            createAction(V_1_0, ActionType.OUTPUT, Port.FLOOD,
                         ActOutput.CONTROLLER_NO_BUFFER);

    private static final BigPortNumber IN_PORT = BigPortNumber.valueOf(3);

    private static final ExecutorService exec =
            Executors.newCachedThreadPool(namedThreads("AckPump"));

    /**
     * Sets the operation mode: packet-in-out, packet-in or packet-out only.
     *
     * @param opMode pio, pi or po
     */
    static void setMode(String opMode) {
        mode = opMode;
        ackOnly = mode.equals("po");
        ackRequired = ackOnly || mode.equals("pio");
    }

    // Predicate indicating that remote switches setup/teardown should occur
    private static boolean doSwitchSetup() {
        return !isEmpty(target) && (isEmpty(setupEnv) || setupEnv.equals("true"));
    }


    @BeforeClass
    public static void classSetUp() {
        // Set-up the remote configuration
        if (doSwitchSetup())
            System.out.print(exec(new String[] { DST_COMMAND, target, "setUp" }));
        setUpLogger();
    }

    @AfterClass
    public static void classTearDown() {
        // Tear-down the remote configuration
        if (doSwitchSetup())
            System.out.print(exec(new String[] { DST_COMMAND, target, "tearDown" }));
        restoreLogger();
    }

    @Before
    public void setUp() {
        assumeTrue(!isEmpty(target) && !isUnderCoverage() && !ignoreSpeedTests());
    }

    @Test
    public void launch() throws IOException {
        PortStateTracker pst = new PortStateTracker(lmgr);
        ReplySink sink = new ReplySink();
        AlertSink as = new AlertLogger();

        // If we're only pumping packet-outs, preempt read idle time-out.
        if (ackOnly)
            OpenflowController.enableIdleDetection(false);

        OpenflowController oc = new OpenflowController(DEFAULT_CTRL_CFG, pst,
                                                       sink, as, PH_SINK, PH_CB, FM_ADV);
        oc.initAndStartListening();
        sink.oc = oc;

        // Launch the remote switch bank and wait...
        BufferedReader br = execute(new String[] {
                DST_COMMAND, target, "launch", ctlAddress,
                switchCount, packetCount, batchPermits, batchSize, mode
        }, System.getProperty("user.dir"));

        String line = null;
        while ((line = br.readLine()) != null)
                System.out.println(line);

        oc.shutdown();
    }


    // Message sink used to drive packet outs in response to packet-ins
    private static class ReplySink implements MessageSink {

        OpenflowController oc;

        @Override
        public void dataPathAdded(DataPathId dpid, ProtocolVersion negotiated,
                                  IpAddress ip) {
            if (ackOnly)
                exec.execute(new AckPump(dpid, this));
        }

        @Override
        public void dataPathRemoved(DataPathId dpid, ProtocolVersion negotiated,
                                    IpAddress ip) { }

        @Override
        public void dataPathRevoked(DataPathId dpid, ProtocolVersion negotiated,
                                    IpAddress ip) { }

        @Override
        public void msgRx(OpenflowMessage msg, DataPathId dpid, int auxId,
                          ProtocolVersion negotiated) {
            try {
                if (ackRequired && msg.getType() == MessageType.PACKET_IN) {
                    OfmPacketIn pin = (OfmPacketIn) msg;
                    OfmMutablePacketOut pout = (OfmMutablePacketOut)
                            MessageFactory.create(msg, MessageType.PACKET_OUT);
                    pout.bufferId(pin.getBufferId());
                    pout.inPort(pin);
                    pout.addAction(FLOOD);
                    pout.data(pin.getData());
                    oc.send(pout.toImmutable(), dpid, auxId);
                }
            } catch (OpenflowException e) {
                System.err.println("Unable to send due to: " + e.getMessage());
            }
        }

    }

    // Auxiliary runnable used to pump packet-outs for a reverse test
    private static class AckPump implements Runnable {

        private final DataPathId dpid;
        private final MessageSink sink;

        AckPump(DataPathId dpid, MessageSink sink) {
            this.dpid = dpid;
            this.sink = sink;
        }

        @Override
        public void run() {
            try {
                // Prepare a fake packet-in stencil
                byte[] data = ByteUtils.slurpBytesFromHexFile(ETH2, CL);
                OfmMutablePacketIn pktIn = (OfmMutablePacketIn)
                        MessageFactory.create(V_1_0, MessageType.PACKET_IN,
                                              PacketInReason.ACTION);
                pktIn.data(data).inPort(IN_PORT);
                OpenflowMessage msg = pktIn.toImmutable();

                // Now pump messages using the sink
                int n = Integer.parseInt(packetCount);
                int bs = Integer.parseInt(batchPermits) * Integer.parseInt(batchSize);
                for (int i = 0; i < n; i++) {
                    sink.msgRx(msg, dpid, MAIN_ID, V_1_0);
                    if ((i % bs) == 0)
                        Task.delay(50);
                }

            } catch (IOException e) {
                System.err.println("Unable to pump acks due to: " + e.getMessage());
            }
        }
    }

}
