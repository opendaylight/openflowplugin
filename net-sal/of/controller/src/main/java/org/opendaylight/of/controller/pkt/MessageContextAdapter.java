/*
 * (c) Copyright 2013,2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.pkt;

import org.opendaylight.of.controller.MessageEvent;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.msg.OfmPacketIn;
import org.opendaylight.of.lib.msg.OfmPacketOut;
import org.opendaylight.util.packet.Packet;
import org.opendaylight.util.packet.ProtocolId;

import java.util.List;

/**
 * An adapter for the {@link MessageContext} interface. This class can be
 * used as a base for unit tests that wish to provide mocked message contexts.
 *
 * @author Simon Hunt
 */
public class MessageContextAdapter implements MessageContext {
    @Override public MessageEvent srcEvent() { return null; }
    @Override public ProtocolVersion getVersion() { return null; }
    @Override public OfmPacketIn getPacketIn() { return null; }
    @Override public Packet decodedPacket() { return null; }
    @Override public List<ProtocolId> getProtocols() { return null; }
    @Override public PacketOut packetOut() { return null; }
    @Override public OfmPacketOut getCompletedPacketOut() { return null; }
    @Override public MessageContext addHint(Hint hint) { return null; }
    @Override public List<Hint> getHints() { return null; }
    @Override public boolean isHandled() { return false; }
    @Override public boolean isBlocked() { return false; }
    @Override public boolean isSent() { return false; }
    @Override public boolean failedToSend() { return false; }
    @Override public String toDebugString() { return null; }
    @Override public boolean isTestPacket() { return false; }
    @Override public boolean requiresProcessing() { return false; }
}
