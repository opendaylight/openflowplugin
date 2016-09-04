/*
 * Copyright (c) 2016 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.reference.app.openflow;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * FlowCloningHandler is used to clone the flows to handle scenario
 * "add two flows on southbound for every flow add request coming from Northbound"
 */
public class FlowCloningHandler {

    // count of clone to be created for flow
    private final static int FLOW_CLONE_COUNT = Integer.getInteger("reference.app.flow.clone.count", 1);


    /**
     * clone flow based on flow count. it clone flows by changing the flow table id.
     * If FLOW_CLONE_COUNT value is 1 then flow is added in table 0.
     * if FLOW_CLONE_COUNT value is 2 then same flow is added in table 0 and table 1.
     *
     * @param flow - flow to be use for cloning
     * @return -- list of flows after cloning
     */
    public List<Flow> cloneFlow(Flow flow) {
        List<Flow> flowList = new ArrayList<>();
        if (1 == FLOW_CLONE_COUNT) {
            flowList.add(flow);
        } else {
            for (int i = 0; i < FLOW_CLONE_COUNT; i++) {
                FlowBuilder tempFlowBuilder = new FlowBuilder(flow);
                tempFlowBuilder.setTableId((short) i);
                Flow tempFlow = tempFlowBuilder.build();
                flowList.add(tempFlow);
            }
        }
        return flowList;
    }

}