/*
 * Copyright (c) 2016 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.southboundmanager.openflowservice.util;

import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

import java.util.Map;

public final class SouthboundManagerUtil {

    public static final String SWITCH_APPENDER = "openflow";
    private static final QName ENTITY_QNAME =
            org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.core.general.entity.rev150820.Entity.QNAME;
    private static final QName ENTITY_NAME = QName.create(ENTITY_QNAME, "name");

    public static final String TRIGGER_RESYNC_VIA_EOS = "sbm.resync.trigger.eos";

    // Type of PriorityTaskFactory
    public static final String PRIORITY_TASK_ACTION_TYPE = "RESYNC";

    public static NodeId getNodeId(YangInstanceIdentifier yangInstanceIdentifier){

       YangInstanceIdentifier.NodeIdentifierWithPredicates niWPredicates =
               (YangInstanceIdentifier.NodeIdentifierWithPredicates)yangInstanceIdentifier.getLastPathArgument();
        Map<QName, Object> keyValMap = niWPredicates.getKeyValues();
        String nodeIdStr = (String)(keyValMap.get(SouthboundManagerUtil.ENTITY_NAME));
        NodeId nodeId = new NodeId(nodeIdStr);
        return nodeId;
    }

    public static String getNodeIdAsString(YangInstanceIdentifier yangInstanceIdentifier){
        YangInstanceIdentifier.NodeIdentifierWithPredicates niWPredicates =
                (YangInstanceIdentifier.NodeIdentifierWithPredicates)yangInstanceIdentifier.getLastPathArgument();
        Map<QName, Object> keyValMap = niWPredicates.getKeyValues();
        String nodeIdStr = (String)(keyValMap.get(SouthboundManagerUtil.ENTITY_NAME));
        return nodeIdStr;
    }

    public static String getStringForm(NodeId nodeId){
        return nodeId.getValue();
    }
}
