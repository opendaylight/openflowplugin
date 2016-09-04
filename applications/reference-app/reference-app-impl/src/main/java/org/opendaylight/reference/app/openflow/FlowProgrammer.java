/*
 * Copyright (c) 2016 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.reference.app.openflow;

import com.google.common.base.Preconditions;
import org.opendaylight.reference.app.ReferenceAppMXBean;
import org.opendaylight.openflowplugin.applications.southboundmanager.openflowservice.api.ErrorCallable;
import org.opendaylight.openflowplugin.applications.southboundmanager.openflowservice.api.IOpenflowFacade;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Flow programmer to add/delete flows from switch using southboundmanager.
 * it also provides counter for number of flow added/removed/failed.
 */
public class FlowProgrammer implements ReferenceAppMXBean {

    private static final Logger LOG = LoggerFactory.getLogger(FlowProgrammer.class);
    private final IOpenflowFacade openflowFacade;
    private final FlowCloningHandler flowCloningHandler = new FlowCloningHandler();
    // map used for counter
    private Map<String, AtomicInteger> flowAddCounterMap = new ConcurrentHashMap<>();
    private Map<String, AtomicInteger> flowRemoveCounterMap = new ConcurrentHashMap<>();
    private Map<String, AtomicInteger> flowFailedCounterMap = new ConcurrentHashMap<>();

    private static final String DISABLE_REFERENCE_APP_COUNTER = "disable.reference.app.counter";

    public FlowProgrammer(IOpenflowFacade openflowFacade) {
        this.openflowFacade = openflowFacade;
        Preconditions.checkNotNull(openflowFacade, "IOpenflowFacade Cannot be null!");
    }


    /**
     * add flow in specified dpn
     *
     * @param flow          - flow to be added
     * @param dpId          - add flow to this dp id
     * @param errorCallable - callback in case of any error
     */
    public void addFlow(Flow flow, String dpId, ErrorCallable errorCallable) {
        openflowFacade.modifyFlow(new NodeId(dpId), flow, errorCallable, true);
        incrementCounter(flowAddCounterMap, dpId);
    }

    /**
     * remove flow from specified dpn
     *
     * @param flow          - flow to be removed
     * @param dpId          - remove flow from this dp id
     * @param errorCallable - callback in case of any error
     */
    public void removeFlow(Flow flow, String dpId, ErrorCallable errorCallable) {
        openflowFacade.deleteFlow(new NodeId(dpId), flow, errorCallable, true);
        incrementCounter(flowRemoveCounterMap, dpId);
    }


    /**
     * update flows in all the connected switchs.
     *
     * @param flow  -- flow to be added/removed
     * @param isAdd - boolean to identify add or remove operation
     */
    public void updateFlowInSwitchs(Flow flow, boolean isAdd) {
        for (String switchId : NodeEventListener.getDpnSet()) {
            List<Flow> flowList = flowCloningHandler.cloneFlow(flow);
            for (Flow tempFlow : flowList) {
                ErrorCallable errorCallable;
                if (isAdd) {
                    errorCallable = new FlowErrorCallable(switchId, flow, "add");
                    addFlow(tempFlow, switchId, errorCallable);
                } else {
                    errorCallable = new FlowErrorCallable(switchId, flow, "remove");
                    removeFlow(tempFlow, switchId, errorCallable);
                }
            }
        }
    }


    @Override
    public Set<String> getFlowAddCounter() {
        return getCounterFromMap(flowAddCounterMap);
    }

    @Override
    public Set<String> getFlowRemoveCounter() {
        return getCounterFromMap(flowRemoveCounterMap);
    }

    @Override
    public Set<String> getFlowFailedCounter() {
        return getCounterFromMap(flowFailedCounterMap);
    }

    @Override
    public void resetCounters() {
        flowAddCounterMap.clear();
        flowRemoveCounterMap.clear();
        flowFailedCounterMap.clear();
    }

    private Set<String> getCounterFromMap(Map<String, AtomicInteger> map) {
        Set<String> counterSet = new HashSet<>();
        for (Map.Entry<String, AtomicInteger> entry : map.entrySet()) {
            StringBuffer result = new StringBuffer();
            result.append(entry.getKey()).append(" = ").append(entry.getValue().toString());
            counterSet.add(result.toString());
        }
        return counterSet;
    }

    private void incrementCounter(Map<String, AtomicInteger> map, String dpId) {
        if (Boolean.getBoolean(DISABLE_REFERENCE_APP_COUNTER)) {
            return;
        }
        AtomicInteger value = map.get(dpId);
        if (null != value) {
            value.incrementAndGet();
        } else {
            value = new AtomicInteger(1);
            map.put(dpId, value);
        }
    }

    /**
     * ErrorCallable to handle the error in case flow provisioning
     * is failed.
     */
    private class FlowErrorCallable extends ErrorCallable {

        private final String dpId;
        private final Flow flow;
        private final String operation;

        public FlowErrorCallable(String dpId, Flow flow, String operation) {
            this.dpId = dpId;
            this.flow = flow;
            this.operation = operation;
        }

        @Override
        public Object call() throws Exception {
            LOG.error("{} flow failed with error {} in {} for flow {}.", operation, this.getCause(), dpId, flow);
            incrementCounter(flowFailedCounterMap, dpId);
            return null;
        }
    }

}