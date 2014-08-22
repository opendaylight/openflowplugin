/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.link;

import org.opendaylight.net.model.AbstractModelEvent;
import org.opendaylight.net.model.Link;

/**
 * Default implementation of {@link org.opendaylight.net.link.LinkEvent}
 *
 * @author Thomas Vachuska
 */
public class DefaultLinkEvent extends AbstractModelEvent<Link, LinkEvent.Type>
        implements LinkEvent {

    /**
     * Constructs a link event with the given type and link subject.
     *
     * @param type the type of event
     * @param subject the link associated with the event
     */
    public DefaultLinkEvent(Type type, Link subject) {
        super(type, subject);
    }

}
