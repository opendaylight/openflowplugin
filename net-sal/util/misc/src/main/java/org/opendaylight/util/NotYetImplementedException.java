/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

/**
 * Runtime exception thrown when some expected functionality has not yet
 * been implemented.
 * <p>
 * This exception should be used <em>only</em> during development; there should
 * be <u>no code</u> throwing this exception in a released product!
 * <p>
 * Note that there are no constructors accepting a cause (throwable), since
 * it should be self evident that the cause is the lack of implemented code.
 * Thus {@link #getCause} will always return {@code null}.
 *
 * @author Simon Hunt
 */
public class NotYetImplementedException extends RuntimeException {

    private static final long serialVersionUID = 7954073993904395026L;

    /** Constructs a new runtime exception with null as its detail message. */
    public NotYetImplementedException() {
        super();
    }

    /** Constructs a new runtime exception with the specified detail message.
     *
     * @param message the detail message
     */
    public NotYetImplementedException(String message) {
        super(message);
    }

}
