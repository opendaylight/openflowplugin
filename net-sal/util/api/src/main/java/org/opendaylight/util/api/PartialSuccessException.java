/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.api;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An exception to indicate that an operation has partially succeeded (or
 * failed partially).
 * 
 * @author Liem Nguyen
 */
public class PartialSuccessException extends RuntimeException {

    private static final long serialVersionUID = 7991166940499694810L;

    private static final String E_MSG = "{0} succeeded, {1} failed";

    private List<String> successes;

    private List<Failure> failures;

    /**
     * Constructor.
     * 
     * @param successes list of success messages
     * @param failures list of failure messages
     */
    public PartialSuccessException(List<String> successes,
                                   List<Failure> failures) {
        super(MessageFormat.format(E_MSG, successes, failures));
        this.successes = new ArrayList<String>(successes);
        this.failures = new ArrayList<Failure>(failures);
    }

    /**
     * Get a list of success messages.
     * 
     * @return list of success messages
     */
    public List<String> successes() {
        return Collections.unmodifiableList(successes);
    }

    /**
     * Get a list of failure messages.
     * 
     * @return list of failure messages
     */
    public List<Failure> failures() {
        return Collections.unmodifiableList(failures);
    }

    /**
     * A data object to represent a failure.
     */
    public static class Failure {

        private String msg, cause;
        
        /**
         * Constructor
         * 
         * @param msg failure message
         */
        public Failure(String msg) {
            this(msg, null);
        }

        /**
         * Constructor
         * 
         * @param msg failure message
         * @param cause cause for the failure
         */
        public Failure(String msg, String cause) {
            this.msg = msg;
            this.cause = cause;
        }
        
        public String msg() {
            return msg;
        }
        
        public String cause() {
            return cause;
        }
        
        public boolean hasCause() {
            return cause != null;
        }

        @Override
        public String toString() {
            return hasCause() ? msg + ": " + cause : msg;
        }
    }
}
