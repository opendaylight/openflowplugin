/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.host;


/**
 * Entity capable of receiving asynchronous notifications about changes in
 * end-station information model.
 *
 * @author Thomas Vachuska
 * @author Shaun Wackerly
 * @author Vikram Bobade
 */
public interface HostListener {
    
    /**
     * Receives a notification about a change in end-station information model.
     * 
     * @param event the node event
     */
    void event(HostEvent event);

}
