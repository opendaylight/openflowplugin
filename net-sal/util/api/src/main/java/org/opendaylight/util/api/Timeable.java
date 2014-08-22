/*
 * (C) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.api;

import java.util.Date;

/**
 * Classes implementing this interface represent an event in time.
 * 
 * @author Fabiel Zuniga
 */
public interface Timeable {

    /**
     * Gets the time the event occurred.
     * 
     * @return the timestamp
     */
    public Date getTimestamp();
}
