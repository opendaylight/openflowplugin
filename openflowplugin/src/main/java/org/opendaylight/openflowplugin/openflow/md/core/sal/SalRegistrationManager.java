/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.sal;

import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.controller.sal.binding.api.data.DataProviderService;
import org.opendaylight.openflowplugin.openflow.md.ModelDrivenSwitch;
import org.opendaylight.openflowplugin.openflow.md.SwitchInventory;
import org.opendaylight.openflowplugin.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.openflow.md.core.session.OFSessionUtil;
import org.opendaylight.openflowplugin.openflow.md.core.session.SessionContext;
import org.opendaylight.openflowplugin.openflow.md.core.session.SessionListener;
import org.opendaylight.openflowplugin.openflow.md.core.session.SessionManager;
import org.opendaylight.openflowplugin.openflow.md.lldp.LLDPSpeaker;
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
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.InstanceIdentifierBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * session and inventory listener implementation
 */
public class SalRegistrationManager implements SessionListener, SwitchInventory {

    private final static Logger LOG = LoggerFactory.getLogger(SalRegistrationManager.class);

    Map<InstanceIdentifier<Node>, ModelDrivenSwitch> salSwitches = new ConcurrentHashMap<>();

    private ProviderContext providerContext;

    private NotificationProviderService publishService;

    private DataProviderService dataService;
    
    private SwitchFeaturesUtil swFeaturesUtil;
    
    /**
     * default ctor
     */
    public SalRegistrationManager() {
        swFeaturesUtil = SwitchFeaturesUtil.getInstance();
    }

    /**
     * @return publish service
     */
    public NotificationProviderService getPublishService() {
        return publishService;
    }

    /**
     * inject publishService
     * @param publishService
     */
    public void setPublishService(NotificationProviderService publishService) {
        this.publishService = publishService;
    }

    /**
     * @return provider context
     */
    public ProviderContext getProviderContext() {
        return providerContext;
    }

    /**
     * inject appropriate services into {@link SessionManager}
     * @param session
     */
    public void onSessionInitiated(ProviderContext session) {
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
    public void onSessionAdded(SwitchConnectionDistinguisher sessionKey, SessionContext context) {
        GetFeaturesOutput features = context.getFeatures();
        BigInteger datapathId = features.getDatapathId();
        InstanceIdentifier<Node> identifier = identifierFromDatapathId(datapathId);
        NodeRef nodeRef = new NodeRef(identifier);
        NodeId nodeId = nodeIdFromDatapathId(datapathId);
        ModelDrivenSwitchImpl ofSwitch = new ModelDrivenSwitchImpl(nodeId, identifier, context);
        LLDPSpeaker.getInstance().addModelDrivenSwitch(identifier, ofSwitch);
        salSwitches.put(identifier, ofSwitch);
        ofSwitch.register(providerContext);

        LOG.debug("ModelDrivenSwitch for {} registered to MD-SAL.", datapathId.toString());

        publishService.publish(nodeAdded(ofSwitch, features,nodeRef));
    }

    @Override
    public void onSessionRemoved(SessionContext context) {
        GetFeaturesOutput features = context.getFeatures();
        BigInteger datapathId = features.getDatapathId();
        
        MDConfiguration mdConfiguration = OFSessionUtil.getSessionManager().getMdConfiguration();
        if (mdConfiguration.isCleanConfigUponSwitchDisconnect()) {
            NodeId switchId = SalRegistrationManager.nodeIdFromDatapathId(datapathId);
            OFSessionUtil.cleanFlowsConfig(switchId, dataService, (short) 255);
            OFSessionUtil.cleanGroupsConfig(switchId, dataService);
            OFSessionUtil.cleanMetersConfig(switchId, dataService);
            
            InstanceIdentifier<Node> identifier = identifierFromDatapathId(datapathId);
            NodeRef nodeRef = new NodeRef(identifier);
            NodeRemoved nodeRemoved = nodeRemoved(nodeRef);
            LLDPSpeaker.getInstance().removeModelDrivenSwitch(identifier);
            LOG.debug("ModelDrivenSwitch for {} unregistered from MD-SAL.", datapathId.toString());
            publishService.publish(nodeRemoved);
        }
    }

    private NodeUpdated nodeAdded(ModelDrivenSwitch sw, GetFeaturesOutput features, NodeRef nodeRef) {
        NodeUpdatedBuilder builder = new NodeUpdatedBuilder();
        builder.setId(sw.getNodeId());
        builder.setNodeRef(nodeRef);
        
        FlowCapableNodeUpdatedBuilder builder2 = new FlowCapableNodeUpdatedBuilder();
        builder2.setSwitchFeatures(swFeaturesUtil.buildSwitchFeatures(features));
        builder.addAugmentation(FlowCapableNodeUpdated.class, builder2.build());
        
        return builder.build();
    }

    /**
     * @param nodeRef
     * @return representation of remove node action
     */
    private static NodeRemoved nodeRemoved(NodeRef nodeRef) {
        NodeRemovedBuilder builder = new NodeRemovedBuilder();
        builder.setNodeRef(nodeRef);
        return builder.build();
    }

    @Override
    public ModelDrivenSwitch getSwitch(NodeRef node) {
        return salSwitches.get(node.getValue());
    }

    /**
     * @param datapathId
     * @return instanceIdentifier build upon Nodes/Node/(key baked from datapathId)
     */
    public static InstanceIdentifier<Node> identifierFromDatapathId(BigInteger datapathId) {
        NodeKey nodeKey = nodeKeyFromDatapathId(datapathId);
        InstanceIdentifierBuilder<Node> builder = InstanceIdentifier.builder(Nodes.class).child(Node.class,nodeKey);
        return builder.toInstance();
    }

    /**
     * @param datapathId
     * @return nodeKey baked from nodeId (baked from datapathId)
     */
    public static NodeKey nodeKeyFromDatapathId(BigInteger datapathId) {
        return new NodeKey(nodeIdFromDatapathId(datapathId));
    }

    /**
     * @param datapathId
     * @return nodeId baked from openflow prefix and datapathId (separated by colon)
     */
    public static NodeId nodeIdFromDatapathId(BigInteger datapathId) {
        // FIXME: Convert to textual representation of datapathID
        String current = datapathId.toString();
        return new NodeId("openflow:" + current);
    }

    /**
     * @return singleton of {@link SessionManager}
     */
    public SessionManager getSessionManager() {
        return OFSessionUtil.getSessionManager();
    }
}
