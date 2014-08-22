/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.link;

/**
 * Entity capable of receiving asynchronous notifications about changes in
 * infrastructure link information model.
 *
 * @author Thomas Vachuska
 * @author Uyen Chau
 * @author Simon Hunt
 */
public interface LinkListener {

    /**
     * Receives a notification about a change in link information model.
     *
     * @param event the link event
     */
    void event(LinkEvent event);

}