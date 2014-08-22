/*
 * (c) Copyright 2013,2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.pkt.impl;

import org.opendaylight.of.controller.MessageEvent;
import org.opendaylight.of.controller.impl.AbstractSubComponent;
import org.opendaylight.of.controller.pkt.*;
import org.opendaylight.of.lib.OpenflowException;
import org.opendaylight.of.lib.msg.OfmPacketOut;
import org.opendaylight.of.lib.msg.OpenflowMessage;
import org.opendaylight.util.ResourceUtils;
import org.opendaylight.util.packet.Packet;
import org.opendaylight.util.packet.ProtocolException;
import org.opendaylight.util.packet.ProtocolId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.opendaylight.of.controller.impl.EventFactory.createErrorEvent;
import static org.opendaylight.of.controller.pkt.SequencedPacketListenerRole.*;
import static org.opendaylight.of.lib.CommonUtils.notNull;
import static org.opendaylight.util.Log.stackTraceSnippet;

/**
 * Implementation of the PacketSequencer.
 *
 * @author Simon Hunt
 * @author Scott Simes
 * @author Thomas Vachuska
 * @author Frank Wood
 */
public class Sequencer extends AbstractSubComponent implements PacketSequencer,
        PacketSequencerSink {

    private final Logger log = LoggerFactory.getLogger(PacketSequencer.class);

    private static final ResourceBundle RES = ResourceUtils.getBundledResource(
            Sequencer.class, "sequencer");

    static final String E_NEG_ALTITUDE = RES.getString("e_neg_altitude");
    static final String E_ALTITUDE_CLAIMED = RES
            .getString("e_altitude_claimed");

    private static final String E_BROKEN_CODE = RES.getString("e_broken_code");
    private static final String E_SEND_FAIL = RES.getString("e_send_fail");
    private static final String E_SEND_EVT = RES.getString("e_send_evt");
    private static final String E_CTX_FAIL = RES.getString("e_ctx_fail");

    private static final long NO_BITS = 0;
    static final long ALL_PROTOCOLS_MASK = -1;

    // Denotes wanting to hear about all packets regardless of protocol.
    static final Set<ProtocolId> ALL_PROTOCOLS = Collections.emptySet();

    // Our listeners.
    private final Map<SequencedPacketListenerRole, List<BoundListener>> map =
            new TreeMap<>();

    /** Constructs a default sequencer. */
    public Sequencer() {
        // set up our listener lists for each of the three listener roles
        for (SequencedPacketListenerRole role : SequencedPacketListenerRole.values())
            map.put(role, new CopyOnWriteArrayList<BoundListener>());
    }

    @Override
    protected void shutdown() {
        super.shutdown();
    }

    @Override
    public String toString() {
        return "{Sequencer: #Adv=" + count(ADVISOR) + ",#Dir="
                + count(DIRECTOR) + ",#Obs=" + count(OBSERVER) + "}";
    }

    private int count(SequencedPacketListenerRole role) {
        return map.get(role).size();
    }

    // =====================================================================
    // === PacketSequencer implementation

    @Override
    public void addPacketListener(SequencedPacketListener listener,
                                  SequencedPacketListenerRole role, int altitude) {
        addPacketListener(listener, role, altitude, null);
    }

    @Override
    public void addPacketListener(SequencedPacketListener listener,
                                  SequencedPacketListenerRole role, int altitude,
                                  Set<ProtocolId> interest) {
        notNull(listener, role);
        if (altitude < 0)
            throw new IllegalArgumentException(E_NEG_ALTITUDE + altitude);

        Set<ProtocolId> careAbout = (interest == null || interest.size() == 0)
                ? ALL_PROTOCOLS : new HashSet<>(interest);

        BoundListener wrapped =
                new BoundListener(listener, role, altitude, careAbout);

        List<BoundListener> listeners = map.get(role);
        // first, remove this listener if already in the list
        listeners.remove(new BoundListener(listener));
        // note: list is maintained in order of descending altitude
        int index = 0;
        for (BoundListener bl : listeners) {
            if (bl.altitude < altitude)
                break;
            index++;
        }
        // verify that we don't have a duplicate altitude...
        if (index > 0 && listeners.get(index - 1).altitude == altitude)
            throw new IllegalStateException(E_ALTITUDE_CLAIMED + altitude);
        listeners.add(index, wrapped);
    }

    @Override
    public void removePacketListener(SequencedPacketListener listener) {
        notNull(listener);
        BoundListener marker = new BoundListener(listener);
        for (List<BoundListener> list : map.values())
            list.remove(marker);
    }

    @Override
    public List<SplMetric> getSplMetrics() {
        List<SplMetric> metrics = new ArrayList<>();
        for (SequencedPacketListenerRole role : SequencedPacketListenerRole.values())
            for (BoundListener bl : map.get(role))
                metrics.add(new SplMetricSnapshot(bl.metric));
        return metrics;
    }

    // =====================================================================
    // === PacketSequencerSink implementation

    private static final String NO_MSG = "(no msg)";

    @Override
    public void processPacket(MessageEvent event) {
        // Only process the packet-in if we are considered the master.
        if (!roleAdvisor.isMasterFor(event.dpid()))
            return;

        MsgContext context;
        try {
            context = new MsgContext(event);
        } catch (ProtocolException e) {
            OpenflowMessage msg = event.msg();
            String msgDump = (msg == null) ? NO_MSG : msg.toDebugString();
            log.warn(E_CTX_FAIL, msgDump, e.decodeDebugString(),
                    stackTraceSnippet(e));
            throw e;
        }

        Packet pkt = context.decodedPacket();
        long protocolMask = (pkt != null) ? pkt.protocolMask() : NO_BITS;
        boolean prevHandled;

        // iterate across the roles: ADVISOR, DIRECTOR, OBSERVER...
        for (SequencedPacketListenerRole role : SequencedPacketListenerRole.values()) {
            // only directors can affect packet-out
            context.enablePacketOut(role == DIRECTOR);

            // iterate across descending altitudes...
            for (BoundListener bl : map.get(role)) {
                // skip the callback if the listener does not care...
                if (bl.careMask != ALL_PROTOCOLS_MASK &&
                        (bl.careMask & protocolMask) == 0)
                    continue;

                long start = System.nanoTime();
                prevHandled = context.isHandled();
                try {
                    bl.spl.event(context);
                } catch (Exception e) {
                    log.warn(E_BROKEN_CODE, bl, stackTraceSnippet(e));
                }
                long end = System.nanoTime();
                bl.metric.addSample(end - start);

                // detect directive to block the packet and mark who did it
                if (!prevHandled && context.isBlocked()) {
                    context.tagHandler(bl.spl.getClass());
                }

                // detect directive to send the packet-out, mark who did it
                // and emit the packet-out response
                if (!prevHandled && context.isSent()) {
                    context.tagHandler(bl.spl.getClass());
                    try {
                        sendPacketOut(context);
                    } catch (Exception e) {
                        context.latchFailed();
                        log.warn(E_SEND_FAIL, event.dpid(), stackTraceSnippet(e));
                        bl.spl.errorEvent(createErrorEvent(E_SEND_EVT, e, context));
                    }
                }
            }
        }
        if (context.isBlocked() || !context.isSent())
            countDrop(context.getPacketIn().getTotalLen());
    }

    // =====================================================================

    /**
     * Converts the packet-out message to immutable, makes it available on the
     * context, and instructs the controller to send it to the originating
     * datapath.
     *
     * @param context the message context
     * @throws OpenflowException if issues arise sending the packet
     */
    private void sendPacketOut(MsgContext context) throws OpenflowException {
        OfmPacketOut pout =
                (OfmPacketOut) context.getMutablePacketOut().toImmutable();
        context.setCompletedPacketOut(pout);
        MessageEvent ev = context.srcEvent();
        listenerService.send(pout, ev.dpid(), ev.auxId());
    }

    private void countDrop(int byteCount) {
        listenerService.countDrop(byteCount);
    }

    // =====================================================================
    // == Unit Test Support

    int getListenerCount(SequencedPacketListenerRole role) {
        return map.get(role).size();
    }

}
