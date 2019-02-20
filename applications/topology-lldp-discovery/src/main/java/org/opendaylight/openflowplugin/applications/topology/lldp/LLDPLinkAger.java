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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.aries.blueprint.annotation.service.Reference;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.mdsal.eos.binding.api.EntityOwnershipService;
import org.opendaylight.openflowplugin.api.openflow.configuration.ConfigurationListener;
import org.opendaylight.openflowplugin.api.openflow.configuration.ConfigurationService;
import org.opendaylight.openflowplugin.applications.topology.lldp.utils.LLDPDiscoveryUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.topology.discovery.rev130819.LinkDiscovered;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.topology.discovery.rev130819.LinkRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.topology.discovery.rev130819.LinkRemovedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.topology.lldp.discovery.config.rev160511.TopologyLldpDiscoveryConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class LLDPLinkAger implements ConfigurationListener, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(LLDPLinkAger.class);
    private final long linkExpirationTime;
    private final Map<LinkDiscovered, Date> linkToDate;
    private final Timer timer;
    private final NotificationPublishService notificationService;
    private final AutoCloseable configurationServiceRegistration;
    private final EntityOwnershipService eos;

    /**
     * default ctor - start timer.
     */
    @Inject
    public LLDPLinkAger(final TopologyLldpDiscoveryConfig topologyLldpDiscoveryConfig,
            @Reference final NotificationPublishService notificationService,
            @Reference final ConfigurationService configurationService,
            @Reference final EntityOwnershipService entityOwnershipService) {
        this.linkExpirationTime = topologyLldpDiscoveryConfig.getTopologyLldpExpirationInterval().getValue();
        this.notificationService = notificationService;
        this.configurationServiceRegistration = configurationService.registerListener(this);
        this.eos = entityOwnershipService;
        linkToDate = new ConcurrentHashMap<>();
        timer = new Timer();
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
                            LOG.trace("Skip publishing Link Remove event for the link {} because link destination "
                                    + "node is not owned by the controller", link);
                        }
                    }
                }
            }
        }
    }

    @VisibleForTesting
    public boolean isLinkToDateEmpty() {
        return linkToDate.isEmpty();
    }

    @Override
    public void onPropertyChanged(@Nonnull final String propertyName, @Nonnull final String propertyValue) {
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
}
