package org.opendaylight.openflowplugin.openflow.md.core.sal;

import java.io.Console;
import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.openflowplugin.openflow.md.ModelDrivenSwitch;
import org.opendaylight.openflowplugin.openflow.md.SwitchInventory;
import org.opendaylight.openflowplugin.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.openflow.md.core.session.OFSessionUtil;
import org.opendaylight.openflowplugin.openflow.md.core.session.SessionContext;
import org.opendaylight.openflowplugin.openflow.md.core.session.SessionListener;
import org.opendaylight.openflowplugin.openflow.md.core.session.SessionManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
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

public class SalRegistrationManager implements SessionListener, SwitchInventory {

    private final static Logger LOG = LoggerFactory.getLogger(SalRegistrationManager.class);

    Map<InstanceIdentifier<Node>, ModelDrivenSwitch> salSwitches = new ConcurrentHashMap<>();

    private ProviderContext providerContext;

    private NotificationProviderService publishService;

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
        this.providerContext = session;
        this.publishService = session.getSALService(NotificationProviderService.class);

        // We register as listener for Session Manager
        getSessionManager().registerSessionListener(this);
        LOG.info("SalRegistrationManager initialized");

    }

    @Override
    public void onSessionAdded(SwitchConnectionDistinguisher sessionKey, SessionContext context) {
        GetFeaturesOutput features = context.getFeatures();
        BigInteger datapathId = features.getDatapathId();
        InstanceIdentifier<Node> identifier = identifierFromDatapathId(datapathId);

        NodeId nodeId = nodeIdFromDatapathId(datapathId);
        ModelDrivenSwitchImpl ofSwitch = new ModelDrivenSwitchImpl(nodeId, identifier, context);
        salSwitches.put(identifier, ofSwitch);
        ofSwitch.register(providerContext);

        LOG.info("ModelDrivenSwitch for {} registered to MD-SAL.", datapathId.toString());

        publishService.publish(nodeAdded(ofSwitch, features));
    }

    private NodeUpdated nodeAdded(ModelDrivenSwitch sw, GetFeaturesOutput features) {
        NodeUpdatedBuilder builder = new NodeUpdatedBuilder();
        builder.setId(sw.getNodeId());
        return builder.build();
    }

    @Override
    public ModelDrivenSwitch getSwitch(NodeRef node) {
        return salSwitches.get(node.getValue());
    }

    public static InstanceIdentifier<Node> identifierFromDatapathId(BigInteger datapathId) {
        InstanceIdentifierBuilder<?> builder = InstanceIdentifier.builder().node(Nodes.class);

        NodeKey nodeKey = nodeKeyFromDatapathId(datapathId);
        return builder.node(Node.class, nodeKey).toInstance();
    }

    public static NodeKey nodeKeyFromDatapathId(BigInteger datapathId) {
        return new NodeKey(nodeIdFromDatapathId(datapathId));
    }

    public static NodeId nodeIdFromDatapathId(BigInteger datapathId) {
        // FIXME: Convert to textual representation of datapathID
        String current = datapathId.toString();
        return new NodeId("openflow://" + current);
    }

    public SessionManager getSessionManager() {
        return OFSessionUtil.getSessionManager();
    }
}
