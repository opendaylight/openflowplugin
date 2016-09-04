/*
 * Copyright (c) 2016 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.reference.app.reconciliation;

import org.opendaylight.reference.app.openflow.FlowCloningHandler;
import org.opendaylight.reference.app.openflow.FlowProgrammer;
import org.opendaylight.reference.app.ReferenceAppUtil;
import org.opendaylight.openflowplugin.applications.southboundmanager.openflowservice.api.ErrorCallable;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ref.app.rev160504.subscriber.list.entries.SubscriberListEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.RecursiveAction;


/**
 * Created by etaseth on 4/19/2016.
 */
public class ReconciliationRecursive extends RecursiveAction {
    private static final Logger LOG = LoggerFactory.getLogger(ReconciliationRecursive.class);
    private static String entityContext;
    private static FlowProgrammer flowProgrammer;
    private static List<SubscriberListEntry> subsList;
    private static FlowCloningHandler flowCloningHandler;

    public ReconciliationRecursive(List<SubscriberListEntry> subsList, String entityContext, FlowProgrammer flowProgrammer) {
        this.subsList = subsList;
        this.entityContext = entityContext;
        this.flowProgrammer = flowProgrammer;
        this.flowCloningHandler = new FlowCloningHandler();
    }

    @Override
    protected void compute() {

        for (SubscriberListEntry entry : subsList) {
            Flow flow = ReferenceAppUtil.convertSubsToFlow(entry);
            List<Flow> flowCloneList = flowCloningHandler.cloneFlow(flow);
            for (Flow tempFlow : flowCloneList) {
                flowProgrammer.addFlow(tempFlow, entityContext, new ReconcialiationErrorCallable());
            }
        }
    }

    private class ReconcialiationErrorCallable extends ErrorCallable {

        @Override
        public Object call() throws Exception {
            LOG.error("flow failed with error {} while reconciliation.", this.getCause());
            return null;
        }
    }
}

