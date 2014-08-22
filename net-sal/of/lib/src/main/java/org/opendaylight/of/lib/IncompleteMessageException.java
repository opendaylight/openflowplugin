/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib;

import org.opendaylight.of.lib.msg.MutableMessage;
import org.opendaylight.of.lib.msg.OpenflowMessage;

/**
 * Exception thrown when an attempt is made to encode an
 * {@link OpenflowMessage OpenFlow message} which is incomplete.
 * That is, the {@link MutableMessage mutable} instance has not had all
 * the required fields set, before converting it to an immutable instance
 * and attempting to encode it.
 *
 * @author Simon Hunt
 */
public class IncompleteMessageException extends OpenflowException {

    private static final long serialVersionUID = -1346916101484890791L;

    /** Constructs a new incomplete-message exception with null as its detail
     *  message, and no specific cause.
     */
    public IncompleteMessageException() {
        super();
    }

    /** Constructs a new incomplete-message exception with the specified detail
     *  message, and the given cause.
     *
     * @param message the detail message
     * @param cause the underlying cause
     */
    public IncompleteMessageException(String message, Throwable cause) {
        super(message, cause);
    }

    /** Constructs a new incomplete-message exception with the specified detail
     *  message, and no specific cause.
     *
     * @param message the detail message
     */
    public IncompleteMessageException(String message) {
        super(message);
    }

    /** Constructs a new incomplete-message exception with null as its detail
     *  message, but with the given cause.
     *
     * @param cause the underlying cause
     */
    public IncompleteMessageException(Throwable cause) {
        super(cause);
    }
}
