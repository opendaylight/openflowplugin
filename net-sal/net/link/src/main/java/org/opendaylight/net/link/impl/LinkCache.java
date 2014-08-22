/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.link.impl;

import org.opendaylight.net.link.DefaultLinkEvent;
import org.opendaylight.net.link.LinkEvent;
import org.opendaylight.net.link.LinkFilter;
import org.opendaylight.net.model.*;
import org.opendaylight.net.supplier.SupplierId;
import org.opendaylight.util.cache.ReadOnlyIterator;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.opendaylight.net.link.LinkEvent.Type.*;
import static org.opendaylight.util.cache.CacheUtils.addToIndex;
import static org.opendaylight.util.cache.CacheUtils.removeFromIndex;
import static org.opendaylight.util.cache.CacheUtils.safeSet;

/**
 * Auxiliary facility for tracking and searching through link inventory.
 *
 * @author Thomas Vachuska
 * @author Uyen Chau
 */
class LinkCache {

    private static final String E_NOT_DEVICE_ID = "Not a device id";

    // Master inventory of links
    private final Map<LinkKey, DefaultLink> links = new ConcurrentHashMap<>();

    // Sets of links leading to/from a device
    private final Map<DeviceId, Set<Link>> srcLinks = new ConcurrentHashMap<>();
    private final Map<DeviceId, Set<Link>> dstLinks = new ConcurrentHashMap<>();

    /**
     * Returns an iterator over all links in the inventory.
     *
     * @return link iterator
     */
    Iterator<Link> getLinks() {
        synchronized (this) {
            return new ReadOnlyIterator<Link, DefaultLink>(links.values());
        }
    }

    /**
     * Returns links to/from the specified device.
     *
     * @param deviceId device id
     * @return set of links
     */
    Set<Link> getLinks(DeviceId deviceId) {
        synchronized (this) {
            Set<Link> set = new HashSet<>(getLinksFrom(deviceId));
            set.addAll(getLinksTo(deviceId));
            return set;
        }
    }

    /**
     * Returns links between the two devices.
     *
     * @param deviceA one device
     * @param deviceB another device
     * @return set of links
     */
    Set<Link> getLinks(DeviceId deviceA, DeviceId deviceB) {
        synchronized (this) {
            // Set of links between two devices is an intersection of sets of
            // links for each of the devices
            Set<Link> setA = new HashSet<>(getLinks(deviceA));
            Set<Link> setB = new HashSet<>(getLinks(deviceB));
            setA.retainAll(setB);
            return setA;
        }
    }

    /**
     * Returns the set of links leading from the specified device.
     *
     * @param deviceId device id
     * @return set of links leading from a device
     */
    Set<Link> getLinksFrom(DeviceId deviceId) {
        return safeSet(srcLinks.get(deviceId));
    }

    /**
     * Returns the set of links leading to the specified device.
     *
     * @param deviceId device id
     * @return set of links leading to a device
     */
    Set<Link> getLinksTo(DeviceId deviceId) {
        return safeSet(dstLinks.get(deviceId));
    }

    /**
     * Returns links to/from the specified connection point.
     *
     * @param cp connection point
     * @return set of links
     */
    Set<Link> getLinks(ConnectionPoint cp) {
        return pruneLinks(new HashSet<>(getLinks(deviceId(cp))),
                          new LinkPointFilter(cp));
    }

    /**
     * Returns link from the specified connection point.
     *
     * @param src connection point
     * @return set of links
     */
    Set<Link> getLinksFrom(ConnectionPoint src) {
        return pruneLinks(new HashSet<>(getLinksFrom(deviceId(src))),
                          new LinkSourceFilter(src));
    }

    /**
     * Returns link to the specified connection point.
     *
     * @param dst connection point
     * @return set of links
     */
    Set<Link> getLinksTo(ConnectionPoint dst) {
        return pruneLinks(new HashSet<>(getLinksTo(deviceId(dst))),
                          new LinkDestFilter(dst));
    }


    // Prune the set of links of a device to include only those that have
    // the given connection point
    private Set<Link> pruneLinks(Set<Link> set, LinkFilter filter) {
        Iterator<Link> it = set.iterator();
        while (it.hasNext())
            if (!filter.matches(it.next()))
                it.remove();
        return set;
    }

    /**
     * Uses the supplied information to either create or update a link.
     *
     * @param supplierId supplier id
     * @param src        link source
     * @param dst        link dest
     * @param linkInfo   link information
     * @return link added or link updated event or null as appropriate
     */
    LinkEvent createOrUpdateLink(SupplierId supplierId,
                                 ConnectionPoint src, ConnectionPoint dst,
                                 LinkInfo linkInfo) {
        LinkKey key = new LinkKey(src, dst);
        DefaultLink link = links.get(key);
        if (link == null) {
            return createAndStoreLink(supplierId, key, linkInfo);
        } else {
            return updateLink(link, linkInfo);
        }
    }

    // Creates a new link and stores it in the master inventory as well as the
    // secondary lookup structures and returns the resulting event.
    private LinkEvent createAndStoreLink(SupplierId supplierId, LinkKey key, LinkInfo linkInfo) {
        DefaultLink link = new DefaultLink(supplierId, key.src, key.dst, linkInfo.type());
        synchronized (this) {
            links.put(key, link);
            addToIndex(deviceId(link.src()), srcLinks, link);
            addToIndex(deviceId(link.dst()), dstLinks, link);
            return new DefaultLinkEvent(LINK_ADDED, link);
        }
    }

    // Updates the existing link and returns the resulting event, if needed
    private LinkEvent updateLink(DefaultLink link, LinkInfo linkInfo) {
        link.setTimestamp(System.currentTimeMillis());

        // FIXME: Inspect & document relationships between link types and possible type evolution paths.
        // TODO: Possibly rework to make less prone to issues when additional attributes are added to link info.
        if (link.type() == Link.Type.MULTIHOP_LINK &&
                linkInfo.type() == Link.Type.DIRECT_LINK) {
            // If the link is upgraded from multi-hop to direct, update and
            // prepare an update event.
            link.setType(linkInfo.type());
            return new DefaultLinkEvent(LINK_UPDATED, link);
        }

        // Event with null type represents an event that should be suppressed.
        return new NoOpLinkEvent(link);
    }

    /**
     * Extracts device id from the specified connection point or throws
     * an exception if the connection point is not associated with a device
     *
     * @param cp connection point
     * @return extracted device id
     */
    static DeviceId deviceId(ConnectionPoint cp) {
        ElementId id = cp.elementId();
        if (id instanceof DeviceId)
            return (DeviceId) id;
        throw new IllegalArgumentException(E_NOT_DEVICE_ID);
    }

    /**
     * Removes the link between the given connection points.
     *
     * @param src link source
     * @param dst link dest
     * @return remove link event if the link was removed
     */
    LinkEvent removeLink(ConnectionPoint src, ConnectionPoint dst) {
        LinkKey key = new LinkKey(src, dst);
        synchronized (this) {
            DefaultLink link = links.remove(key);
            if (link != null) {
                removeFromIndex(deviceId(link.src()), srcLinks, link);
                removeFromIndex(deviceId(link.dst()), dstLinks, link);
                return new DefaultLinkEvent(LINK_REMOVED, link);
            }
        }
        return null;
    }


    // Mechanism for filtering links based on a connection point
    private class LinkPointFilter implements LinkFilter {
        protected final ConnectionPoint cp;

        LinkPointFilter(ConnectionPoint cp) {
            this.cp = cp;
        }

        @Override
        public boolean matches(Link link) {
            return link.src().equals(cp) || link.dst().equals(cp);
        }
    }

    // Mechanism for filtering links based on source connection point
    private class LinkSourceFilter extends LinkPointFilter {
        LinkSourceFilter(ConnectionPoint cp) {
            super(cp);
        }

        @Override
        public boolean matches(Link link) {
            return link.src().equals(cp);
        }
    }

    // Mechanism for filtering links based on destination connection point
    private class LinkDestFilter extends LinkPointFilter {
        LinkDestFilter(ConnectionPoint cp) {
            super(cp);
        }

        @Override
        public boolean matches(Link link) {
            return link.dst().equals(cp);
        }
    }


}
