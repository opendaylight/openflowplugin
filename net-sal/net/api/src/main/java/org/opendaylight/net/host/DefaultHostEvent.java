/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.host;

import org.opendaylight.net.model.AbstractModelEvent;
import org.opendaylight.net.model.Host;

/**
 * Default implementation of {@link HostEvent}.
 *
 * @author Shaun Wackerly
 */
public class DefaultHostEvent extends AbstractModelEvent<Host, HostEvent.Type>
        implements HostEvent {

    /**
     * Constructs a node event with the given type and one or more subject nodes.
     *
     * @param type node event type
     * @param host node event subject(s)
     */
    public DefaultHostEvent(Type type, Host host) {
        super(type, host);
    }

}
