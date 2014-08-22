/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.adapter;

import org.apache.felix.scr.annotations.*;
import org.opendaylight.driver.base.DefaultFlowMod;
import org.opendaylight.net.driver.DeviceDriverService;
import org.opendaylight.net.facet.FlowModAdjuster;
import org.opendaylight.of.controller.FlowModAdvisor;
import org.opendaylight.of.controller.PostHandshakeCallback;
import org.opendaylight.of.controller.PostHandshakeSink;
import org.opendaylight.of.controller.PostHandshakeTask;
import org.opendaylight.of.controller.pipeline.PipelineDefinition;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.dt.DataPathInfo;
import org.opendaylight.of.lib.mp.MBodyDesc;
import org.opendaylight.of.lib.msg.OfmFlowMod;
import org.opendaylight.util.driver.*;
import org.opendaylight.util.net.IpAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.opendaylight.util.NamedThreadFactory.namedThreads;

/**
 * Auxiliary adapter to integrate OpenFlow controller with device driver
 * framework.
 *
 * @author Thomas Vachuska
 * @author Uyen Chau
 */
@Component(immediate = true)
@Service
public class OpenFlowServiceAdapter implements PostHandshakeSink, FlowModAdvisor {

    private final Logger log = LoggerFactory.getLogger(OpenFlowServiceAdapter.class);
    private static final int WORKER_COUNT = 16;

    private final ExecutorService pool;

    private static final long PHQ_KEEP_ALIVE_MS = 5000;

    private static final String MSG_START_POST_HS = "Starting Post-Handshake: {}";

    private static final String E_DDF = "{FMA} Device driver framework:- ";
    private static final String DBG_INVALID_TASK =
            "Invalidated Post-Handshake task [{}] - not run";

    @Reference(name = "DeviceDriverService", policy = ReferencePolicy.DYNAMIC,
               cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceDriverService driverService;


    // FIXME: need to use ScalingThreadPoolExecutor
    public OpenFlowServiceAdapter() {
        pool = new ThreadPoolExecutor(WORKER_COUNT, WORKER_COUNT * 2,
                                      PHQ_KEEP_ALIVE_MS, TimeUnit.MILLISECONDS,
                                      new LinkedBlockingQueue<Runnable>(), namedThreads("PhQPool"));
    }

    @Override
    public List<OfmFlowMod> getDefaultFlowMods(DataPathInfo dpi,
                                               List<OfmFlowMod> contributedFlows,
                                               PipelineDefinition pipelineDefinition,
                                               boolean isHybrid) {
        DeviceInfo info = driverService.create(dpi.deviceTypeName());
        FlowModAdjuster fma = info.getFacet(FlowModAdjuster.class);

        // If there is no flow mod adjuster, we can't do anything so just
        // return the contributed flows
        if (fma == null)
            return contributedFlows;

        fma.setTableProperties(pipelineDefinition, dpi.negotiated(), isHybrid);

        // Fetch the default flows using the device facet
        List<OfmFlowMod> flows = new ArrayList<>(fma.generateDefaultFlows());

        // Then adjusted any contributed flows per device nuances...
        List<OfmFlowMod> adjusted = adjustContribFlows(dpi, contributedFlows, fma);
        validateFlowMods(adjusted);

        // Append the adjusted contributed flows to any defaults and return.
        flows.addAll(adjusted);
        return flows;
    }

    private void validateFlowMods(List<OfmFlowMod> adjusted) {
        // FIXME: put this back
    }

    @Override
    public List<OfmFlowMod> adjustFlowMod(DataPathInfo dpi, OfmFlowMod fm) {
        List<OfmFlowMod> flowMods = new ArrayList<>();
        flowMods.add(fm);
        return flowMods;
    }

    private List<OfmFlowMod> adjustContribFlows(DataPathInfo dpi, List<OfmFlowMod> mods,
                                                FlowModAdjuster fmf) {
        if (fmf == null)
            return mods;

        // FIXME: iterate through flow mods and adjust them
        return mods;
    }

    @Override
    public PostHandshakeTask doPostHandshake(IpAddress ip, DataPathId dpid,
                                             MBodyDesc desc,
                                             PostHandshakeCallback callback) {
        PostHandshakeTask task = new PostHandshakeTaskImpl(ip, dpid, desc, callback);
        pool.submit(task);
        return task;
    }


    // Auxiliary task for executing device type assignment logic
    private class PostHandshakeTaskImpl implements PostHandshakeTask {
        private final IpAddress ip;
        private final DataPathId dpid;
        private final MBodyDesc desc;
        private final PostHandshakeCallback callback;

        private volatile boolean valid = true;

        public PostHandshakeTaskImpl(IpAddress ip, DataPathId dpid, MBodyDesc desc,
                                     PostHandshakeCallback callback) {
            this.ip = ip;
            this.dpid = dpid;
            this.desc = desc;
            this.callback = callback;
        }

        @Override
        public void run() {
            if (!valid) {
                log.debug(DBG_INVALID_TASK, dpid);
                return;
            }

            // Ask the driver service to resolve the primordial information
            // into a device type name; assumption is that one will be provided
            // always
            String dtn = driverService.
                    getTypeName(desc.getMfrDesc(), desc.getHwDesc(), desc.getSwDesc());
            callback.handshakeComplete(dpid, dtn);
        }

        @Override
        public void invalidate() {
            valid = false;
        }

        @Override
        public boolean isValid() {
            return valid;
        }
    }

}
