/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.controller.flow;

/**
 * A listener interested in hearing about group-related events.
 *
 * @author Simon Hunt
 */
public interface GroupListener {
    /**
     * This callback is invoked for each group event.
     *
     * @param event the event
     */
    void event(GroupEvent event);
}
