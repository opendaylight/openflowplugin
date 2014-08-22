/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.controller.flow.impl;

import org.apache.commons.lang.math.RandomUtils;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.of.controller.FlowModAdvisor;
import org.opendaylight.of.controller.impl.ListenerServiceAdapter;
import org.opendaylight.of.controller.pipeline.PipelineDefinition;
import org.opendaylight.of.lib.OpenflowException;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.dt.DataPathInfo;
import org.opendaylight.of.lib.dt.DataPathInfoAdapter;
import org.opendaylight.of.lib.err.ECodeFlowModFailed;
import org.opendaylight.of.lib.err.ErrorType;
import org.opendaylight.of.lib.msg.*;
import org.opendaylight.util.StringUtils;
import org.opendaylight.util.ValidationException;
import org.opendaylight.util.junit.TestLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static org.junit.Assert.*;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.dt.DataPathId.dpid;
import static org.opendaylight.of.lib.msg.MessageType.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for {@link InitialFlowUtils}.
 *
 * @author Simon Hunt
 */
public class InitialFlowUtilsTest {
    private static final String DEVICE_TYPE = "FakeDP";
    private static final ProtocolVersion PV = V_1_3;
    private static final DataPathId DPID = dpid("1/123456abcdef");
    private static final TestLogger tlog = new TestLogger();

    private static class MyDpi extends DataPathInfoAdapter {
        @Override public String deviceTypeName() { return DEVICE_TYPE; }
        @Override public DataPathId dpid() { return DPID; }
        @Override public ProtocolVersion negotiated() { return PV; }
    }

    private static final String FTA = "Failed to add initial flow-rule(s) ";
    private static final String E_MSG = "Badness Happened";

    private static enum Behavior {
        NULL_FLOWS, INVALID_FLOW, UNKNOWN,
        SEND_ERROR, SEND_TIMEOUT,
        ZERO_FLOWS, ONE_FLOW, FAILED_FLOW, FAIL_ALL_FLOWS,
    }

    // ==== FIXTURE to generate default flows ...
    private class FakeAdvisor implements FlowModAdvisor {
        private final List<String> msgs = new ArrayList<>();

        @Override
        public List<OfmFlowMod> getDefaultFlowMods(DataPathInfo dpi,
                                                   List<OfmFlowMod> contributedFlows,
                                                   PipelineDefinition pipelineDefinition,
                                                   boolean isHybrid) {
            switch (behavior) {
                case NULL_FLOWS:
                    return null;
                case INVALID_FLOW:
                    throw new ValidationException(E_MSG, msgs);
                case UNKNOWN:
                    throw new RuntimeException(E_MSG);
                case ZERO_FLOWS:
                    return createGoodFlows(0);

                case ONE_FLOW:
                case SEND_ERROR:
                case SEND_TIMEOUT:
                    return createGoodFlows(1);

                case FAILED_FLOW:
                case FAIL_ALL_FLOWS:
                    return createGoodFlows(3);

                // just in case...
                default:
                    print("PROG-ERR: missing behavior case: " + behavior);
            }
            return null;
        }

        private List<OfmFlowMod> createGoodFlows(int num) {
            List<OfmFlowMod> flows = new ArrayList<>();
            if (num > 0) {
                for (int i=0; i<num; i++)
                    flows.add(createAFlow());
            }
            return flows;
        }

        private OfmFlowMod createAFlow() {
            OfmMutableFlowMod fm = (OfmMutableFlowMod)
                    MessageFactory.create(PV, FLOW_MOD, FlowModCommand.ADD);
            return (OfmFlowMod) fm.toImmutable();
        }

        @Override
        public List<OfmFlowMod> adjustFlowMod(DataPathInfo dpi, OfmFlowMod fm) {
            return null;
        }
    }

    // ==== FIXTURE to mock the listener service ...
    private class FakeService extends ListenerServiceAdapter {
        private final Map<Long, MessageFuture> futureCache = new HashMap<>();
        private final ScheduledExecutorService sched =
                Executors.newSingleThreadScheduledExecutor();
        private MessageBatchFuture batchBeingTested;

        @Override
        public DataPathInfo getDataPathInfo(DataPathId dpid) {
            return new MyDpi();
        }

        @Override
        public void sendFuture(DataPathMessageFuture f)
                throws OpenflowException {
            // capture the batch future so we can pass a ref to the test
            if (f instanceof MessageBatchFuture)
                batchBeingTested = (MessageBatchFuture) f;

            // emulate controller service future caching...
            futureCache.put(f.xid(), f);

            switch (behavior) {
                case ONE_FLOW:
                case FAILED_FLOW:
                case FAIL_ALL_FLOWS:
                    scheduleReply(f, behavior);
                    break;
                case SEND_ERROR:
                    throw new RuntimeException(E_MSG);
                case SEND_TIMEOUT:
                    // nothing to do but wait...
                    break;
                default:
                    print("PROG_ERR: LS.sendFuture() - missing case: " + behavior);
            }
        }

        @Override
        public void cancelFuture(DataPathMessageFuture f) {
            futureCache.remove(f.xid());
        }

        private void scheduleReply(final DataPathMessageFuture f, Behavior b) {
            if (f.request().getType() == BARRIER_REQUEST) {
                if (b == Behavior.FAILED_FLOW)
                    findAndFailAFlow((MessageBatchFuture) f);
                else if (b == Behavior.FAIL_ALL_FLOWS)
                    findAndfailAllFlows((MessageBatchFuture) f);
                smallDelay(new Runnable() {
                    @Override
                    public void run() {
                        long xid = f.xid();
                        futureCache.remove(xid);
                        f.setSuccess(makeBarrierReply(f.request()));
                    }
                });
            }
        }

        private void smallDelay(Runnable task) {
            sched.schedule(task, 100, TimeUnit.MILLISECONDS);
        }

        private void findAndfailAllFlows(MessageBatchFuture batchFuture) {
            List<DataPathMessageFuture> futures = batchFuture.getFlowFutures();
            for (int i=0,n=futures.size(); i<n; i++)
                failIndexedFlowFuture(futures, i);
        }

        private void findAndFailAFlow(MessageBatchFuture batchFuture) {
            List<DataPathMessageFuture> futures = batchFuture.getFlowFutures();
            int index = RandomUtils.nextInt(futures.size());
            failIndexedFlowFuture(futures, index);
        }

        private void failIndexedFlowFuture(List<DataPathMessageFuture> futures,
                                           int index) {
            DataPathMessageFuture selected = futures.get(index);
            OfmError error = createError(selected.request());
            futureCache.remove(selected.xid());
            selected.setFailure(error);
        }

        private OfmError createError(OpenflowMessage request) {
            OfmMutableError err = (OfmMutableError)
                    MessageFactory.create(request, ERROR);
            err.errorType(ErrorType.FLOW_MOD_FAILED)
                    .errorCode(ECodeFlowModFailed.TABLE_FULL);
            return (OfmError) err.toImmutable();
        }

        private OpenflowMessage makeBarrierReply(OpenflowMessage request) {
            return MessageFactory.create(request, BARRIER_REPLY).toImmutable();
        }

        private void assertEmptyFutureCache() {
            assertTrue("Future Cache not empty", futureCache.isEmpty());
        }

        private MessageBatchFuture batchUnderTest() {
            return batchBeingTested;
        }
    }

    private final ExecutorService exec = Executors.newSingleThreadExecutor();

    private FakeService ls;
    private FakeAdvisor fma;
    private InitialFlowUtils utils;
    private Behavior behavior;
    private boolean flowsPushedOk;

    private String fmt(String fmt, Object... items) {
        return StringUtils.format(fmt, items);
    }

    private void verifyError(String fmt, Object... items) {
        tlog.assertErrorContains(fmt(fmt, items));
    }

    private void verifyInfo(String fmt, Object... items) {
        tlog.assertInfoContains(fmt(fmt, items));
    }

    private static final long LATCH_MAX_MS = 3000;

    private void waitForLatch(CountDownLatch latch) {
        try {
            latch.await(LATCH_MAX_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("time-out waiting for latch");
        }
    }

    @Before
    public void setUp() {
        ls = new FakeService();
        fma = new FakeAdvisor();
        utils = new InitialFlowUtils(tlog, ls, fma);
    }

    @Test
    public void nullFlows() {
        behavior = Behavior.NULL_FLOWS;
        flowsPushedOk = utils.pushDefaultFlows(DPID, null, null, false);
        verifyError(FTA + "[DD NULL FLOWS] {}: {}", DPID, DEVICE_TYPE);
        assertFalse(AM_HUH, flowsPushedOk);
    }

    @Test
    public void invalidFlows() {
        behavior = Behavior.INVALID_FLOW;
        flowsPushedOk = utils.pushDefaultFlows(DPID, null, null, false);
        verifyError(FTA + "[DD INVALID FLOWS] {}: {}:", DPID, DEVICE_TYPE);
        assertFalse(AM_HUH, flowsPushedOk);
    }

    @Test
    public void unknownError() {
        behavior = Behavior.UNKNOWN;
        flowsPushedOk = utils.pushDefaultFlows(DPID, null, null, false);
        verifyError(FTA + "[DD UNEXPECTED] {}: {}:", DPID, DEVICE_TYPE);
        assertFalse(AM_HUH, flowsPushedOk);
    }


    @Test
    public void sendError() {
        behavior = Behavior.SEND_ERROR;
        flowsPushedOk = utils.pushDefaultFlows(DPID, null, null, false);
        verifyError("[Failed to SEND] {}", DPID);
        assertFalse(AM_HUH, flowsPushedOk);
    }

    @Test
    public void sendTimeout() {
        behavior = Behavior.SEND_TIMEOUT;
        flowsPushedOk = utils.pushDefaultFlows(DPID, null, null, false);
        verifyError("Initial flow-rules [{}] - Timeout 5000ms", DPID);
        assertFalse(AM_HUH, flowsPushedOk);
    }

    @Test
    public void zeroFlows() {
        behavior = Behavior.ZERO_FLOWS;
        flowsPushedOk = utils.pushDefaultFlows(DPID, null, null, false);
        verifyError(FTA + "[DD ZERO FLOWS] {}: {}", DPID, DEVICE_TYPE);
        assertFalse(AM_HUH, flowsPushedOk);
    }

    @Test
    public void oneFlow() {
        behavior = Behavior.ONE_FLOW;
        final CountDownLatch latch = new CountDownLatch(1);

        exec.submit(new Runnable() {
            @Override
            public void run() {
                flowsPushedOk = utils.pushDefaultFlows(DPID, null, null, false);
                latch.countDown();
            }
        });
        waitForLatch(latch);

        verifyInfo("Initial flow-rules [{}] - OK", DPID);
        assertTrue(AM_HUH, flowsPushedOk);
        ls.assertEmptyFutureCache();
    }

    @Test
    public void failedFlow() {
        behavior = Behavior.FAILED_FLOW;
        final CountDownLatch latch = new CountDownLatch(1);

        exec.submit(new Runnable() {
            @Override
            public void run() {
                flowsPushedOk = utils.pushDefaultFlows(DPID, null, null, false);
                latch.countDown();
            }
        });
        waitForLatch(latch);
        verifyError("FLOW_MOD_FAILED/TABLE_FULL");
        assertFalse(AM_HUH, flowsPushedOk);
        ls.assertEmptyFutureCache();

        // confirm state of batch future
        MessageBatchFuture batch = ls.batchUnderTest();
        print(batch.toDebugString());
        assertEquals(AM_NEQ, MessageFuture.Result.OFM_ERROR, batch.result());
        assertEquals(AM_UXS, 1, batch.reconcileFlowFutures().size());
    }

    @Test
    public void failAllFlows() {
        behavior = Behavior.FAIL_ALL_FLOWS;
        final CountDownLatch latch = new CountDownLatch(1);

        exec.submit(new Runnable() {
            @Override
            public void run() {
                flowsPushedOk = utils.pushDefaultFlows(DPID, null, null, false);
                latch.countDown();
            }
        });
        waitForLatch(latch);
        verifyError("FLOW_MOD_FAILED/TABLE_FULL");
        assertFalse(AM_HUH, flowsPushedOk);
        ls.assertEmptyFutureCache();

        // confirm state of batch future
        MessageBatchFuture batch = ls.batchUnderTest();
        print(batch.toDebugString());
        assertEquals(AM_NEQ, MessageFuture.Result.OFM_ERROR, batch.result());
        assertEquals(AM_UXS, 3, batch.reconcileFlowFutures().size());
    }

}
