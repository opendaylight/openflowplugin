/*
 * Copyright (c) 2016 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.reference.app.reconciliation;

import org.opendaylight.openflowplugin.applications.prioritytaskmgr.api.IPriorityBasedTaskFactory;
import org.opendaylight.reference.app.openflow.FlowProgrammer;
import org.opendaylight.reference.app.openflow.NodeEventListener;
import org.opendaylight.reference.app.subscriber.SubscriberHandler;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ref.app.rev160504.subscriber.list.entries.SubscriberListEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveAction;

/**
 * Created by etaseth on 4/7/2016.
 */
public class TaskFactory implements IPriorityBasedTaskFactory {
    private static Logger LOG = LoggerFactory.getLogger(TaskFactory.class);
    private FlowProgrammer flowProgrammer;

    public TaskFactory(FlowProgrammer flowProgrammer) {
        this.flowProgrammer = flowProgrammer;
    }


    @Override
    public List<RecursiveAction> create(int priority, String entityContext) {
        if(0 == priority){
            //update dpn set because node connected event from Operational DS will be triggered
            // later than EOS based reconciliation event, so add dpn during this call, so that
            // flow will not be missing.
            NodeEventListener.nodeConnected(entityContext);
        }
        List<RecursiveAction> actionList = new ArrayList<>();
        LOG.info("Got create request with priority {} for entityContext {} for subscriber {} ", priority, entityContext,
                SubscriberHandler.getListOfHash().get(priority).size());
        List<SubscriberListEntry> subsList = new ArrayList<>(SubscriberHandler.getListOfHash().get(priority).values());
        RecursiveAction action = new ReconciliationRecursive(subsList, entityContext, flowProgrammer);
        actionList.add(action);
        return actionList;
    }

}
