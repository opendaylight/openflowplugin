/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.model;

import org.opendaylight.util.event.TypedEvent;

/**
 * Represents an event in the network information base.
 *
 * @author Thomas Vachuska
 * @author Uyen Chau
 * @author Simon Hunt
 *
 */
public interface ModelEvent<M extends Model, T extends Enum<?>>
        extends TypedEvent<T> {

    /**
     * Returns the model subject of the event.
     *
     * @return event subject
     */
    M subject();

}
