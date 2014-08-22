/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib;

/**
 * Exception thrown when an attempt is made to encode an
 * {@link OpenflowStructure OpenFlow structure} which is incomplete.
 * That is, the {@link MutableStructure mutable} instance has not had all
 * the required fields set, before converting it to an immutable instance
 * and attempting to encode it.
 *
 * @author Simon Hunt
 */
public class IncompleteStructureException extends OpenflowException {

    private static final long serialVersionUID = -66135566690494159L;

    /** Constructs a new incomplete-structure exception with null as its detail
     *  message, and no specific cause.
     */
    public IncompleteStructureException() {
        super();
    }

    /** Constructs a new incomplete-structure exception with the specified
     * detail message, and the given cause.
     *
     * @param message the detail message
     * @param cause the underlying cause
     */
    public IncompleteStructureException(String message, Throwable cause) {
        super(message, cause);
    }

    /** Constructs a new incomplete-structure exception with the specified
     * detail message, and no specific cause.
     *
     * @param message the detail message
     */
    public IncompleteStructureException(String message) {
        super(message);
    }

    /** Constructs a new incomplete-structure exception with null as its detail
     *  message, but with the given cause.
     *
     * @param cause the underlying cause
     */
    public IncompleteStructureException(Throwable cause) {
        super(cause);
    }
}
