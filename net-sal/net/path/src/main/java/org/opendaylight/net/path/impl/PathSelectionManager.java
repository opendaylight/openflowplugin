/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.path.impl;

import org.apache.felix.scr.annotations.*;
import org.opendaylight.net.host.HostService;
import org.opendaylight.net.model.*;
import org.opendaylight.net.path.PathSelectionService;
import org.opendaylight.net.topology.LinkWeight;
import org.opendaylight.net.topology.TopologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.apache.felix.scr.annotations.ReferenceCardinality.MANDATORY_UNARY;
import static org.apache.felix.scr.annotations.ReferencePolicy.DYNAMIC;
import static org.opendaylight.util.CommonUtils.notNull;

/**
 * Implementation of the end-to-end path selection service.
 *
 * @author Thomas Vachuska
 */
@Component(immediate = true)
@Service
public class PathSelectionManager implements PathSelectionService {

    private final Logger log = LoggerFactory.getLogger(PathSelectionManager.class);

    private static final String MSG_STARTED = "PathSelectionManager started";
    private static final String MSG_STOPPED = "PathSelectionManager stopped";

    @Reference(name = "HostService",
               cardinality = MANDATORY_UNARY, policy = DYNAMIC)
    protected volatile HostService hostService;

    @Reference(name = "TopologyService",
               cardinality = MANDATORY_UNARY, policy = DYNAMIC)
    protected volatile TopologyService topologyService;

    @Activate
    public void activate() {
        log.info(MSG_STARTED);
    }

    @Deactivate
    public void deactivate() {
        log.info(MSG_STOPPED);
    }

    @Override
    public Set<Path> getPaths(Host src, Host dst) {
        return getPaths(src, dst, null);
    }

    @Override
    public Set<Path> getPaths(Host src, Host dst, LinkWeight weight) {
        notNull(src, dst);
        HostLink srcLink = new DefaultHostLink(src, true);
        HostLink dstLink = new DefaultHostLink(dst, false);

        DeviceId srcDeviceId = deviceId(srcLink);
        DeviceId dstDeviceId = deviceId(dstLink);

        // If the src and dst are connected to the same device, produce
        // a set of paths (one) using the exterior links only
        if (srcDeviceId.equals(dstDeviceId))
            return endToEndPaths(srcLink, dstLink);

        // Otherwise consult the topology service to get a set of interior paths
        Set<Path> paths = weight == null ?
                topologyService.getPaths(srcDeviceId, dstDeviceId) :
                topologyService.getPaths(srcDeviceId, dstDeviceId, weight);

        if (paths == null || paths.isEmpty())
            return new HashSet<>();

        // Finally, convert the exterior links and interior paths into end-to-end paths
        return endToEndPaths(srcLink, dstLink, paths);
    }

    @Override
    public boolean isBroadcastAllowed(ConnectionPoint point) {
        return topologyService.isBroadcastAllowed(point);
    }

    // Extracts connecting device id from the specified host link
    private DeviceId deviceId(HostLink link) {
        return (DeviceId) link.connectionPoint().elementId();
    }

    // Produce a set of end-to-end paths from the supplied exterior links
    private Set<Path> endToEndPaths(HostLink srcLink, HostLink dstLink) {
        Set<Path> endToEndPaths = new HashSet<>(1);
        endToEndPaths.add(endToEndPath(srcLink, dstLink));
        return endToEndPaths;
    }

    // Produce an end-to-end path from the supplied exterior links
    private Path endToEndPath(HostLink srcLink, HostLink dstLink) {
        List<Link> links = new ArrayList<>(2);
        links.add(srcLink);
        links.add(dstLink);
        return new DefaultPath(links);
    }

    // Produce a set of end-to-end paths from the supplied exterior links and
    // the set of interior ones
    private Set<Path> endToEndPaths(HostLink srcLink, HostLink dstLink, Set<Path> paths) {
        Set<Path> endToEndPaths = new HashSet<>(paths.size());
        for (Path path : paths)
            endToEndPaths.add(endToEndPath(srcLink, dstLink, path));
        return endToEndPaths;
    }

    // Produce an end-to-end path from the supplied exterior links and the
    // interior path
    private Path endToEndPath(HostLink srcLink, HostLink dstLink, Path path) {
        List<Link> links = new ArrayList<>(path.links().size() + 2);
        links.add(srcLink);
        links.addAll(path.links());
        links.add(dstLink);
        return new DefaultPath(links);
    }

}
