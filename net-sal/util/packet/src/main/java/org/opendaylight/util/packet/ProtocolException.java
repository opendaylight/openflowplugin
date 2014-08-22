/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.packet;

import static org.opendaylight.util.StringUtils.EMPTY;
import static org.opendaylight.util.StringUtils.EOL;


/**
 * Exception thrown when a protocol's data violates some constraint or there
 * is a encoder error (i.e. buffer overrun).
 */
public class ProtocolException extends RuntimeException {

    private static final long serialVersionUID = -6260716621121862340L;

    private PacketReader r;
    private Protocol p;
    private Packet pkt;
    
    /**
     * Constructs a new runtime exception with the specified detail message.
     * 
     * @param m the detail message
     */
    public ProtocolException(String m) {
        super(m);
    }
    
    /**
     * Constructs a new runtime exception for the specified protocol.
     * 
     * @param e original exception
     * @param p the protocol associated with this exception
     */
    public ProtocolException(Exception e, Protocol p) {
        super(e);
        this.p = p;
    }    
    
    /**
     * Constructs a new runtime exception for the specified protocol.
     * 
     * @param m the detail message
     * @param p the protocol associated with this exception
     * @param r the reader associated with this exception
     */
    public ProtocolException(String m, Protocol p, PacketReader r) {
        super(m);
        this.r = r;
        this.p = p;
    }
    
    /**
     * Constructs a new runtime exception for the specified protocol.
     * 
     * @param e original exception
     * @param p the protocol associated with this exception
     * @param r the reader associated with this exception
     */
    public ProtocolException(Exception e, Protocol p, PacketReader r) {
        super(e);
        this.r = r;
        this.p = p;
    }

    /**
     * Constructs a new runtime exception for the specified packet and protocol.

     * @param e original protocol exception
     * @param pkt the packet associated with this exception
     */
    public ProtocolException(ProtocolException e, Packet pkt) {
        super(e);
        this.pkt = pkt;
        this.p = e.protocol();
        this.r = e.reader();
    }
    
    /**
     * Returns the packet reader associated with this exception or null.
     * 
     * @return the reader associated with this exception
     */
    public PacketReader reader() {
        return r;
    }    
    
    /**
     * Returns the protocol associated with this exception or null.
     * 
     * @return the protocol associated with this exception
     */
    public Protocol protocol() {
        return p;
    }

    /**
     * Returns the packet associated with this exception or null.
     * 
     * @return the packet associated with this exception
     */
    public Packet packet() {
        return pkt;
    }
    
    /**
     * Returns the root cause of this protocol exception.
     * 
     * @return the root cause of this protocol exception
     */
    public Throwable rootCause() {
        Throwable rc = this;
        while (rc.getCause() != null)
            rc = rc.getCause();
        return rc;
    }

    /**
     * Returns the decode debug string associated with this exception or "".
     * 
     * @return the decode debug string
     */
    public String decodeDebugString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Reader: ").append(r)
            .append(", Failed parsing: " )
            .append((p != null) ? p.toDebugString() : EMPTY)
            .append(EOL)
            .append((pkt != null) ? pkt.toDebugString() : EMPTY);
        return sb.toString();
    }    
}
