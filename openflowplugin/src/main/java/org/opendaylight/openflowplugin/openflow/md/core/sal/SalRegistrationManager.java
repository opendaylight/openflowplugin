/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.sal;

import java.math.BigInteger;

import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.controller.sal.binding.api.data.DataProviderService;
import org.opendaylight.openflowplugin.openflow.md.ModelDrivenSwitch;
import org.opendaylight.openflowplugin.openflow.md.core.session.OFSessionUtil;
import org.opendaylight.openflowplugin.openflow.md.core.session.SessionContext;
import org.opendaylight.openflowplugin.openflow.md.core.session.SessionListener;
import org.opendaylight.openflowplugin.openflow.md.core.session.SessionManager;
import org.opendaylight.openflowplugin.openflow.md.core.session.SwitchSessionKeyOF;
import org.opendaylight.openflowplugin.openflow.md.lldp.LLDPSpeaker;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.reconcil.rev140616.NodeReconcilAdd;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.reconcil.rev140616.NodeReconcilAddBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.reconcil.rev140616.NodeReconcilDel;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.reconcil.rev140616.NodeReconcilDelBuilder;
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
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.InstanceIdentifierBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * session and inventory listener implementation
 */
public class SalRegistrationManager implements SessionListener, AutoCloseable {

    private final static Logger LOG = LoggerFactory.getLogger(SalRegistrationManager.class);

    private ProviderContext providerContext;

    private NotificationProviderService publishService;

    private DataProviderService dataService;

    private final SwitchFeaturesUtil swFeaturesUtil;

    public SalRegistrationManager() {
        swFeaturesUtil = SwitchFeaturesUtil.getInstance();
    }

    public NotificationProviderService getPublishService() {
        return publishService;
    }

    public void setPublishService(NotificationProviderService publishService) {
        this.publishService = publishService;
    }

    public ProviderContext getProviderContext() {
        return providerContext;
    }

    public void onSessionInitiated(ProviderContext session) {
        LOG.debug("onSessionInitiated");
        this.providerContext = session;
        this.publishService = session.getSALService(NotificationProviderService.class);
        this.dataService = session.getSALService(DataProviderService.class);
        // We register as listener for Session Manager
        getSessionManager().registerSessionListener(this);
        getSessionManager().setNotificationProviderService(publishService);
        getSessionManager().setDataProviderService(dataService);
        LOG.debug("SalRegistrationManager initialized");
    }

    @Override
    public void onSessionAdded(SwitchSessionKeyOF sessionKey, SessionContext context) {
        GetFeaturesOutput features = context.getFeatures();
        BigInteger datapathId = features.getDatapathId();
        InstanceIdentifier<Node> identifier = identifierFromDatapathId(datapathId);
        NodeRef nodeRef = new NodeRef(identifier);
        NodeId nodeId = nodeIdFromDatapathId(datapathId);
        ModelDrivenSwitchImpl ofSwitch = new ModelDrivenSwitchImpl(nodeId, identifier, context);
        LLDPSpeaker.getInstance().addModelDrivenSwitch(identifier, ofSwitch);
        CompositeObjectRegistration<ModelDrivenSwitch> registration = ofSwitch.register(providerContext);
        context.setProviderRegistration(registration);

        LOG.debug("ModelDrivenSwitch for {} registered to MD-SAL.", datapathId.toString());

        publishService.publish(nodeAdded(ofSwitch, features,nodeRef));
        publishService.publish( prepareAddNodeReconciliation( nodeRef ) );
    }

    @Override
    public void onSessionRemoved(SessionContext context) {
        GetFeaturesOutput features = context.getFeatures();
        BigInteger datapathId = features.getDatapathId();
        InstanceIdentifier<Node> identifier = identifierFromDatapathId(datapathId);
        NodeRef nodeRef = new NodeRef(identifier);
        NodeRemoved nodeRemoved = nodeRemoved(nodeRef);
        LLDPSpeaker.getInstance().removeModelDrivenSwitch(identifier);
        CompositeObjectRegistration<ModelDrivenSwitch> registration = context.getProviderRegistration();
        registration.close();

        LOG.debug("ModelDrivenSwitch for {} unregistered from MD-SAL.", datapathId.toString());
        publishService.publish( prepareDelNodeReconciliation( nodeRef ) );
        publishService.publish(nodeRemoved);
    }

    private NodeReconcilAdd prepareAddNodeReconciliation( final NodeRef nodeRef ) {
        final NodeReconcilAddBuilder nodeReconcil = new NodeReconcilAddBuilder();
        nodeReconcil.setNodeRef( nodeRef );
        return nodeReconcil.build();
    }

    private NodeReconcilDel prepareDelNodeReconciliation( final NodeRef nodeRef ) {
        final NodeReconcilDelBuilder nodeReconcil = new NodeReconcilDelBuilder();
        nodeReconcil.setNodeRef( nodeRef );
        return nodeReconcil.build();
    }

    private NodeUpdated nodeAdded(ModelDrivenSwitch sw, GetFeaturesOutput features, NodeRef nodeRef) {

        FlowCapableNodeUpdatedBuilder flowCapNodeBuilder = new FlowCapableNodeUpdatedBuilder();
        flowCapNodeBuilder.setSwitchFeatures(swFeaturesUtil.buildSwitchFeatures(features));

        NodeUpdatedBuilder builder = new NodeUpdatedBuilder();
        builder.setId(sw.getNodeId());
        builder.setNodeRef(nodeRef);
        builder.addAugmentation(FlowCapableNodeUpdated.class, flowCapNodeBuilder.build());

        return builder.build();
    }

    private NodeRemoved nodeRemoved(NodeRef nodeRef) {
        NodeRemovedBuilder builder = new NodeRemovedBuilder();
        builder.setNodeRef(nodeRef);
        return builder.build();
    }

    public static InstanceIdentifier<Node> identifierFromDatapathId(BigInteger datapathId) {
        NodeKey nodeKey = nodeKeyFromDatapathId(datapathId);
        InstanceIdentifierBuilder<Node> builder = InstanceIdentifier.builder(Nodes.class).child(Node.class,nodeKey);
        return builder.toInstance();
    }

    public static NodeKey nodeKeyFromDatapathId(BigInteger datapathId) {
        return new NodeKey(nodeIdFromDatapathId(datapathId));
    }

    public static NodeId nodeIdFromDatapathId(BigInteger datapathId) {
        // FIXME: Convert to textual representation of datapathID
        String current = datapathId.toString();
        return new NodeId("openflow:" + current);
    }

    public SessionManager getSessionManager() {
        return OFSessionUtil.getSessionManager();
    }

    @Override
    public void close() {
        LOG.debug("close");
        dataService = null;
        providerContext = null;
        publishService = null;
    }
}
