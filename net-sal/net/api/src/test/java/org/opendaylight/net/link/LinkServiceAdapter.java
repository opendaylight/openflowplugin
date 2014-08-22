/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.link;

import org.opendaylight.net.model.ConnectionPoint;
import org.opendaylight.net.model.DeviceId;
import org.opendaylight.net.model.Link;

import java.util.Iterator;
import java.util.Set;

/**
 * An adapter for the {@link org.opendaylight.net.link.LinkService} API, provided specifically for
 * unit tests and implementers to use, to insulate from changes in the API.
 *
 * @author Marjorie Krueger
 */
public class LinkServiceAdapter implements LinkService{
    @Override public Iterator<Link> getLinks() { return null; }
    @Override public Set<Link> getLinks(DeviceId deviceId) { return null; }
    @Override public Set<Link> getLinks(DeviceId deviceA, DeviceId deviceB) { return null; }
    @Override public Set<Link> getLinksFrom(DeviceId deviceId) { return null; }
    @Override public Set<Link> getLinksTo(DeviceId deviceId) { return null; }
    @Override public Set<Link> getLinks(ConnectionPoint cp) { return null; }
    @Override public Set<Link> getLinksFrom(ConnectionPoint cp) { return null; }
    @Override public Set<Link> getLinksTo(ConnectionPoint cp) { return null; }
    @Override public void addListener(LinkListener listener) { }
    @Override public void removeListener(LinkListener listener) { } @Override public Set<LinkListener> getListeners() { return null; }
}
