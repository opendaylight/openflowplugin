/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.sal;

import com.google.common.base.Preconditions;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.openflowplugin.api.openflow.md.ModelDrivenSwitch;
import org.opendaylight.openflowplugin.api.openflow.md.core.NotificationQueueWrapper;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.SessionContext;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.SessionListener;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.SessionManager;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.SwitchSessionKeyOF;
import org.opendaylight.openflowplugin.openflow.md.core.session.OFSessionUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRemovedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yangtools.concepts.CompositeObjectRegistration;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.InstanceIdentifierBuilder;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipService;
import org.opendaylight.openflowplugin.openflow.md.core.role.OfEntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * session and inventory listener implementation
 */
public class SalRegistrationManager implements SessionListener, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(SalRegistrationManager.class);

    private NotificationProviderService publishService;

    private DataBroker dataService;

    private RpcProviderRegistry rpcProviderRegistry;

    private final SwitchFeaturesUtil swFeaturesUtil;

    private ListenerRegistration<SessionListener> sessionListenerRegistration;

    private OfEntityManager entManager;
    private ModelDrivenSwitchImpl ofSwitch;

    public SalRegistrationManager() {
        swFeaturesUtil = SwitchFeaturesUtil.getInstance();
    }

    public NotificationProviderService getPublishService() {
        return publishService;
    }

    public void setPublishService(final NotificationProviderService publishService) {
        this.publishService = publishService;
    }

    public void setDataService(final DataBroker dataService) {
        this.dataService = dataService;
    }

    public void setRpcProviderRegistry(final RpcProviderRegistry rpcProviderRegistry) {
        this.rpcProviderRegistry = rpcProviderRegistry;
    }

    public void setOfEntityManager(OfEntityManager entManager) {
	this.entManager = entManager;
    }

    public void init() {
        LOG.debug("init..");
        sessionListenerRegistration = getSessionManager().registerSessionListener(this);
        getSessionManager().setNotificationProviderService(publishService);
        getSessionManager().setDataBroker(dataService);
        LOG.debug("SalRegistrationManager initialized");
    }

    @Override
    public void onSessionAdded(final SwitchSessionKeyOF sessionKey, final SessionContext context) {
        GetFeaturesOutput features = context.getFeatures();
        BigInteger datapathId = features.getDatapathId();
        InstanceIdentifier<Node> identifier = identifierFromDatapathId(datapathId);
        NodeRef nodeRef = new NodeRef(identifier);
        NodeId nodeId = nodeIdFromDatapathId(datapathId);
        ofSwitch = new ModelDrivenSwitchImpl(nodeId, identifier,
							 context);
        CompositeObjectRegistration<ModelDrivenSwitch> registration =
                ofSwitch.register(rpcProviderRegistry);
        context.setProviderRegistration(registration);

        LOG.debug("ModelDrivenSwitch for {} registered to MD-SAL.", datapathId);
        ofSwitch.reqOpenflowEntityOwnership(entManager, nodeId, context);
        NotificationQueueWrapper wrappedNotification = new NotificationQueueWrapper(
                nodeAdded(ofSwitch, features, nodeRef),
                context.getFeatures().getVersion());
        context.getNotificationEnqueuer().enqueueNotification(wrappedNotification);
    }

    @Override
    public void setRole (SessionContext context) {
	ofSwitch.setRole(entManager, context);
    }

    @Override
    public void onSessionRemoved(final SessionContext context) {
        GetFeaturesOutput features = context.getFeatures();
        BigInteger datapathId = features.getDatapathId();
        InstanceIdentifier<Node> identifier = identifierFromDatapathId(datapathId);
        NodeRef nodeRef = new NodeRef(identifier);
	    NodeId nodeId = nodeIdFromDatapathId(datapathId);
	    ofSwitch.unregOpenflowEntityOwnership(entManager, nodeId);
        NodeRemoved nodeRemoved = nodeRemoved(nodeRef);

        CompositeObjectRegistration<ModelDrivenSwitch> registration = context.getProviderRegistration();
        if (null != registration) {
            registration.close();
            context.setProviderRegistration(null);
        }
        LOG.debug("ModelDrivenSwitch for {} unregistered from MD-SAL.", datapathId);

        NotificationQueueWrapper wrappedNotification = new NotificationQueueWrapper(
                nodeRemoved, context.getFeatures().getVersion());
        context.getNotificationEnqueuer().enqueueNotification(wrappedNotification);
    }


    private NodeUpdated nodeAdded(final ModelDrivenSwitch sw, final GetFeaturesOutput features, final NodeRef nodeRef) {
        NodeUpdatedBuilder builder = new NodeUpdatedBuilder();
        builder.setId(sw.getNodeId());
        builder.setNodeRef(nodeRef);

        FlowCapableNodeUpdatedBuilder builder2 = new FlowCapableNodeUpdatedBuilder();
        try {
            builder2.setIpAddress(getIpAddressOf(sw));
        } catch (Exception e) {
            LOG.warn("IP address of the node {} cannot be obtained.", sw.getNodeId(), e);
        }
        builder2.setSwitchFeatures(swFeaturesUtil.buildSwitchFeatures(features));
        builder.addAugmentation(FlowCapableNodeUpdated.class, builder2.build());

        return builder.build();
    }

    private static IpAddress getIpAddressOf(final ModelDrivenSwitch sw) {
        SessionContext sessionContext = sw.getSessionContext();
        Preconditions.checkNotNull(sessionContext.getPrimaryConductor(),
                "primary conductor must not be NULL -> " + sw.getNodeId());
        Preconditions.checkNotNull(sessionContext.getPrimaryConductor().getConnectionAdapter(),
                "connection adapter of primary conductor must not be NULL -> " + sw.getNodeId());
        InetSocketAddress remoteAddress = sessionContext.getPrimaryConductor().getConnectionAdapter()
                .getRemoteAddress();
        if (remoteAddress == null) {
            LOG.warn("IP address of the node {} cannot be obtained. No connection with switch.", sw.getNodeId());
            return null;
        }
        return resolveIpAddress(remoteAddress.getAddress());
    }

    private static IpAddress resolveIpAddress(final InetAddress address) {
        String hostAddress = address.getHostAddress();
        if (address instanceof Inet4Address) {
            return new IpAddress(new Ipv4Address(hostAddress));
        }
        if (address instanceof Inet6Address) {
            return new IpAddress(new Ipv6Address(hostAddress));
        }
        throw new IllegalArgumentException("Unsupported IP address type!");
    }

    private static NodeRemoved nodeRemoved(final NodeRef nodeRef) {
        NodeRemovedBuilder builder = new NodeRemovedBuilder();
        builder.setNodeRef(nodeRef);
        return builder.build();
    }

    public static InstanceIdentifier<Node> identifierFromDatapathId(final BigInteger datapathId) {
        NodeKey nodeKey = nodeKeyFromDatapathId(datapathId);
        InstanceIdentifierBuilder<Node> builder = InstanceIdentifier.builder(Nodes.class).child(Node.class, nodeKey);
        return builder.build();
    }

    public static NodeKey nodeKeyFromDatapathId(final BigInteger datapathId) {
        return new NodeKey(nodeIdFromDatapathId(datapathId));
    }

    public static NodeId nodeIdFromDatapathId(final BigInteger datapathId) {
        // FIXME: Convert to textual representation of datapathID
        String current = String.valueOf(datapathId);
        return new NodeId("openflow:" + current);
    }

    public SessionManager getSessionManager() {
        return OFSessionUtil.getSessionManager();
    }

    @Override
    public void close() {
        LOG.debug("close");
        dataService = null;
        rpcProviderRegistry = null;
        publishService = null;
        if (sessionListenerRegistration != null) {
            sessionListenerRegistration.close();
        }
    }
}
