/*
 * (c) Copyright 2013,2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.pkt.impl;

import org.opendaylight.of.controller.MessageEvent;
import org.opendaylight.of.controller.pkt.*;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.instr.Action;
import org.opendaylight.of.lib.msg.MessageUtils;
import org.opendaylight.of.lib.msg.OfmMutablePacketOut;
import org.opendaylight.of.lib.msg.OfmPacketIn;
import org.opendaylight.of.lib.msg.OfmPacketOut;
import org.opendaylight.util.packet.Codec;
import org.opendaylight.util.packet.Packet;
import org.opendaylight.util.packet.ProtocolException;
import org.opendaylight.util.packet.ProtocolId;

import java.nio.BufferUnderflowException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.unmodifiableList;
import static org.opendaylight.of.lib.CommonUtils.*;
import static org.opendaylight.of.lib.dt.BufferId.NO_BUFFER;
import static org.opendaylight.of.lib.msg.MessageFactory.createPacketOutFromPacketIn;

/**
 * Provides the context for a given <em>PacketIn</em> message event.
 * <p>
 * This is a private implementation of {@link MessageContext} that the
 * {@link Sequencer} uses to enforce restricted access to the
 * {@link #packetOut() PacketOut} API for {@link SequencedPacketListener}s
 * that register as Directors.
 *
 * @author Simon Hunt
 * @author Scott Simes
 * @author Thomas Vachuska
 * @author Frank Wood
 */
public class MsgContext implements MessageContext {

// ======================================================================
// IMPLEMENTATION NOTE:
// Any changes to this class should prompt corresponding changes to
// org.opendaylight.of.controller.pkt.TestMessageContext which, for OSGi "impl-bundling"
// reasons, cannot simply extend this class.
// ======================================================================

    static final String E_ALREADY_BLOCKED = "PacketIn has been blocked";
    static final String E_ALREADY_SENT = "PacketIn has been sent already";
    static final String E_NOT_DIRECTOR =
            "Operation supported only for DIRECTOR listeners";
    private static final String SPACER = "  ";

    private static final List<ProtocolId> NO_PROTOCOLS = Collections.emptyList();

    private final MessageEvent srcEvent;
    private final Packet decodedPacket;
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

    private boolean packetOutEnabled = false;
    private boolean failedToSend = false;

    /**
     * Constructs the message context for the given <em>PacketIn</em>
     * message event.
     *
     * @param srcEvent the source message event
     */
    MsgContext(MessageEvent srcEvent) {
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
        if (isHandled() && !handlerHintAdded) {
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