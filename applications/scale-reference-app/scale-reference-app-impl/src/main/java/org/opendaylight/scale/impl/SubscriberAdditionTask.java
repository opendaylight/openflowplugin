package org.opendaylight.scale.impl;

import org.opendaylight.openflowplugin.applications.southboundmanager.openflowservice.api.IOpenflowFacade;
import org.opendaylight.scale.dataaccess.StorageWrapper;
import org.opendaylight.scale.util.FlowUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;

import java.util.Set;

/**
 * Created by evijayd on 10/6/2016.
 */
public class SubscriberAdditionTask implements Runnable{

    private final String subscriberId;
    private final int portStart;
    private final IOpenflowFacade openflowFacade;
    private final Set<String> nodes;
    private final static Ipv4Prefix DEFAULT_IP_PREFIX = new Ipv4Prefix("127.0.0.1/32");

    public SubscriberAdditionTask(String subscriberId, int portStart, IOpenflowFacade openflowFacade, Set<String> nodes){
        this.subscriberId = subscriberId;
        this.portStart = portStart;
        this.openflowFacade = openflowFacade;
        this.nodes = nodes;
    }

    @Override
    public void run() {
        StorageWrapper.getInstance().addSubscriberFromHint(subscriberId, portStart);
        Flow flow = FlowUtils.convertSubsToFlow(subscriberId, DEFAULT_IP_PREFIX);
        for (String node: nodes) {
            openflowFacade.modifyFlow(new NodeId(node), flow, null, true);
        }
    }
}
