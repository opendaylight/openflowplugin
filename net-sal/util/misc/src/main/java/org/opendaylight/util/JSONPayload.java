/*
 * (c) Copyright 2011 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

/**
 * Classes implementing this interface are declaring that they can export
 * a JSON representation of their data.
 *
 * @author Simon Hunt
 */
public interface JSONPayload {

    /**
     * Returns the payload as a JSON representation.
     *
     * @return the payload as a JSON formatted string
     */
    public String toJSON();
}
