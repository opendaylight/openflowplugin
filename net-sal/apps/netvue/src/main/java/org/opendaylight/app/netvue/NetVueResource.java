/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.app.netvue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.opendaylight.net.device.DeviceService;
import org.opendaylight.net.host.HostService;
import org.opendaylight.net.link.LinkService;
import org.opendaylight.net.model.*;
import org.opendaylight.net.path.PathSelectionService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.*;

import static org.opendaylight.util.net.IpAddress.ip;

/**
 * Resource for serving data about current state of network topology.
 *
 * @author Thomas Vachuska
 */
@javax.ws.rs.Path("/topo")
public class NetVueResource {

    /**
     * Retrieves the reference to the specified service class implementation.
     *
     * @param serviceClass service class
     * @param <T>          type of service
     * @return service implementation
     */
    static <T> T get(Class<T> serviceClass) {
        BundleContext bc = FrameworkUtil.getBundle(NetVueResource.class).getBundleContext();
        return bc.getService(bc.getServiceReference(serviceClass));
    }

    /**
     * Returns a JSON array of all paths between the specified hosts.
     *
     * @param src source host id
     * @param dst target host id
     * @return JSON array of paths
     */
    @javax.ws.rs.Path("/paths/{src}/{dst}")
    @GET
    @Produces("application/json")
    public Response paths(@PathParam("src") String src, @PathParam("dst") String dst) {
        PathSelectionService pathSelectionService = get(PathSelectionService.class);
        HostService hostService = get(HostService.class);

        ObjectMapper mapper = new ObjectMapper();

        ArrayNode pathsNode = mapper.createArrayNode();
        Host srcHost = hostService.getHost(HostId.valueOf(ip(src), SegmentId.UNKNOWN));
        Host dstHost = hostService.getHost(HostId.valueOf(ip(dst), SegmentId.UNKNOWN));

        if (srcHost != null && dstHost != null) {
            for (Path path : pathSelectionService.getPaths(srcHost, dstHost))
                pathsNode.add(json(mapper, path));
        }

        // Now put the vertexes and edges into a root node and ship them off
        ObjectNode rootNode = mapper.createObjectNode();
        rootNode.put("paths", pathsNode);
        return Response.ok(rootNode.toString()).build();
    }

    /**
     * Returns topology graph as a list of vertexes and edges.
     *
     * @return SON object describing the graph
     */
    @javax.ws.rs.Path("/graph")
    @GET
    @Produces("application/json")
    public Response graph() {
        DeviceService ds = get(DeviceService.class);
        HostService hs = get(HostService.class);

        ObjectMapper mapper = new ObjectMapper();

        // Build all device nodes
        ArrayNode vertexesNode = mapper.createArrayNode();
        Iterator<Device> devices = ds.getDevices();
        while (devices.hasNext()) {
            Device device = devices.next();
            vertexesNode.add(json(mapper, device.id().fingerprint(), 2, device.isOnline()));
        }

        // Now scan all links and count number of them between the same devices
        // using a normalized link key.
        Map<String, LinkRecord> linkRecords = collectLinkRecords();

        // Now build all edge nodes
        ArrayNode edgesNode = mapper.createArrayNode();
        for (LinkRecord lr : linkRecords.values()) {
            edgesNode.add(json(mapper, lr.links.size(), lr.link.src(), lr.link.dst()));
        }

        // Finally append all hosts as vertexes and their edges
        Iterator<Host> hosts = hs.getHosts();
        while (hosts.hasNext()) {
            Host host = hosts.next();
            vertexesNode.add(json(mapper, host.id().ip().toString(), 3, true));
            edgesNode.add(json(mapper, 1, host.ip().toString(),
                               host.location().elementId().fingerprint()));
        }

        // Now put the vertexes and edges into a root node and ship them off
        ObjectNode rootNode = mapper.createObjectNode();
        rootNode.put("vertexes", vertexesNode);
        rootNode.put("edges", edgesNode);
        return Response.ok(rootNode.toString()).build();
    }

    // Scan all links and counts number of them between the same devices
    // using a normalized link key.
    private Map<String, LinkRecord> collectLinkRecords() {
        LinkService ls = get(LinkService.class);
        Map<String, LinkRecord> linkRecords = new HashMap<>();
        Iterator<Link> links = ls.getLinks();
        while (links.hasNext()) {
            Link link = links.next();
            String key = linkKey(link);
            LinkRecord lr = linkRecords.get(key);
            if (lr == null) {
                lr = new LinkRecord(key);
                linkRecords.put(key, lr);
            }
            lr.addLink(link);
        }
        return linkRecords;
    }

    // Produces json array of links for the specified path.
    private ArrayNode json(ObjectMapper mapper, Path path) {
        ArrayNode pathNode = mapper.createArrayNode();
        for (Link link : path.links()) {
            ObjectNode linkNode = mapper.createObjectNode()
                    .put("src", id(link.src()))
                    .put("dst", id(link.dst()));
            pathNode.add(linkNode);
        }
        return pathNode;
    }

    // Returns json node for a graph vertex.
    private ObjectNode json(ObjectMapper mapper, String id, int group, boolean isOnline) {
        return mapper.createObjectNode().put("name", id).put("group", group)
                .put("online", isOnline);
    }

    // Returns json node for a graph edge.
    private ObjectNode json(ObjectMapper mapper, int count,
                            ConnectionPoint src, ConnectionPoint dst) {
        return json(mapper, count, id(src), id(dst));
    }

    // Returns json node for a graph edge.
    private ObjectNode json(ObjectMapper mapper, int count, String src, String dst) {
        return mapper.createObjectNode()
                .put("source", src).put("target", dst).put("value", count);
    }

    // Produces a key for canonical handle on a link.
    private String linkKey(Link link) {
        String s = id(link.src());
        String d = id(link.dst());
        return s.compareTo(d) > 0 ? d + s : s + d;
    }

    // Returns a string representation of element id of the connection point.
    private String id(ConnectionPoint cp) {
        ElementId eid = cp.elementId();
        return eid instanceof DeviceId ?
                ((DeviceId) cp.elementId()).fingerprint() :
                ((HostId) cp.elementId()).ip().toString();
    }

    // Record for tracking links between the same two devices.
    private class LinkRecord {
        final String key;
        final Set<Link> links = new HashSet<>();
        Link link;

        LinkRecord(String key) {
            this.key = key;
        }

        void addLink(Link link) {
            links.add(link);
            if (this.link == null)
                this.link = link;
        }
    }
}
