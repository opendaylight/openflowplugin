/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.nbio;

/**
 * Abstraction of a message being transferred via {@link MessageBuffer}.
 *
 * @author Thomas Vachuska
 */
public interface Message {

    /**
     * Gets the message length in bytes.
     * 
     * @return number of bytes
     */
    int length();
    
}
