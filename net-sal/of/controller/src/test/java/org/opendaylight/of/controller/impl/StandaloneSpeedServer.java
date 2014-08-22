/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.impl;

import org.opendaylight.of.controller.ControllerStats;
import org.opendaylight.of.controller.impl.AbstractControllerTest.MockEventDispatcher;
import org.opendaylight.of.controller.impl.AbstractControllerTest.MockRoleAdvisor;
import org.opendaylight.of.controller.pkt.MessageContext;
import org.opendaylight.of.controller.pkt.SequencedPacketListenerRole;
import org.opendaylight.of.controller.pkt.SequencedPacketAdapter;
import org.opendaylight.of.lib.instr.ActOutput;
import org.opendaylight.of.lib.instr.Action;
import org.opendaylight.of.lib.instr.ActionType;
import org.opendaylight.of.lib.msg.Port;
import org.opendaylight.util.Task;
import org.opendaylight.util.ThroughputTracker;
import org.opendaylight.util.net.IpAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.DecimalFormat;

import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.instr.ActionFactory.createAction;


/**
 * Auxiliary test fixture to measure speed of packet-in decoding & packet-out
 * encoding from/to NBIO message buffers.
 *
 * @author Thomas Vachuska
 */
public class StandaloneSpeedServer {

    private static Logger log = LoggerFactory.getLogger(StandaloneSpeedServer.class);

    static final long TIMEOUT = 500;
    static final int BUFFER_SIZE = 64 * 1024;

    static final boolean SO_NO_DELAY = false;
    static final int SO_SEND_BUFFER_SIZE = 1024 * 1024;
    static final int SO_RCV_BUFFER_SIZE = 1024 * 1024;

    static final DecimalFormat format = new DecimalFormat("#,##0");

    private final ControllerManager controller;

    ThroughputTracker messages;

    /**
     * Main entry point to launch the server.
     *
     * @param args command-line arguments
     * @throws IOException if unable to launch IO loops
     */
    public static void main(String args[]) throws IOException {
        IpAddress ip = args.length > 0 ? IpAddress.valueOf(args[0]) : IpAddress.LOOPBACK_IPv4;
        int wc = args.length > 1 ? Integer.parseInt(args[1]) : 6;

        log.info("Setting up the server with {} workers on {}... ", wc, ip);
        StandaloneSpeedServer ss = new StandaloneSpeedServer(ip, wc);
        ss.start();

        // Wait...
        while (true)
            Task.delay(5000);
    }

    static final Action FLOOD =
            createAction(V_1_0, ActionType.OUTPUT, Port.FLOOD,
                         ActOutput.CONTROLLER_NO_BUFFER);

    private class TestDirector extends SequencedPacketAdapter {
        @Override
        public void event(MessageContext context) {
            context.packetOut().addAction(FLOOD);
            context.packetOut().send();
        }
    }

    /**
     * Creates a speed server.
     *
     * @param ip optional ip of the adapter where to bind
     * @param wc worker count
     * @throws IOException if unable to launch IO loops
     */
    public StandaloneSpeedServer(IpAddress ip, int wc) throws IOException {
        OpenflowController.enableIdleDetection(false);
        controller = new AbstractControllerTest.TestControllerManager(
                new ControllerConfig.Builder().build(), new AlertLogger(),
                new MockRoleAdvisor(), new MockEventDispatcher());
    }

    /** Start the server IO loops and kicks off throughput tracking. */
    public void start() {
        messages = new ThroughputTracker();
        controller.startIOProcessing();
        controller.addPacketListener(new TestDirector(),
                                     SequencedPacketListenerRole.DIRECTOR, 123);
    }

    /** Stop the server IO loops and freezes throughput tracking. */
    public void stop() {
        ControllerStats stats = controller.getStats();
        messages.add(stats.packetInCount());
        messages.freeze();
        controller.shutdown();
    }

    /** Reports on the accumulated throughput trackers. */
    public void report() {
        DecimalFormat f = new DecimalFormat("#,##0");
        log.info("{} messages; {} mps",
                 f.format(messages.total()), f.format(messages.throughput()));
    }

}
