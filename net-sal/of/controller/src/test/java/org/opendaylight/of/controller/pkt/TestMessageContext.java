/*
 * (c) Copyright 2013,2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.pkt;

import org.opendaylight.of.controller.MessageEvent;
import org.opendaylight.of.controller.OpenflowEventType;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.instr.Action;
import org.opendaylight.of.lib.msg.*;
import org.opendaylight.util.TimeUtils;
import org.opendaylight.util.packet.Codec;
import org.opendaylight.util.packet.Packet;
import org.opendaylight.util.packet.ProtocolException;
import org.opendaylight.util.packet.ProtocolId;
import org.opendaylight.util.test.FakeTimeUtils;

import java.nio.BufferUnderflowException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.unmodifiableList;
import static org.opendaylight.of.controller.OpenflowEventType.MESSAGE_RX;
import static org.opendaylight.of.lib.CommonUtils.*;
import static org.opendaylight.of.lib.dt.BufferId.NO_BUFFER;
import static org.opendaylight.of.lib.dt.DataPathId.dpid;
import static org.opendaylight.of.lib.msg.MessageFactory.createPacketOutFromPacketIn;

/**
 * A test message context implementation, available to facilitate unit testing
 * implementations of {@link org.opendaylight.of.controller.pkt.SequencedPacketListener} (SPL).
 * <p>
 * Note that this implementation allows access to the
 * {@link #packetOut() PacketOut} API whereas in the production code this will
 * be restricted to only those SPLs that register as a <em>Director</em>.
 * <p>
 * Also note that a constructor is provided that takes an {@link org.opendaylight.of.lib.msg.OfmPacketIn}
 * argument. Internally, this is wrapped in a mock message event. This will
 * hopefully reduce the amount of unnecessary boiler-plate code required to set
 * up the mock context.
 *
 * @author Simon Hunt
 */
public class TestMessageContext implements MessageContext {

    static final String E_ALREADY_BLOCKED = "PacketIn has been blocked";
    static final String E_ALREADY_SENT = "PacketIn has been sent already";
    static final String E_NOT_DIRECTOR =
            "Operation supported only for DIRECTOR listeners";
    private static final String SPACER = "  ";

    private static final List<ProtocolId> NO_PROTOCOLS = Collections.emptyList();

    private final MessageEvent srcEvent;
    private Packet decodedPacket;
    private List<ProtocolId> protocols;
    private OfmMutablePacketOut mutablePacketOut;
    private OfmPacketOut completedPacketOut;
    private PacketOut pktOutApi = new PktOut();

    // Set of hints
    private final List<Hint> hints = new ArrayList<>();

    // Optimized for tracking handler class & diagnostic packets
    private Class<? extends SequencedPacketListener> handlerClass;
    private boolean handlerHintAdded = false;
    private boolean isTest = false;
    private boolean blocked = false;
    private boolean sent = false;

    // ======= NOTE ===================================================
    // to allow testing without registration as Directors, enable API:
    private boolean packetOutEnabled = true;
    // ================================================================
    private boolean failedToSend = false;

    // ================================================================
    // == Mock message event ==
    /** A test datapath ID, used by default. */
    public static final DataPathId DPID = dpid("1/12349876abcd");

    private static final String REMOTE = "remote-id";
    private static final TimeUtils TIME =
            FakeTimeUtils.getInstance(FakeTimeUtils.Advance.MANUAL).timeUtils();

    private static class MockEvent implements MessageEvent {
        private final OfmPacketIn pi;
        private final DataPathId dpid;

        MockEvent(OfmPacketIn pi, DataPathId dpid) {
            this.pi = pi;
            this.dpid = dpid;
        }

        @Override public OpenflowMessage msg() { return pi; }
        @Override public DataPathId dpid() { return dpid; }
        @Override public int auxId() { return 0; }
        @Override public ProtocolVersion negotiated() { return pi.getVersion(); }
        @Override public String remoteId() { return REMOTE; }
        @Override public long ts() { return TIME.currentTimeMillis(); }
        @Override public OpenflowEventType type() { return MESSAGE_RX; }
    }
    // ================================================================

    // helper to wrap the given packet in message in a message event
    private static MessageEvent createMockEvent(OfmPacketIn packetIn,
                                                DataPathId dpid) {
        notNull(packetIn, dpid);
        notMutable(packetIn);
        return new MockEvent(packetIn, dpid);
    }

    /**
     * Constructs the message context for the given <em>PacketIn</em>
     * message, by wrapping the message in a mock event, using the default
     * {@link #DPID datapath ID}.
     *
     * @param packetIn the source message
     */
    public TestMessageContext(OfmPacketIn packetIn) {
        this(createMockEvent(packetIn, DPID));
    }

    /**
     * Constructs the message context for the given <em>PacketIn</em>
     * message, by wrapping the message in a mock event, using the supplied
     * datapath ID.
     *
     * @param packetIn the source message
     * @param dpid the source datapath ID
     */
    public TestMessageContext(OfmPacketIn packetIn, DataPathId dpid) {
        this(createMockEvent(packetIn, dpid));
    }

    /**
     * Replaces the "decoded" packet with the given instance.
     * Allows unit tests to supply their own mock packet data, rather than
     * relying on the decoded packet data from the PacketIn message.
     *
     * @param packet the packet to set
     * @return self, for chaining
     */
    public TestMessageContext setDecodedPacket(Packet packet) {
        decodedPacket = packet;
        return this;
    }


    /**
     * Constructs the message context for the given <em>PacketIn</em>
     * message event.
     *
     * @param srcEvent the source message event
     */
    public TestMessageContext(MessageEvent srcEvent) {
        this.srcEvent = srcEvent;
        OfmPacketIn pktIn = (OfmPacketIn) srcEvent.msg();
        mutablePacketOut = createPacketOutFromPacketIn(pktIn);
        byte[] data = MessageUtils.getPacketBytes(mutablePacketOut);

        // TODO: If there is no packet data, then presumably the packet-in
        // match contains meta data about the packet...
        // TODO: figure out how to determine "interest" from the match

        decodedPacket = decodePacketData(pktIn, data);
    }

    private Packet decodePacketData(OfmPacketIn pktIn, byte[] data) {
        Packet pkt = null;
        if (data != null) {
            try {
                pkt = Codec.decodeEthernet(data);
            } catch (ProtocolException e) {
                if (!NO_BUFFER.equals(pktIn.getBufferId())
                        && e.packet() != null
                        && e.rootCause() instanceof BufferUnderflowException)
                    pkt = e.packet();
                else
                    throw e;
            }
        }
        return pkt;
    }

    /**
     * Enables/disables packet-out API operations.
     *
     * @param enable true to allow packet-out API operations
     */
    void enablePacketOut(boolean enable) {
        this.packetOutEnabled = enable;
    }

    @Override
    public String toString() {
        return "{MsgCtx:" +
                "blocked=" + blocked +
                ",sent=" + sent +
                ",failedToSend=" + failedToSend +
                ",protocols=" + protocols +
                ",pktIn=" + srcEvent.msg() +
                ",#hints=" + cSize(hints) +
                ",#pktOutActions=" + cSize(mutablePacketOut.getActions()) +
                "}";
    }

    @Override
    public String toDebugString() {
        StringBuilder sb = new StringBuilder(toString());
        sb.append(EOLI).append("Message Event: ").append(srcEvent.toString())
                .append(EOLI).append("Blocked: ").append(blocked)
                .append(EOLI).append("Sent: ").append(sent)
                .append(EOLI).append("Failed to Send: ").append(failedToSend)
                .append(EOLI).append("Protocols: ").append(protocols)
                .append(EOLI).append("Decoded Packet: ")
                    .append(decodedPacket.toDebugString())
                .append(EOLI).append("Mutable Packet-Out: ")
                .append(mutablePacketOut.toDebugString(2))
                .append(EOLI).append("Completed Packet-Out: ")
                .append(completedPacketOut == null
                        ? NULL_REP : completedPacketOut.toDebugString(2))
                .append(EOLI).append("Hints: ");
        if (hints.size() == 0)
            sb.append(EOLI).append(SPACER).append(NONE);
        else
            for (Hint h: hints)
                sb.append(EOLI).append(SPACER).append(h);
        return sb.toString();
    }

    @Override
    public MessageEvent srcEvent() {
        return srcEvent;
    }

    @Override
    public ProtocolVersion getVersion() {
        return srcEvent.negotiated();
    }

    @Override
    public OfmPacketIn getPacketIn() {
        return (OfmPacketIn) srcEvent.msg();
    }

    @Override
    public Packet decodedPacket() {
        return decodedPacket;
    }

    @Override
    public List<ProtocolId> getProtocols() {
        if (protocols == null)
            protocols = decodedPacket != null
                    ? decodedPacket.protocolIds() : NO_PROTOCOLS;
        return protocols;
    }

    @Override
    public boolean isHandled() {
        return blocked || sent;
    }

    @Override
    public boolean isBlocked() {
        return blocked;
    }

    @Override
    public boolean isSent() {
        return sent;
    }

    @Override
    public boolean isTestPacket() {
        return isTest;
    }

    @Override
    public boolean requiresProcessing() {
        return !blocked && !sent && !isTest;
    }

    @Override
    public boolean failedToSend() {
        return failedToSend;
    }

    @Override
    public PacketOut packetOut() {
        return pktOutApi;
    }

    @Override
    public OfmPacketOut getCompletedPacketOut() {
        return completedPacketOut;
    }

    @Override
    public MessageContext addHint(Hint hint) {
        notNull(hint);
        if (hint.getType() == HintType.TEST_PACKET)
            isTest = true;
        hints.add(hint);
        return this;
    }

    @Override
    public List<Hint> getHints() {
        // Add the handler hint lazily, if we have not already done so
        if ((blocked || sent) && !handlerHintAdded) {
            handlerHintAdded = true;
            addHint(HintFactory.createHint(HintType.HANDLER, handlerClass));
        }
        return unmodifiableList(hints);
    }

    // =====================================================================
    // === Methods that only the sequencer should be invoking

    /**
     * Called by the sequencer when one of the listeners blocks or sends the
     * packet-out response, to capture their identity.
     *
     * @param handlerClass the implementing class that handled the packet
     */
    void tagHandler(Class<? extends SequencedPacketListener> handlerClass) {
        this.handlerClass = handlerClass;
    }

    /**
     * Called by the sequencer when it is time to make the packet out
     * message immutable and send it on its way.
     *
     * @return the mutable packet-out
     */
    OfmMutablePacketOut getMutablePacketOut() {
        return mutablePacketOut;
    }

    /**
     * Sets the completed packet-out message on the context.
     *
     * @param pout the packet-out message
     */
    void setCompletedPacketOut(OfmPacketOut pout) {
        completedPacketOut = pout;
    }

    /** Latches the failed-to-send flag. */
    void latchFailed() {
        failedToSend = true;
    }

    //======================================================================

    /**
     * Our implementation of the PacketOut API, providing guarded access
     * to the mutable Packet-Out message under construction.
     */
    private class PktOut implements PacketOut {
        @Override
        public void addAction(Action action) {
            validateAccess();
            mutablePacketOut.addAction(action);
        }

        @Override
        public void clearActions() {
            validateAccess();
            mutablePacketOut.clearActions();
        }

        @Override
        public void block() {
            validateAccess();
            blocked = true;
        }

        @Override
        public void send() {
            validateAccess();
            sent = true;
        }
    }

    /**
     * Validates that the packet-out API operations have been enabled and that
     * the message has not been blocked or sent. Otherwise, an exception
     * is thrown.
     *
     * @throws IllegalStateException if the message has been blocked or sent
     */
    private void validateAccess() {
        if (!packetOutEnabled)
            throw new IllegalStateException(E_NOT_DIRECTOR);
        if (blocked)
            throw new IllegalStateException(E_ALREADY_BLOCKED);
        if (sent)
            throw new IllegalStateException(E_ALREADY_SENT);
    }
}