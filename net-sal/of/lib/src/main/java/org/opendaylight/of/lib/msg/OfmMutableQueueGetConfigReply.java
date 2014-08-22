/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.opendaylight.of.lib.InvalidMutableException;
import org.opendaylight.of.lib.Mutable;
import org.opendaylight.util.net.BigPortNumber;

import java.util.ArrayList;

import static org.opendaylight.of.lib.CommonUtils.notMutable;
import static org.opendaylight.of.lib.CommonUtils.notNull;

/**
 * Mutable subclass of {@link OfmQueueGetConfigReply}.
 *
 * @author Scott Simes
 */
public class OfmMutableQueueGetConfigReply extends OfmQueueGetConfigReply
        implements MutableMessage {

    private final Mutable mutt = new Mutable();

    /**
     * Constructs a mutable OpenFlow QUEUE_GET_CONFIG_REPLY message.
     *
     * @param header the message header
     */
    OfmMutableQueueGetConfigReply(Header header) {
        super(header);
        queues = new ArrayList<Queue>();
    }

    @Override
    public void clearXid() {
        mutt.checkWritable(this);
        header.xid = 0;
    }

    @Override
    public OpenflowMessage toImmutable() {
        // Can only do this once
        mutt.invalidate(this);
        OfmQueueGetConfigReply msg = new OfmQueueGetConfigReply(header);
        msg.port = this.port;
        msg.queues = this.queues;
        return msg;
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

    /**
     * Sets the ID of the port that was queried; Since 1.0.
     * <p>
     * Note that in 1.0, the port number is u16.
     *
     * @param port the port number
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws NullPointerException if port is null
     * @throws IllegalArgumentException if the port number is invalid
     */
    public void setPort(BigPortNumber port) {
        mutt.checkWritable(this);
        notNull(port);
        Port.validatePortValue(port, header.version);
        this.port = port;
    }

    /**
     * Adds a queue to the list of queues; since 1.0.
     *
     * @param queue the queue to add
     */
    public void addQueue(Queue queue) {
        notNull(queue);
        notMutable(queue);
        queues.add(queue);
        header.length += queue.length;
    }
}
