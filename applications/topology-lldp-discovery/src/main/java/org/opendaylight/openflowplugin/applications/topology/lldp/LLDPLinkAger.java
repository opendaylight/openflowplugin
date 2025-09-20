/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.topology.lldp;

import com.google.common.annotations.VisibleForTesting;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataObjectModification;
import org.opendaylight.mdsal.binding.api.DataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.eos.binding.api.EntityOwnershipService;
import org.opendaylight.openflowplugin.api.openflow.configuration.ConfigurationListener;
import org.opendaylight.openflowplugin.api.openflow.configuration.ConfigurationService;
import org.opendaylight.openflowplugin.applications.topology.lldp.utils.LLDPDiscoveryUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.topology.discovery.rev130819.LinkDiscovered;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.topology.discovery.rev130819.LinkRemovedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.topology.lldp.discovery.config.rev160511.TopologyLldpDiscoveryConfig;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;
import org.opendaylight.yangtools.binding.DataObjectReference;
import org.opendaylight.yangtools.concepts.Registration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public final class LLDPLinkAger implements ConfigurationListener, DataTreeChangeListener<Link>, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(LLDPLinkAger.class);

    private final long linkExpirationTime;
    // FIXME: use Instant instead of Date
    private final ConcurrentMap<LinkDiscovered, Date> linkToDate = new ConcurrentHashMap<>();
    private final Timer timer = new Timer();
    private final NotificationPublishService notificationService;
    private final AutoCloseable configurationServiceRegistration;
    private final EntityOwnershipService eos;
    private Registration listenerRegistration;

    /**
     * default ctor - start timer.
     */
    @Inject
    public LLDPLinkAger(final TopologyLldpDiscoveryConfig topologyLldpDiscoveryConfig,
                        final NotificationPublishService notificationService,
                        final ConfigurationService configurationService,
                        final EntityOwnershipService entityOwnershipService,
                        final DataBroker dataBroker) {
        linkExpirationTime = topologyLldpDiscoveryConfig.getTopologyLldpExpirationInterval().getValue().toJava();
        this.notificationService = notificationService;
        configurationServiceRegistration = configurationService.registerListener(this);
        eos = entityOwnershipService;
        listenerRegistration = dataBroker.registerTreeChangeListener(LogicalDatastoreType.OPERATIONAL,
            DataObjectReference.builder(NetworkTopology.class)
                // FIXME: do not hard-code the topology-id here: we should be servicing all topologies that support
                //        LLDP discovery
                .child(Topology.class, new TopologyKey(new TopologyId("flow:1")))
                .child(Link.class)
                .build(),
            this);
        timer.schedule(new LLDPAgingTask(), 0,
            topologyLldpDiscoveryConfig.getTopologyLldpInterval().getValue().toJava());
    }

    public void put(final LinkDiscovered link) {
        Date expires = new Date();
        expires.setTime(expires.getTime() + linkExpirationTime);
        linkToDate.put(link, expires);
    }

    @Override
    @PreDestroy
    public void close() throws Exception {
        if (listenerRegistration != null) {
            listenerRegistration.close();
            listenerRegistration = null;
        }
        timer.cancel();
        linkToDate.clear();
        configurationServiceRegistration.close();
    }

    @Override
    public void onDataTreeChanged(final List<DataTreeModification<Link>> changes) {
        for (DataTreeModification<Link> modification : changes) {
            switch (modification.getRootNode().modificationType()) {
                case WRITE:
                    break;
                case SUBTREE_MODIFIED:
                    // NOOP
                    break;
                case DELETE:
                    processLinkDeleted(modification.getRootNode());
                    break;
                default:
                    LOG.error("Unhandled modification type: {}", modification.getRootNode().modificationType());
            }
        }
    }

    @VisibleForTesting
    public boolean isLinkToDateEmpty() {
        return linkToDate.isEmpty();
    }

    @Override
    public void onPropertyChanged(@NonNull final String propertyName, @NonNull final String propertyValue) {
        final TopologyLLDPDiscoveryProperty lldpDiscoveryProperty = TopologyLLDPDiscoveryProperty.forValue(
                propertyName);
        if (lldpDiscoveryProperty != null) {
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
        }
    }

    protected boolean isLinkPresent(final LinkDiscovered linkDiscovered) {
        return linkToDate.containsKey(linkDiscovered);
    }

    private void processLinkDeleted(final DataObjectModification<Link> rootNode) {
        Link link = rootNode.dataBefore();
        LOG.trace("Removing link {} from linkToDate cache", link);
        linkToDate.remove(LLDPDiscoveryUtils.toLLDPLinkDiscovered(link));
    }

    private final class LLDPAgingTask extends TimerTask {
        @Override
        public void run() {
            for (var entry : linkToDate.entrySet()) {
                final var link = entry.getKey();
                final var expires = entry.getValue();
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
                            final var lr = lrb.build();
                            try {
                                notificationService.putNotification(lr);
                            } catch (InterruptedException e) {
                                LOG.warn("Interrupted while publishing notification {}", lr, e);
                            }
                        } else {
                            LOG.trace("Skip publishing Link Remove event for the link {} because link destination "
                                    + "node is not owned by the controller", link);
                        }
                    }
                }
            }
        }
    }
}
