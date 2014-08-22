/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.impl;

import org.opendaylight.of.common.MessageSink;
import org.opendaylight.of.common.ProgrammableSink;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.msg.OpenflowMessage;
import org.opendaylight.util.net.IpAddress;

import java.util.concurrent.CountDownLatch;

import static org.opendaylight.util.junit.TestTools.EOL;
import static org.opendaylight.util.junit.TestTools.print;

/**
 * Augments the {@link ProgrammableSink} class to output the debug
 * string of each message received.
 * <p>
 * This class also allows {@link CountDownLatch}es to be associated with it,
 * to allow asynchronous coordination with test code:
 * <ul>
 *     <li>
 *         {@link #setDataPathAddedLatch(CountDownLatch)} will count down
 *         for every datapath added.
 *     </li>
 * </ul>
 *
 * in which case {@link CountDownLatch#countDown()} is invoked every time
 * {@link MessageSink#dataPathAdded} is called. This can be used to trigger
 * the latch after <em>N</em> datapaths have connected to the controller.
 *
 * @author Simon Hunt
 */
public class TestSink extends ProgrammableSink {

    private CountDownLatch dpReady;
    private CountDownLatch dpDisc;
    private CountDownLatch msgIn;

    /** Constructs a test sink with no default dpid. */
     public TestSink() {
        this(null);
    }

    /** Constructs a test sink with the specified default dpid.
     *
     * @param defaultDpid dpid to use for convenience methods
     */
    public TestSink(DataPathId defaultDpid) {
        super(defaultDpid);
    }

    /** Sets a countdown latch for {@link MessageSink#dataPathAdded} events.
     *
     * @param latch the countdown latch
     */
    public void setDataPathAddedLatch(CountDownLatch latch) {
        dpReady = latch;
    }

    /** Sets a countdown latch for {@link MessageSink#dataPathRemoved} events.
     *
     * @param latch the countdown latch
     */
    public void setDataPathRemovedLatch(CountDownLatch latch) {
        dpDisc = latch;
    }

    /** Sets a countdown latch for {@link #msgRx} events.
     *
     * @param latch the countdown latch
     */
    public void setMsgRxLatch(CountDownLatch latch) {
        msgIn = latch;
    }

    @Override
    public void msgRx(OpenflowMessage msg, DataPathId dpid, int auxId,
                      ProtocolVersion pv) {
        print("{}[{}] <<SINK>> RX from {},aux={} => {}",
            EOL, System.currentTimeMillis(), dpid, auxId, msg.toDebugString());
        super.msgRx(msg, dpid, auxId, pv);
        if (msgIn != null)
            msgIn.countDown();
    }

    @Override
    public void dataPathAdded(DataPathId dpid, ProtocolVersion negotiated,
                              IpAddress ip) {
        if (dpReady != null)
            dpReady.countDown();
    }

    @Override
    public void dataPathRemoved(DataPathId dpid, ProtocolVersion negotiated,
                                IpAddress ip) {
        if (dpDisc != null)
            dpDisc.countDown();
    }
}
