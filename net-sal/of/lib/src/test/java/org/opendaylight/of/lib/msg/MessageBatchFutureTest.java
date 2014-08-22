/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.lib.msg;

import org.junit.Test;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.dt.TableId;
import org.opendaylight.of.lib.err.ECodeFlowModFailed;
import org.opendaylight.of.lib.err.ErrorType;
import org.opendaylight.of.lib.match.Match;
import org.opendaylight.of.lib.match.MatchFactory;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.dt.DataPathId.dpid;
import static org.opendaylight.of.lib.msg.MessageBatchFuture.createBatchFuture;
import static org.opendaylight.of.lib.msg.MessageFactory.create;
import static org.opendaylight.of.lib.msg.MessageFuture.Result;
import static org.opendaylight.of.lib.msg.MessageType.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for {@link MessageBatchFuture}.
 *
 * @author Simon Hunt
 */
public class MessageBatchFutureTest {

    private static final DataPathId DPID = dpid("1/234234:786999");
    private static final int FLOW_COUNT = 3;

    private MessageBatchFuture batch;
    private List<String> problems;


    private Match createMatch(ProtocolVersion pv) {
        return (Match) MatchFactory.createMatch(pv).toImmutable();
    }

    private OfmMutableFlowMod createMutableFlowMod(ProtocolVersion pv,
                                                   long cookie) {
        OfmMutableFlowMod fm = (OfmMutableFlowMod)
                create(pv, FLOW_MOD, FlowModCommand.ADD);
        fm.match(createMatch(pv)).cookie(cookie);
        if (pv.gt(ProtocolVersion.V_1_0))
            fm.tableId(TableId.valueOf(42));
        return fm;
    }

    private OfmFlowMod createFlowMod(ProtocolVersion pv, long cookie) {
        return (OfmFlowMod) createMutableFlowMod(pv, cookie).toImmutable();
    }

    private List<OfmFlowMod> createFlowList(ProtocolVersion pv) {
        List<OfmFlowMod> flows = new ArrayList<>(FLOW_COUNT);
        for (int i=0; i<FLOW_COUNT; i++) {
            long cookie = 0x1 << i;
            flows.add(createFlowMod(pv, cookie));
        }
        return flows;
    }

    private OfmBarrierReply barrierReply(OfmBarrierRequest request) {
        return (OfmBarrierReply) MessageFactory.create(request, BARRIER_REPLY)
                .toImmutable();
    }

    @Test
    public void basic() {
        print(EOL + "basic()");
        batch = createBatchFuture(createFlowList(V_1_3), DPID);
        print(batch.toDebugString());
        assertEquals(AM_UXS, 3, batch.getFlowFutures().size());
        assertEquals(AM_UXS, 0, batch.getFailedFutures().size());
        assertEquals(AM_NEQ, MessageType.BARRIER_REQUEST,
                batch.request().getType());
        assertEquals(AM_NEQ, Result.UNSATISFIED, batch.result());
    }

    @Test(expected = NullPointerException.class)
    public void nullList() {
        createBatchFuture(null, DPID);
    }

    @Test(expected = NullPointerException.class)
    public void nullDpid() {
        createBatchFuture(createFlowList(V_1_3), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void noFlows() {
        createBatchFuture(new ArrayList<OfmFlowMod>(), DPID);
    }

    @Test(expected = IllegalArgumentException.class)
    public void mutableFlow() {
        List<OfmFlowMod> flows = createFlowList(V_1_3);
        flows.add(createMutableFlowMod(V_1_3, 0xff));
        createBatchFuture(flows, DPID);
    }

    @Test
    public void reconcileBasic() {
        print(EOL + "reconcileBasic()");
        batch = createBatchFuture(createFlowList(V_1_3), DPID);
        // let's pretend the barrier was satisfied:
        batch.setSuccess(barrierReply((OfmBarrierRequest) batch.request()));
        problems = batch.reconcileFlowFutures();
        print(batch.toDebugString());
        assertEquals(AM_NEQ, Result.SUCCESS, batch.result());
        assertEquals(AM_UXS, 3, batch.getFlowFutures().size());
        assertEquals(AM_UXS, 0, batch.getFailedFutures().size());
        assertEquals(AM_UXS, 0, problems.size());
        checkFlowFutures(batch,
                Result.UNSATISFIED, Result.UNSATISFIED, Result.UNSATISFIED);
    }

    private void checkFlowFutures(MessageBatchFuture batch, Result... expRes) {
        List<DataPathMessageFuture> flowFutures = batch.getFlowFutures();
        assertEquals(AM_UXS, expRes.length, flowFutures.size());
        for (int i=0; i<expRes.length; i++)
            assertEquals(AM_NEQ, expRes[i], flowFutures.get(i).result());
    }

    private void failFlow(DataPathMessageFuture f) {
        OfmMutableError error =
                (OfmMutableError) MessageFactory.create(f.request(), ERROR,
                        ErrorType.FLOW_MOD_FAILED);
        error.errorCode(ECodeFlowModFailed.TABLE_FULL);
        f.setFailure((OfmError) error.toImmutable());
    }

    private void failFlowEx(DataPathMessageFuture f) {
        RuntimeException ex = new RuntimeException("Bad Send or something");
        f.setFailure(ex);
    }

    @Test
    public void reconcileOfmError() {
        print(EOL + "reconcileOfmError()");
        batch = createBatchFuture(createFlowList(V_1_3), DPID);
        // let's fail the second flow before satisfying the barrier
        failFlow(batch.getFlowFutures().get(1));
        // let's pretend the barrier was satisfied:
        batch.setSuccess(barrierReply((OfmBarrierRequest) batch.request()));
        problems = batch.reconcileFlowFutures();
        print(batch.toDebugString());
        assertEquals(AM_NEQ, Result.OFM_ERROR, batch.result());
        assertEquals(AM_UXS, 3, batch.getFlowFutures().size());
        assertEquals(AM_UXS, 1, batch.getFailedFutures().size());
        assertEquals(AM_UXS, 1, problems.size());
        print(EOL + "Problems....");
        for (String s: problems)
            print("  >> {}", s);
        checkFlowFutures(batch,
                Result.UNSATISFIED, Result.OFM_ERROR, Result.UNSATISFIED);
    }

    @Test
    public void reconcileException() {
        print(EOL + "reconcileException()");
        batch = createBatchFuture(createFlowList(V_1_3), DPID);
        // let's fail the second and third flows
        failFlowEx(batch.getFlowFutures().get(1));
        failFlow(batch.getFlowFutures().get(2));
        // let's pretend the barrier was satisfied:
        batch.setSuccess(barrierReply((OfmBarrierRequest) batch.request()));
        problems = batch.reconcileFlowFutures();
        print(batch.toDebugString());
        assertEquals(AM_NEQ, Result.EXCEPTION, batch.result());
        assertEquals(AM_UXS, 3, batch.getFlowFutures().size());
        assertEquals(AM_UXS, 2, batch.getFailedFutures().size());
        assertEquals(AM_UXS, 2, problems.size());
        print(EOL + "Problems....");
        for (String s: problems)
            print("  >> {}", s);
        checkFlowFutures(batch,
                Result.UNSATISFIED, Result.EXCEPTION, Result.OFM_ERROR);
    }

}
