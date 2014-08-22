/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.controller.flow.impl;

import org.opendaylight.of.controller.FlowModAdvisor;
import org.opendaylight.of.controller.impl.ListenerService;
import org.opendaylight.of.controller.pipeline.PipelineDefinition;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.dt.DataPathInfo;
import org.opendaylight.of.lib.msg.DataPathMessageFuture;
import org.opendaylight.of.lib.msg.MessageBatchFuture;
import org.opendaylight.of.lib.msg.OfmFlowMod;
import org.opendaylight.util.Log;
import org.opendaylight.util.ValidationException;
import org.slf4j.Logger;

import java.util.List;

import static org.opendaylight.of.lib.msg.MessageBatchFuture.createBatchFuture;

/**
 * Encapsulates the behavior required for creating initial flow rules to be
 * laid down on newly connected datapaths.
 *
 * @author Simon Hunt
 * @author Scott Simes
 */
class InitialFlowUtils {

    private static final String FAILED_TO_ADD =
            "Failed to add initial flow-rule(s) ";
    private static final String E_INIT_FLOW_RULES_DD =
            FAILED_TO_ADD + "[DD NULL FLOWS] {}: {}";
    private static final String E_ZERO_FLOW_RULES_DD =
            FAILED_TO_ADD + "[DD ZERO FLOWS] {}: {}";
    private static final String E_INVALID_FLOW_RULES_DD =
            FAILED_TO_ADD + "[DD INVALID FLOWS] {}: {}: {}";
    private static final String E_UNEXPECTED_DD =
            FAILED_TO_ADD + "[DD UNEXPECTED] {}: {}: {}";

    private static final long INIT_FLOW_RESPONSE_TIMEOUT_MS = 5000;

    private static final String E_FAILED_TO_SEND =
            "[Failed to SEND] {}: {}";

    private final Logger log;
    private final ListenerService ls;
    private final FlowModAdvisor fma;

    InitialFlowUtils(Logger log, ListenerService ls, FlowModAdvisor fma) {
        this.log = log;
        this.ls = ls;
        this.fma = fma;
    }

    /**
     * Gathers and pushes default flows to the specified datapath.
     *
     * @param dpid               target datapath id
     * @param contributedFlows   list of flows from initial flow contributors
     * @param pipelineDefinition device table pipeline definition
     * @param isHybrid           indicates if controller is running in hybrid mode
     * @return true if the operation was a success
     */
    boolean pushDefaultFlows(DataPathId dpid,
                             List<OfmFlowMod> contributedFlows,
                             PipelineDefinition pipelineDefinition,
                             boolean isHybrid) {
        /*
         * NOTE: Anywhere we fail to install the default flows we should
         * consider shutting down the connection to the dpid. TODO: Review this
         */
        DataPathInfo dpi = ls.getDataPathInfo(dpid);
        List<OfmFlowMod> defaultFlows;

        try {
            defaultFlows = fma.getDefaultFlowMods(dpi, contributedFlows,
                                                  pipelineDefinition, isHybrid);
        } catch (ValidationException e) {
            log.error(E_INVALID_FLOW_RULES_DD, dpid, dpi.deviceTypeName(), e);
            return false;
        } catch (Exception e) {
            log.error(E_UNEXPECTED_DD, dpid, dpi.deviceTypeName(), e);
            return false;
        }

        // NOTE: If null is returned, it means there was an issue getting
        //       the default flows from the device driver framework - so check
        //       the log for warnings with "{FMA} Device driver framework:-"
        if (defaultFlows == null) {
            log.error(E_INIT_FLOW_RULES_DD, dpid, dpi.deviceTypeName());
            return false;
        }

        // Shouldn't ever happen, but for robustness...
        if (defaultFlows.isEmpty()) {
            log.error(E_ZERO_FLOW_RULES_DD, dpid, dpi.deviceTypeName());
            return false;
        }

        // NOTE: the flows thus returned are :
        //  (1) Flows contributed by core controller
        //      (default flow miss rules)
        //  (2) Flows contributed by subsystems (e.g. LinkManager)
        //      - already adjusted via device drivers

        // Create a message batch, submit it, and wait for acknowledgement
        MessageBatchFuture batchFuture = createBatchFuture(defaultFlows, dpid);
        return sendMessageBatch(batchFuture) &&
                waitForBarrierReply(batchFuture, "Initial flow-rules");
    }

    // also invoked from FlowTrk.sendConfirmedFlowMod(...) and purgeFlows(...)
    boolean sendMessageBatch(MessageBatchFuture batchFuture) {
        try {
            // first send the flow-mod futures...
            for (DataPathMessageFuture f: batchFuture.getFlowFutures())
                ls.sendFuture(f);
            // then send the barrier-request future
            ls.sendFuture(batchFuture);

        } catch (Exception e) {
            log.error(E_FAILED_TO_SEND, batchFuture.dpid(),
                    Log.stackTraceSnippet(e));
            batchFuture.setFailure(e);
            return false;
        }
        return true;
    }

    private static final String MSG_CONTEXT = "{} [{}] - {}";
    private static final String MSG_CONTEXT_SNIPPET = "{} [{}] - {}: {}";

    boolean waitForBarrierReply(MessageBatchFuture batchFuture, String context) {
        boolean completedSuccessfully = false;
        final DataPathId dpid = batchFuture.dpid();
        boolean doneInTime;

        try {
            doneInTime = batchFuture.await(INIT_FLOW_RESPONSE_TIMEOUT_MS);
            if (doneInTime) {
                // we got a barrier reply from the switch..
                // now we have to double check to see whether thw switch
                // squawked about any of the flowmods.
                List<String> errors = reconcileBatch(batchFuture);
                if (errors.isEmpty()) {
                    log.info(MSG_CONTEXT, context, dpid, "OK");
                    completedSuccessfully = true;
                } else {
                    log.error(MSG_CONTEXT, context, dpid, "Failure...");
                    for (String err : errors)
                        log.error(err);
                }
            } else {
                log.error(MSG_CONTEXT, context, dpid, "Timeout " +
                        INIT_FLOW_RESPONSE_TIMEOUT_MS + "ms");
                failedSendCleanup(batchFuture);
                batchFuture.setFailureTimeout();
            }
        } catch (InterruptedException e) {
            log.error(MSG_CONTEXT_SNIPPET, context, dpid, "Interrupted",
                    Log.stackTraceSnippet(e));
            failedSendCleanup(batchFuture);
            batchFuture.setFailure(e);
        }
        return completedSuccessfully;
    }

    private void failedSendCleanup(MessageBatchFuture batchFuture) {
        // explicit cleanup
        cancelFlowFutures(batchFuture);
        ls.cancelFuture(batchFuture);
    }

    private void cancelFlowFutures(MessageBatchFuture batchFuture) {
        // first, make sure all unsatisfied message futures are cleaned up
        for (DataPathMessageFuture f: batchFuture.getFlowFutures())
            ls.cancelFuture(f);
    }

    // process flow-mod futures to detect errors sent from the datapath.
    // return a list of error strings, suitable for logging.
    private List<String> reconcileBatch(MessageBatchFuture batchFuture) {
        cancelFlowFutures(batchFuture);
        return batchFuture.reconcileFlowFutures();
    }

}
