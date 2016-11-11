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
    private final BulkModStatistics statistics;
    private final int ipIndex;

    public SubscriberAdditionTask(String subscriberId, int portStart, IOpenflowFacade openflowFacade,
                                  Set<String> nodes, BulkModStatistics statistics, int ipIndex) {
        this.subscriberId = subscriberId;
        this.portStart = portStart;
        this.openflowFacade = openflowFacade;
        this.nodes = nodes;
        this.statistics = statistics;
        this.ipIndex = ipIndex;
    }

    @Override
    public void run() {
        statistics.add(StorageWrapper.getInstance().addSubscriberFromHint(subscriberId, portStart));
        for (String node: nodes) {
            Flow flow = FlowUtils.convertSubsToFlow(subscriberId, ipIndex);
            openflowFacade.modifyFlow(new NodeId(node), flow, null, true);
        }
    }
}
