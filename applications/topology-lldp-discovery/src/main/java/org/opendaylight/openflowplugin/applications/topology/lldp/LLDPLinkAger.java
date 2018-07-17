/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.topology.lldp;

import com.google.common.annotations.VisibleForTesting;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.aries.blueprint.annotation.service.Reference;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.ClusteredDataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataObjectModification;
import org.opendaylight.mdsal.binding.api.DataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.eos.binding.api.EntityOwnershipService;
import org.opendaylight.openflowplugin.api.openflow.configuration.ConfigurationListener;
import org.opendaylight.openflowplugin.api.openflow.configuration.ConfigurationService;
import org.opendaylight.openflowplugin.applications.topology.lldp.utils.LLDPDiscoveryUtils;
import org.opendaylight.openflowplugin.common.wait.SimpleTaskRetryLooper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.topology.discovery.rev130819.LinkDiscovered;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.topology.discovery.rev130819.LinkRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.topology.discovery.rev130819.LinkRemovedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.topology.lldp.discovery.config.rev160511.TopologyLldpDiscoveryConfig;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class LLDPLinkAger implements ConfigurationListener, ClusteredDataTreeChangeListener<Link>, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(LLDPLinkAger.class);
    private static final long STARTUP_LOOP_TICK = 500L;
    private static final int STARTUP_LOOP_MAX_RETRIES = 8;
    static final String TOPOLOGY_ID = "flow:1";
    static final InstanceIdentifier<Link> II_TO_LINK = InstanceIdentifier.create(NetworkTopology.class)
            .child(Topology.class, new TopologyKey(new TopologyId(TOPOLOGY_ID))).child(Link.class);

    private final long linkExpirationTime;
    private final Map<LinkDiscovered, Date> linkToDate;
    private final Timer timer;
    private final NotificationPublishService notificationService;
    private final AutoCloseable configurationServiceRegistration;
    private final EntityOwnershipService eos;
    private final ListenerRegistration<DataTreeChangeListener> listenerRegistration;

    /**
     * default ctor - start timer.
     */
    @Inject
    @SuppressWarnings("checkstyle:IllegalCatch")
    public LLDPLinkAger(final TopologyLldpDiscoveryConfig topologyLldpDiscoveryConfig,
                        @Reference final NotificationPublishService notificationService,
                        @Reference final ConfigurationService configurationService,
                        @Reference final EntityOwnershipService entityOwnershipService,
                        @Reference final DataBroker dataBroker) {
        this.linkExpirationTime = topologyLldpDiscoveryConfig.getTopologyLldpExpirationInterval().getValue();
        this.notificationService = notificationService;
        this.configurationServiceRegistration = configurationService.registerListener(this);
        this.eos = entityOwnershipService;
        linkToDate = new ConcurrentHashMap<>();
        timer = new Timer();
        final DataTreeIdentifier dtiToNodeConnector = DataTreeIdentifier.create(LogicalDatastoreType.OPERATIONAL,
                II_TO_LINK);
        final SimpleTaskRetryLooper looper = new SimpleTaskRetryLooper(STARTUP_LOOP_TICK, STARTUP_LOOP_MAX_RETRIES);
        try {
            listenerRegistration = looper.loopUntilNoException(() ->
                    dataBroker.registerDataTreeChangeListener(dtiToNodeConnector, LLDPLinkAger.this));
        } catch (Exception e) {
            LOG.error("DataTreeChangeListeners registration failed:", e);
            throw new IllegalStateException("LLDPLinkAger startup failed!", e);
        }
        timer.schedule(new LLDPAgingTask(), 0, topologyLldpDiscoveryConfig.getTopologyLldpInterval().getValue());
    }

    public void put(LinkDiscovered link) {
        Date expires = new Date();
        expires.setTime(expires.getTime() + linkExpirationTime);
        linkToDate.put(link, expires);
    }

    @Override
    @PreDestroy
    public void close() throws Exception {
        timer.cancel();
        linkToDate.clear();
        configurationServiceRegistration.close();
    }

    @Override
    public void onDataTreeChanged(@NonNull Collection<DataTreeModification<Link>> changes) {
        for (DataTreeModification modification : changes) {
            switch (modification.getRootNode().getModificationType()) {
                case WRITE:
                    break;
                case SUBTREE_MODIFIED:
                    // NOOP
                    break;
                case DELETE:
                    processLinkDeleted(modification.getRootNode());
                    break;
                default:
                    throw new IllegalArgumentException(
                            "Unhandled modification type: {}" + modification.getRootNode().getModificationType());
            }
        }
    }

    private void processLinkDeleted(DataObjectModification rootNode) {
        Link link = (Link) rootNode.getDataBefore();
        LOG.info("processLinkdeleted received for link Link-{}:{}", link.getDestination().getDestNode().getValue(),
                link.getSource().getSourceNode().getValue());
        if (LLDPDiscoveryUtils.isEntityOwned(eos, link.getDestination().getDestNode().getValue())) {
            StringBuilder str = new StringBuilder();
            linkToDate.keySet().forEach(a ->
                    str.append("link-" + getNodeIdFromNodeIdentifier(a.getDestination().getValue()) + "-"
                            + getNodeIdFromNodeIdentifier(a.getSource().getValue()) + ", "));
            LOG.info("LinktoDate contains {}", str.toString());
            str.delete(0, str.length());
            LinkDiscovered linkDiscovered = LLDPDiscoveryUtils
                    .toLLDPLinkDiscovered(link);
            linkToDate.remove(linkDiscovered);
            linkToDate.keySet().forEach(a ->
                    str.append("link-" + getNodeIdFromNodeIdentifier(a.getDestination().getValue()) + "-"
                            + getNodeIdFromNodeIdentifier(a.getSource().getValue()) + ", "));
            LOG.info("LinktoDate contains after deletion {}", str.toString());
        }
    }

    public static String getNodeIdFromNodeIdentifier(final InstanceIdentifier<?> nodeIdent) {
        String nodeId = nodeIdent.firstKeyOf(Node.class).getId().getValue();
        return nodeId.substring(nodeId.lastIndexOf(":") + 1);
    }

    private class LLDPAgingTask extends TimerTask {

        @Override
        public void run() {
            for (Entry<LinkDiscovered, Date> entry : linkToDate.entrySet()) {
                LinkDiscovered link = entry.getKey();
                Date expires = entry.getValue();
                Date now = new Date();
                if (now.after(expires)) {
                    if (notificationService != null) {
                        LinkRemovedBuilder lrb = new LinkRemovedBuilder(link);
                        NodeKey nodeKey = link.getDestination().getValue().firstKeyOf(Node.class);
                        LOG.info("No update received for link {} from last {} milliseconds. Removing link from cache.",
                                link, linkExpirationTime);
                        linkToDate.remove(link);
                        if (nodeKey != null && LLDPDiscoveryUtils.isEntityOwned(eos, nodeKey.getId().getValue())) {
                            LOG.info("Publish Link Remove event for the link {}", link);
                            final LinkRemoved lr = lrb.build();
                            try {
                                notificationService.putNotification(lr);
                            } catch (InterruptedException e) {
                                LOG.warn("Interrupted while publishing notification {}", lr, e);
                            }
                        } else {
                            LOG.info("Skip publishing Link Remove event for the link {} because link destination "
                                    + "node is not owned by the controller", link);
                        }
                    }
                }
            }
        }
    }

    public boolean isLinkPresent(final LinkDiscovered linkDiscovered) {
        return linkToDate.containsKey(linkDiscovered);
    }

    @VisibleForTesting
    public boolean isLinkToDateEmpty() {
        return linkToDate.isEmpty();
    }

    @Override
    public void onPropertyChanged(@Nonnull final String propertyName, @Nonnull final String propertyValue) {
        Optional.ofNullable(TopologyLLDPDiscoveryProperty.forValue(propertyName)).ifPresent(lldpDiscoveryProperty -> {
            switch (lldpDiscoveryProperty) {
                case LLDP_SECURE_KEY:
                case TOPOLOGY_LLDP_INTERVAL:
                case TOPOLOGY_LLDP_EXPIRATION_INTERVAL:
                    LOG.warn("Runtime update not supported for property {}", lldpDiscoveryProperty);
                    break;
                default:
                    LOG.warn("No topology lldp discovery property found.");
                    break;
            }
        });
    }
}
