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

/**
 * Mutable subclass of {@link MBodyQueueStatsRequest}.
 *
 * @author Shruthy Mohanram
 */
public class MBodyMutableQueueStatsRequest extends MBodyQueueStatsRequest
        implements MutableStructure {
    
    private final Mutable mutt = new Mutable();

    /**
     * Constructs a mutable multipart body QUEUE request type.
     * <p>
     * Note that a freshly constructed instance has the following default
     * values:
     * <ul>
     *     <li>port number: {@link Port#ANY}</li>
     *     <li>queue id   : {@link QueueId#ALL}</li>
     * </ul>
     * <p>
     *
     * @param pv the protocol version
     */
    public MBodyMutableQueueStatsRequest(ProtocolVersion pv) {
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
        MBodyQueueStatsRequest req = new MBodyQueueStatsRequest(version);        
        req.port = this.port;
        req.queueId = this.queueId;                
        return req;
    }
    
    @Override
    public String toString() {
        return mutt.tagString(super.toString());
    }
    
    // =====================================================================
    // ==== SETTERS
    
    /** Sets the port number for which statistics are requested; Since 1.0.
     * A value of {@link Port#ANY} indicates no restriction.
     * <p>
     * Note that in 1.0, port numbers are u16.
     *
     * @param port the port
     * @return self, for chaining    
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws NullPointerException if port is null
     * @throws IllegalArgumentException if the port number is invalid
     */    
    public MBodyMutableQueueStatsRequest port(BigPortNumber port) {
        mutt.checkWritable(this);
        notNull(port);
        Port.validatePortValue(port, version);
        this.port = port;
        return this;
    }
    
    /** Sets the ID of the queue to read; Since 1.0.
    * <p>
    * A value of {@link QueueId#ALL} indicates all queues configured
    * at the specified port.
    * 
    * @param queueId the queue ID
    * @return self, for chaining
    * @throws InvalidMutableException if this instance is no longer writable
    * @throws NullPointerException if queueId is null
    */
    public MBodyMutableQueueStatsRequest queueId(QueueId queueId) {
        mutt.checkWritable(this);
        notNull(queueId);
        this.queueId = queueId;
        return this;
    }
}
