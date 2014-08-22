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
 * Set of services to allow consumers to query the network information base
 * regarding the available infrastructure link information.
 *
 * @author Thomas Vachuska
 * @author Uyen Chau
 * @author Simon Hunt
 * @author Marjorie Krueger
 */
public interface LinkService {

    /**
     * Returns an iterator over the set of all infrastructure links.
     *
     * @return all links
     */
    Iterator<Link> getLinks();

    /**
     * Returns the set of all links for which the given device is either
     * the source or the destination.
     * <p/>
     * If no such links exist, an empty set is returned.
     *
     * @param deviceId the device ID
     * @return the set of matching links
     */
    Set<Link> getLinks(DeviceId deviceId);

    /**
     * Returns the set of all links between the two given devices.
     * If no such links exist, an empty set is returned.
     *
     * @param deviceA the first device ID
     * @param deviceB the second device ID
     * @return the set of matching links
     */
    Set<Link> getLinks(DeviceId deviceA, DeviceId deviceB);

    /**
     * Returns the set of all links for which this device is the source.
     * If no such links exist, an empty set is returned.
     *
     * @param deviceId the device ID
     * @return the set of matching links
     */
    Set<Link> getLinksFrom(DeviceId deviceId);

    /**
     * Returns the set of all links for which this device is
     * the destination.
     * If no such links exist, an empty set is returned.
     *
     * @param deviceId the device ID
     * @return the set of matching links
     */
    Set<Link> getLinksTo(DeviceId deviceId);

    /**
     * Returns the link(s) that contain the given connection point. If no
     * such links exist, an empty set is returned.
     * <p/>
     * Note that a connection point can appear to be part of more than one
     * link when switches not connected to this controller are in the path.
     *
     * @param cp the connection point to match
     * @return the set of matching links
     */
    Set<Link> getLinks(ConnectionPoint cp);

    /**
     * Returns the set of all links for which this connection point
     * is the source.
     * If no such links exist, an empty set is returned.
     *
     * @param src the source connection point
     * @return the set of matching links
     */
    Set<Link> getLinksFrom(ConnectionPoint src);

    /**
     * Returns the set of all links for which this connection point
     * is the destination.
     * If no such links exist, an empty set is returned.
     *
     * @param dst the source connection point
     * @return the set of matching links
     */
    Set<Link> getLinksTo(ConnectionPoint dst);

    /**
     * Adds the specified link update listener.
     *
     * @param listener the listener to be added
     * @throws NullPointerException if the listener is null
     */
    void addListener(LinkListener listener);

    /**
     * Removes the specified link update listener.
     *
     * @param listener the listener to be removed
     * @throws NullPointerException if the listener is null
     */
    void removeListener(LinkListener listener);

    /**
     * Returns the set of all link update listeners.
     *
     * @return all link listeners
     */
    Set<LinkListener> getListeners();

}
