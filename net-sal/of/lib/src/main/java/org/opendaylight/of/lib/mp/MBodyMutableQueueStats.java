/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.mp;

import org.opendaylight.of.lib.*;
import org.opendaylight.of.lib.dt.QueueId;
import org.opendaylight.of.lib.msg.Port;
import org.opendaylight.util.net.BigPortNumber;

import static org.opendaylight.of.lib.CommonUtils.notNull;
import static org.opendaylight.util.PrimitiveUtils.verifyU32;

/**
 * Mutable subclass of {@link MBodyQueueStats}.
 *
 * @author Shruthy Mohanram
 */
public class MBodyMutableQueueStats extends MBodyQueueStats
         implements MutableStructure {
    
     private final Mutable mutt = new Mutable();

    /**
     * Constructs a mutable multipart body QUEUE stats element.
     * 
     * @param pv the protocol version
     */
    public MBodyMutableQueueStats(ProtocolVersion pv) {
        super(pv);        
        port = Port.ANY;
        queueId = QueueId.ALL;
    }

    @Override
    public boolean writable() {
        return mutt.writable();
    }

    @Override
    public OpenflowStructure toImmutable() {
        // Can only do this once
        mutt.invalidate(this);
        // Transfer the payload to an immutable instance
        MBodyQueueStats qs = new MBodyQueueStats(version);
        qs.port = this.port;
        qs.queueId = this.queueId;
        qs.txBytes = this.txBytes;
        qs.txPackets = this.txPackets;
        qs.txErrors = this.txErrors;       
        qs.durationSec = this.durationSec;
        qs.durationNsec = this.durationNsec;       
        return qs;
    }
    
    @Override
    public String toString() {
        return mutt.tagString(super.toString());
    }
    
    // =========================================SETTERS========
    
    /** Sets the port number; Since 1.0.
     * <p>    
     * Note that in 1.0, port numbers are u16.
     * 
     * @param port the port number
     * @return self, for chaining
     * @throws NullPointerException if port is null
     * @throws IllegalArgumentException if the port number is invalid
     * @throws InvalidMutableException if this instance is no longer writable     
     */    
    public MBodyMutableQueueStats port(BigPortNumber port) {
        mutt.checkWritable(this);        
        notNull(port);
        Port.validatePortValue(port, version);
        this.port = port;
        return this;
    }
        
    /** Sets the queue ID configured for specified port; Since 1.0.
     * 
     * @param queueId the queue ID
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws NullPointerException if queueId is null
     */
    public MBodyMutableQueueStats queueId(QueueId queueId) {
        mutt.checkWritable(this);
        notNull(queueId);
        this.queueId = queueId;
        return this;
    }

    /** Sets the number of transmitted bytes; Since 1.0.
     * 
     * @param txBytes the number of transmitted bytes
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     */
    public MBodyMutableQueueStats txBytes(long txBytes) {
        mutt.checkWritable(this);
        this.txBytes = txBytes;
        return this;
    }

    /** Sets the number of transmitted packets; Since 1.0.
     * 
     * @param txPackets the number of transmitted packets
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     */
    public MBodyMutableQueueStats txPackets(long txPackets) {
        mutt.checkWritable(this);
        this.txPackets = txPackets;
        return this;
    }

    /** Sets the number of error packets; Since 1.0.
     * 
     * @param txErrors the number of packets dropped due to overrun
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     */
    public MBodyMutableQueueStats txErrors(long txErrors) {
        mutt.checkWritable(this);
        this.txErrors = txErrors;
        return this;
    }
    
    /** Sets the time the queue has been installed; Since 1.0.
     * <p>
     * The first parameter is the number of seconds; the second number is
     * the additional number of nanoseconds.
     *
     * @param seconds the number of seconds
     * @param nano the additional number of nanoseconds
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws IllegalArgumentException if either value is not u32
     */
    public MBodyMutableQueueStats duration(long seconds, long nano) {
        mutt.checkWritable(this);
        verifyU32(seconds);
        verifyU32(nano);
        this.durationSec = seconds;
        this.durationNsec = nano;
        return this;
    }
}
