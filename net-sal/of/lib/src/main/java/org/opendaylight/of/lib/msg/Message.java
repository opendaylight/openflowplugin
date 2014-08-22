/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.lib.msg;

import org.opendaylight.of.lib.Structure;

/**
 * Provides access to the attributes common to all {@link OpenflowMessage}s.
 *
 * @author Simon Hunt
 */
public interface Message extends Structure {

    /** Returns the message type.
     *
     * @return the message type
     */
    MessageType getType();

    /** Returns the transaction id of this message.
     *
     * @return the message transaction id
     */
    long getXid();

    /* Implementation note:
    *   we don't expose the header.length field, since that is an
    *   implementation detail that the consumer should not care about.
    */

}
