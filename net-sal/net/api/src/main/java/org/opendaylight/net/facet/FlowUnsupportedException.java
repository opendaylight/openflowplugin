/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.facet;

/**
 * An exception thrown when the device does not support the given flow.
 *
 */
public class FlowUnsupportedException extends RuntimeException {

    /**
     * TODO?
     */
    private static final long serialVersionUID = 1678179502318371082L;

    /**
     * Constructs an exception with the specified message.
     *
     * @param msg the message describing the specific nature of the error
     */
    public FlowUnsupportedException(String msg) {
        super(msg);
    }

    /**
     * Constructs an exception with the specified message and cause.
     *
     * @param msg  the message describing the specific nature of the error
     * @param cause the underlying cause of this error
     */
    public FlowUnsupportedException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
