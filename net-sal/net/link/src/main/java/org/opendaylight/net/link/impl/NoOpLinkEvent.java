/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.link.impl;

import org.opendaylight.net.link.LinkEvent;
import org.opendaylight.net.model.Link;

/**
 * Representation of a link event that is to be suppressed.
 *
 * @author Thomas Vachuska
 * @author Uyen Chau
 */
class NoOpLinkEvent implements LinkEvent {

    private final Link link;

    NoOpLinkEvent(Link link) { this.link = link; }

    @Override public Link subject() { return link; }
    @Override public long ts() { return 0; }
    @Override public Type type() { return null; }
}
