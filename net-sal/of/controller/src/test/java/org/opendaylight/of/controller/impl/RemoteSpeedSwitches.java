/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.impl;

import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.util.Task;
import org.opendaylight.util.ThroughputTracker;
import org.opendaylight.util.net.IpAddress;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

import static java.lang.System.err;
import static java.lang.System.out;


/**
 * Auxiliary test fixture intended to be deployable and executable from a
 * remote test host as an aid in measuring performance of the controller
 * manager assembly.
 * 
 * @author Thomas Vachuska
 */
public class RemoteSpeedSwitches {
    
    protected static final String IMPL_ROOT = "org/opendaylight/of/controller/impl/";
    protected static final String SW10P12 = IMPL_ROOT + "simple10sw12port.def";
    protected static final String ETH2 = IMPL_ROOT + "eth2-arp-req.hex";

    private final IpAddress controller;
    private final Executor exec;
    private final SpeedSwitch switches[];
    private final int packetCount;
    private final int batchSize;
    private final int batchPermits;
    private boolean ackRequired;
    private boolean ackOnly;
    
    /**
     * Main entry point to launch the switch bank.
     * 
     * @param args command-line arguments; first is expected to be the IP
     *        address of the controller; second the number of switches and
     *        third the number of messages to be sent by each switch
     */
    public static void main(String args[]) {
        IpAddress cip = IpAddress.valueOf(args[0]);
        int switchCount = Integer.parseInt(args[1]);
        int packetCount = Integer.parseInt(args[2]);
        int batchPermits = Integer.parseInt(args[3]);
        int batchSize = Integer.parseInt(args[4]);
        
        boolean ackOnly = args[5].equals("po");
        boolean ackRequired = ackOnly || args[5].equals("pio");
        
        RemoteSpeedSwitches bank = 
                new RemoteSpeedSwitches(cip, switchCount, packetCount,
                                        batchSize, batchPermits, 
                                        ackRequired, ackOnly);
        try {
            out.print("Connecting switches to " + cip + "... ");
            // Create and connect a number of switches
            bank.connectSwitches();

            out.print("Pumping messages... ");

            ThroughputTracker tt = new ThroughputTracker();
            
            if (!ackOnly)
                // Kick off pumping of messages by each of the switches
                for (Future<DataPathId> f : bank.pumpMessages(tt))
                    f.get();
            else
                // If we're doing ack-only, measure throughput separately
                for (SpeedSwitch sw : bank.switches)
                    tt.add(packetCount);
            
            // Wait for all switches to receive all pending packet-outs.
            for (SpeedSwitch sw : bank.switches) 
                sw.waitForPump();

            tt.freeze();
            DecimalFormat fmt = new DecimalFormat("#,##0");
            out.print(fmt.format(tt.throughput()) + " p/s... " + 
                      tt.total() + " packet-ins... " + 
                      switchCount + " switches... ");
            
            out.println("Done!");
            
            if (!ackRequired)
                Task.delay(2000);
            
        } catch (Exception e) {
            err.println("Unable to complete test");
            e.printStackTrace();
            System.exit(1);
            
        } finally {
            // Disconnect all switches
            bank.disconnectSwitches();
            System.exit(0);
        }
    }
    
    
    /**
     * Create a bank of mock switches.
     * 
     * @param cip controller IP address
     * @param count number of switches
     * @param packetCount number of packet per switch
     * @param batchSize how many messages to send in a batch
     * @param batchPermits how many batches can be outstanding
     * @param ackRequired true if packet-out messages are required
     * @param ackOnly true if only packet-out messages are expected
     */
    private RemoteSpeedSwitches(IpAddress cip, int count, int packetCount,
                                int batchSize, int batchPermits, 
                                boolean ackRequired, boolean ackOnly) {
        this.controller = cip;
        this.packetCount = packetCount;
        this.batchSize = batchSize;
        this.batchPermits = batchPermits;
        this.ackRequired = ackRequired;
        this.ackOnly = ackOnly;        
        exec = Executors.newFixedThreadPool(count);
        switches = new SpeedSwitch[count];
    }
    
    /**
     * Create and connect all switches.
     * 
     * @throws IOException if the mock switches cannot be created
     */
    private void connectSwitches() throws IOException {
        // Create all switches first...
        for (byte i = 0; i < switches.length; i++)
            switches[i] = createSwitch(DataPathId.valueOf(new byte[] { 
                    (byte) 222, (byte) 173, (byte) 190, (byte) 239, 
                    (byte) 222, (byte) 173, (byte) 190, (byte) (i + 1)
            }));
        
        // Then wait for all switches to be ready
        for (SpeedSwitch sw : switches)
            sw.waitForHandshake(true);
    }

    /**
     * Disconnects the entire bank of switches.
     */
    private void disconnectSwitches() {
        for (SpeedSwitch s : switches)
            s.deactivate();
    }
    
    /**
     * Creates a new mock switch.
     * 
     * @param dpid data path ID to be given to the new switch
     * @return newly created switch
     * @throws IOException if the mock switches cannot be created
     */
    private SpeedSwitch createSwitch(DataPathId dpid) throws IOException {
        SpeedSwitch sw = new SpeedSwitch(dpid, SW10P12, ETH2, exec);
        
        // Configure the new switch
        sw.getDefn().getCfgBase().setControllerAddress(controller);
        sw.setBatchParams(new Semaphore(batchPermits), batchSize, ackRequired);
        if (ackOnly)
            sw.setUpPumpLatch(packetCount);
        sw.activate();
        
        return sw;
    }
    
    /**
     * Kicks of message pump on each of the switches in the bank.
     * 
     * @param tt optional throughput tracker to measure rates
     * @return set of futures, one for each switch to be used for determining
     *         whether and when each switch is done pumping
     */
    private Set<Future<DataPathId>> pumpMessages(ThroughputTracker tt) {
        Set<Future<DataPathId>> futures = new HashSet<Future<DataPathId>>();
        for (SpeedSwitch sw : switches)
            futures.add(sw.pumpPackets(packetCount, tt));
        return futures;
    }
    
}
