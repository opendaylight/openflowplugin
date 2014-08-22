/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.pkt;

/**
 * Represents a hint that names the implementing class of the
 * {@link SequencedPacketListener} that declared to the
 * {@link PacketSequencer} that the <em>Packet-In</em> message was "handled".
 * <p>
 * The type of this hint is {@link HintType#HANDLER}.
 *
 * @author Simon Hunt
 */
public class HandlerHint extends AbstractHint {
    private final Class<? extends SequencedPacketListener> handlerClass;

    /**
     * Constructs the HANDLER hint.
     *
     * @param handlerCls the class of the handler
     */
    protected HandlerHint(Class<? extends SequencedPacketListener> handlerCls) {
        super(HintType.HANDLER.encodedType());
        handlerClass = handlerCls;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        int len = sb.length();
        sb.replace(len-1, len, ",cls=").append(handlerClass.getName())
                .append("}");
        return sb.toString();
    }

    /** Returns the class of the packet listener associated with this hint.
     *
     * @return the class of the "handler"
     */
    public Class<? extends SequencedPacketListener> getHandlerClass() {
        return handlerClass;
    }
}
