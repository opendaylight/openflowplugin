package org.opendaylight.openflowplugin.test.ForwardingConsumer;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlowClientBuild {

    private static final Logger logger = LoggerFactory.getLogger(FlowClientBuild.class);


    public FlowBuilder installFlow(OvsFlowMatch ovsFlowMatch,
            OvsFlowInstruction ovsFlowInstruction,
            OvsFlowParams ovsFlowParams,
            FlowBuilder flow) {

        ovsFlowMatch.buildNewMatch(flow, ovsFlowMatch);
        ovsFlowInstruction.buildClientInstruction(flow, ovsFlowInstruction);
        ovsFlowParams.buildNewFlowParam(flow, ovsFlowParams);
        return flow;
    }
}