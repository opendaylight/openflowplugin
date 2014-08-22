/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.host;

import org.opendaylight.net.model.Host;
import org.opendaylight.net.model.ModelEvent;

/**
 * Represents an event in the end-station host information model.
 *
 * @author Thomas Vachuska
 * @author Shaun Wackerly
 * @author Vikram Bobade
 */
public interface HostEvent extends ModelEvent<Host, HostEvent.Type> {

    /** Host event types. */
    enum Type {
        /** Event represents host addition. */
        HOST_ADDED,

        /** Event represents host removal. */
        HOST_REMOVED,

        /** Event represents host update. */
        HOST_UPDATED,

        /** Event represents host move, e.g. change in the most recent location. */
        HOST_MOVED
    }

}
