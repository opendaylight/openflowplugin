/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.impl;

import org.junit.Test;
import org.opendaylight.of.controller.pkt.MessageContext;
import org.opendaylight.of.controller.pkt.SequencedPacketAdapter;

import java.io.BufferedReader;
import java.io.IOException;

import static org.opendaylight.of.controller.pkt.SequencedPacketListenerRole.*;
import static org.opendaylight.util.ProcessUtils.execute;


/**
 * Unit tests to verify speed of processing of the OpenFlow (core) controller
 * using remotely located mock switches.
 *
 * @author Thomas Vachuska
 */
public class ControllerManagerDistributedSpeedTest 
                    extends OpenflowControllerDistributedSpeedTest {

    /** A packet listener. */
    private static class PacketHandler extends SequencedPacketAdapter {
        private boolean handle = false;

        @Override
        public void event(MessageContext context) {
            if (handle) {
                context.packetOut().addAction(FLOOD);
                context.packetOut().send();
            }
        }
    }

    // Create 3 advisors, 3 directors and 4 observers
    private void createPacketHandlers() {
        PacketHandler spl = null;
        for (int i = 0; i < 3; i++)
            cs.addPacketListener(spl = new PacketHandler(), ADVISOR, 5+i);
        for (int i = 0; i < 3; i++)
            cs.addPacketListener(spl = new PacketHandler(), DIRECTOR, 5+i);
        spl.handle = true; // last director will handle messages
        for (int i = 0; i < 4; i++)
            cs.addPacketListener(spl = new PacketHandler(), OBSERVER, 5+i);
    }


    @Override @Test
    public void launch() throws IOException {
        cmgr = new ControllerManager(DEFAULT_CTRL_CFG, new AlertLogger(),
                PH_SINK, FM_ADV, new MockRoleAdvisor(),
                new MockEventDispatcher());
        cs = cmgr;
        cmgr.startIOProcessing();
        lmgr = cmgr.getListenerManager();

        createPacketHandlers();

        // Launch the remote switch bank and wait...
        BufferedReader br = execute(new String[] {
                DST_COMMAND, target, "launch", ctlAddress, 
                switchCount, packetCount, batchPermits, batchSize, "pio"
        }, System.getProperty("user.dir"));

        String line;
        while ((line = br.readLine()) != null)
                System.out.println(line);
    }

}
