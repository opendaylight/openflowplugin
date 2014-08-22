/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.lib.msg;

import org.opendaylight.of.lib.InvalidMutableException;
import org.opendaylight.of.lib.MutableObject;

/**
 * Implemented by OpenFlow message classes that provide mutators for
 * setting the state of the message.
 *
 * @author Simon Hunt
 */
public interface MutableMessage extends MutableObject, Message {

    /**
     * Clears the transaction ID field of this message (sets it to 0).
     * This is provided for unit testing purposes, only.
     *
     * @throws InvalidMutableException if this message is no longer valid
     */
    void clearXid();

    /**
     * Returns an immutable instance of this message. In essence, converts
     * this message to be read-only.
     * <p>
     * It is expected that the reference to this mutable message will be
     * dropped. Note that <em>all</em> method calls invoked on a
     * {@code MutableMessage} after {@code toImmutable()} has been invoked
     * will result in an {@link InvalidMutableException} being thrown.
     *
     * @return an immutable instance of this message.
     * @throws InvalidMutableException if this message is no longer writable
     */
    OpenflowMessage toImmutable();

}
