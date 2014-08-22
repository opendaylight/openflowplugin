/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.link;

import org.opendaylight.net.model.Link;
import org.opendaylight.net.model.ModelEvent;

/**
 * Represents an event in the infrastructure link information model.
 *
 * @author Thomas Vachuska
 * @author Uyen Chau
 * @author Simon Hunt
 */
public interface LinkEvent extends ModelEvent<Link, LinkEvent.Type> {

    /** Link event types. */
    enum Type {
        /** Event represents link addition. */
        LINK_ADDED,

        /** Event represents link removal. */
        LINK_REMOVED,

        /** Event represents link update, e.g. change in type. */
        LINK_UPDATED
    }

}
