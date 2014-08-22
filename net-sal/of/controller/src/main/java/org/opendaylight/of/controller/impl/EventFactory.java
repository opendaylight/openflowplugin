/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.impl;

import org.opendaylight.of.controller.ErrorEvent;

/**
 * Facilitates the creation of events from outside the package.
 *
 * @author Simon Hunt
 */
public class EventFactory {

    /** Creates and returns an error event.
     *
     * @param text the error text message
     * @param cause the cause of the error
     * @param context the context of the error
     * @return the error event
     */
    public static ErrorEvent createErrorEvent(String text, Throwable cause,
                                              Object context) {
        return new ErrorEvt(text, cause, context);
    }
}
