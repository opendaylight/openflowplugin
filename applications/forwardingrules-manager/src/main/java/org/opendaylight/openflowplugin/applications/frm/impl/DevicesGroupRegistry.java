package org.opendaylight.openflowplugin.applications.frm.impl;

import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by eabeegm on 2/20/2018.
 */
public class DevicesGroupRegistry {
    private final Map<NodeId, List<Long>> dpnGroupMapping = new ConcurrentHashMap<>();


    public boolean isGroupExits(NodeId nodeId, Long groupId) {
        return dpnGroupMapping.get(nodeId) !=null ? dpnGroupMapping.get(nodeId).contains(groupId) : false;
    }

    public void storeGroup(NodeId nodeId, Long groupId) {
        dpnGroupMapping.computeIfAbsent(nodeId, groupIdList -> new ArrayList<>()).add(groupId);

    }

    public void removeGroup(NodeId nodeId, Long groupId) {
        dpnGroupMapping.computeIfPresent(nodeId, (node, groupIds)-> groupIds).remove(groupId);
    }

    public void clearNodeGroups(NodeId nodeId) {
        dpnGroupMapping.remove(nodeId);
    }


}
