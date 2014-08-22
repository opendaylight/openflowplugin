/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.mp;

import org.opendaylight.of.lib.*;
import org.opendaylight.of.lib.msg.Port;
import org.opendaylight.util.net.BigPortNumber;

import static org.opendaylight.of.lib.CommonUtils.notNull;
import static org.opendaylight.of.lib.CommonUtils.verMin13;
import static org.opendaylight.util.PrimitiveUtils.verifyU32;

/**
 * Mutable subclass of {@link MBodyPortStats}.
 *
 * @author Pramod Shanbhag
 */
public class MBodyMutablePortStats extends MBodyPortStats 
        implements MutableStructure {

    private final Mutable mutt = new Mutable();

    /**
     * Constructs a mutable multipart body PORT_STATS element.
     * <p>
     *  A valid {@link Port} must be present for this element to be valid.
     *
     * @param pv the protocol version
     */
    public MBodyMutablePortStats(ProtocolVersion pv) {
        super(pv);
    }

    @Override
    public OpenflowStructure toImmutable() {
        // Can only do this once
        mutt.invalidate(this);
        // Transfer the payload to an immutable instance
        MBodyPortStats ps = new MBodyPortStats(version);
        ps.port = this.port;
        ps.rxPackets = this.rxPackets;
        ps.txPackets = this.txPackets;
        ps.rxBytes = this.rxBytes;
        ps.txBytes = this.txBytes;
        ps.rxDropped = this.rxDropped;
        ps.txDropped = this.txDropped;
        ps.rxErrors = this.rxErrors;
        ps.txErrors = this.txErrors;
        ps.rxFrameErr = this.rxFrameErr;
        ps.rxOverErr = this.rxOverErr;
        ps.rxCrcErr = this.rxCrcErr;
        ps.collisions = this.collisions;
        ps.durationSec = this.durationSec;
        ps.durationNsec = this.durationNsec;
        return ps;
    }

    @Override
    public boolean writable() {
        return mutt.writable();
    }

    @Override
    public String toString() {
        return mutt.tagString(super.toString());
    }

    // =====================================================================
    // ==== SETTERS

    /** Sets the port number; Since 1.0.
     * <p>    
     * Note that in 1.0, port numbers are u16.
     * 
     * @param port the port number
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable     
     * @throws NullPointerException if port is null
     * @throws IllegalArgumentException if the port number is invalid
     */    
    public MBodyMutablePortStats port(BigPortNumber port) {
        mutt.checkWritable(this);        
        notNull(port);
        Port.validatePortValue(port, version);
        this.port = port;
        return this;
    }

    /** Sets the number of packets received by this port; Since 1.0.
     *
     * @param rxPackets the number of received packets 
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     */
    public MBodyMutablePortStats rxPackets(long rxPackets) {
        mutt.checkWritable(this);
        this.rxPackets = rxPackets;
        return this;
    }

    /** Sets the number of packets transmitted by this port; Since 1.0.
     *
     * @param txPackets the number of transmitted packets 
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     */
    public MBodyMutablePortStats txPackets(long txPackets) {
        mutt.checkWritable(this);
        this.txPackets = txPackets;
        return this;
    }

    /** Sets the number of bytes received by this port; Since 1.0.
     * 
     * @param rxBytes the number of received bytes
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     */
    public MBodyMutablePortStats rxBytes(long rxBytes) {
        mutt.checkWritable(this);
        this.rxBytes = rxBytes;
        return this;
    }

    /** Sets the number of bytes transmitted by this port; Since 1.0.
     * 
     * @param txBytes the number of transmitted bytes
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     */
    public MBodyMutablePortStats txBytes(long txBytes) {
        mutt.checkWritable(this);
        this.txBytes = txBytes;
        return this;
    }

    /** Sets the number of packets dropped by this port at receiving end;
     *  Since 1.0.
     * 
     * @param rxDropped the number of dropped packets
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     */
    public MBodyMutablePortStats rxDropped(long rxDropped) {
        mutt.checkWritable(this);
        this.rxDropped = rxDropped;
        return this;
    }

    /** Sets the number of packets dropped by this port at transmitting end;
     *  Since 1.0.
     * 
     * @param txDropped the number of dropped packets
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     */
    public MBodyMutablePortStats txDropped(long txDropped) {
        mutt.checkWritable(this);
        this.txDropped = txDropped;
        return this;
    }

    /** Sets the number of error packets received by this port; Since 1.0.
     * 
     * @param rxErrors the number of packets with error 
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     */
    public MBodyMutablePortStats rxErrors(long rxErrors) {
        mutt.checkWritable(this);
        this.rxErrors = rxErrors;
        return this;
    }

    /** Sets the number of transmit error packets; Since 1.0.
     * 
     * @param txErrors the number of packets with error
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     */
    public MBodyMutablePortStats txErrors(long txErrors) {
        mutt.checkWritable(this);
        this.txErrors = txErrors;
        return this;
    }

    /** Sets the number of frame alignment error packets received by 
     * this port; Since 1.0.
     * 
     * @param rxFrameErr the number of packets with frame alignment error 
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     */
    public MBodyMutablePortStats rxFrameErr(long rxFrameErr) {
        mutt.checkWritable(this);
        this.rxFrameErr = rxFrameErr;
        return this;
    }

    /** Sets the number of overrun error packets received by this 
     * port; Since 1.0.
     * 
     * @param rxOverErr the number of packets with overrun error 
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     */
    public MBodyMutablePortStats rxOverErr(long rxOverErr) {
        mutt.checkWritable(this);
        this.rxOverErr = rxOverErr;
        return this;
    }

    /** Sets the number of CRC error packets received by this port; Since 1.0.
     * 
     * @param rxCrcErr the number of packets with CRC error 
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     */
    public MBodyMutablePortStats rxCrcErr(long rxCrcErr) {
        mutt.checkWritable(this);
        this.rxCrcErr = rxCrcErr;
        return this;
    }

    /** Sets the number of collisions; Since 1.0.
     * 
     * @param collisions the number of collisions 
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     */
    public MBodyMutablePortStats collisions(long collisions) {
        mutt.checkWritable(this);
        this.collisions = collisions;
        return this;
    }

    /** Sets the time the group has been alive; Since 1.3.
     * <p>
     * The first parameter is the number of seconds; the second number is
     * the additional number of nanoseconds.
     *
     * @param seconds the number of seconds
     * @param nano the additional number of nanoseconds
     * @return self, for chaining
     * @throws VersionMismatchException if version &lt; 1.3
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws IllegalArgumentException if seconds or nanoSeconds is not u32
     */
    public MBodyMutablePortStats duration(long seconds, long nano) {
        mutt.checkWritable(this);
        verMin13(version);
        verifyU32(seconds);
        verifyU32(nano);
        this.durationSec = seconds;
        this.durationNsec = nano;
        return this;
    }
}
