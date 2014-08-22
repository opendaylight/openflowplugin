/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.impl;

import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.msg.*;
import org.opendaylight.of.mockswitch.MockOpenflowSwitch;
import org.opendaylight.util.ByteUtils;
import org.opendaylight.util.ThroughputTracker;
import org.opendaylight.util.net.BigPortNumber;

import java.io.IOException;
import java.util.concurrent.*;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.opendaylight.util.junit.TestTools.random;
import static org.opendaylight.util.junit.TestTools.tweakBytes;

/**
 * A mock switch implementation tailored to send lots of
 * packet-in messages in short order.
 *
 * @author Thomas Vachuska
 * @author Simon Hunt
 */
public class SpeedSwitch extends MockOpenflowSwitch {
    private static final ClassLoader CL = SpeedSwitch.class.getClassLoader();
    private static final BigPortNumber IN_PORT = BigPortNumber.valueOf(3);

    private static final long PUMP_MAX_WAIT_SECS = 10;  // seconds

    private Executor exec;

    // Stuff to throttle
    private boolean ackRequired = true;
    private Semaphore semaphore;
    private int batchCounter;
    private int batchSize;
    private int received = 0;

    private CountDownLatch pumpLatch;
    private ThroughputTracker tt;

    private final byte[] pktData;

    private OfmMutablePacketIn pktIn;

    /** Constructs a mock switch with the ability to send packet-in messages
     * containing the payload specified.
     *
     * @param dpid the datapath ID
     * @param defPath the switch definition file
     * @param pktPath the packet hex file
     * @param exec executor to use for pumping messages
     * @throws IOException if there was an issue reading the packet data
     */
    public SpeedSwitch(DataPathId dpid, String defPath, String pktPath,
                       Executor exec)
            throws IOException {
        super(defPath, false);
        this.dpid = dpid;
        this.exec = exec;
        pktData = ByteUtils.slurpBytesFromHexFile(pktPath, CL);
    }

    private OfmPacketIn createMsg() {
        // Patch pktData with unique dst (1st 6 bytes) and src (2nd 6 bytes)
        // mac addresses.
        tweakBytes(random(), pktData, 4, 0, 12);
        pktIn = (OfmMutablePacketIn) MessageFactory.create(negotiated,
                MessageType.PACKET_IN, PacketInReason.ACTION);
        pktIn.data(pktData).inPort(IN_PORT);
        return (OfmPacketIn) pktIn.toImmutable();
    }

    /** Sets up batching parameters, so we regulate sending of messages.
     *
     * @param semaphore the semaphore to use
     * @param batchSize the number of msgs per permit
     * @param ackRequired true if packet-out messages are required
     */
    void setBatchParams(Semaphore semaphore, int batchSize, boolean ackRequired) {
        this.semaphore = semaphore;
        this.batchSize = batchSize;
        this.ackRequired = ackRequired;
    }
    
    /**
     * Instructs the switch to send 'n' packets as fast as possible, to the
     * controller.
     *
     * @param n the number of packets to send
     * @param tt optional throughput tracker to measure throughput
     * @return the runnable future used to pump messages
     */
    public RunnableFuture<DataPathId> pumpPackets(int n, ThroughputTracker tt) {
        this.tt = tt;
        RunnableFuture<DataPathId> rf = sender(n);
        exec.execute(rf);
        return rf;
    }

    /**
     * Waits for all messages to finish pumping
     */
    public void waitForPump() {
        if (ackRequired && pumpLatch != null) {
            try {
                pumpLatch.await(PUMP_MAX_WAIT_SECS, SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Sets up the pump latch to expect the given number of packet-outs
     * 
     * @param n expected number of packet-outs
     */
    public void setUpPumpLatch(int n) {
        pumpLatch = new CountDownLatch(n);
    }

    /**
     * Get a runnable capable of sending the given number of packet-ins.
     *
     * @param n number of packet-in messages to send
     * @return the runnable future used to pump messages
     */
    protected RunnableFuture<DataPathId> sender(final int n) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                int msgsToSend = n;
                setUpPumpLatch(n);
                while (msgsToSend > 0) {
                    if (batchCounter == 0)
                        acquirePermit();
                    send(createMsg());
                    msgsToSend--;
                    batchCounter--;
                }
            }
        };
        return new FutureTask<DataPathId>(r, dpid);
    }

    private void acquirePermit() {
        if (ackRequired && semaphore != null) {
            try {
                semaphore.acquire();   // blocks (if out of permits)
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else if (!ackRequired) {
            // Count throughput using outbound rate when not expecting acks
            tt.add(batchSize);
        }
        batchCounter = batchSize;
    }

    @Override
    protected void msgRx(OpenflowMessage msg) {
        switch (msg.getType()) {
            case PACKET_OUT:
                received++;
                if (received == batchSize) {
                    if (tt != null)
                        tt.add(batchSize);
                    received = 0;
                    if (ackRequired && semaphore != null)
                        semaphore.release();
                }
                pumpLatch.countDown();
                break;
            default:
                break;
        }
    }

}
