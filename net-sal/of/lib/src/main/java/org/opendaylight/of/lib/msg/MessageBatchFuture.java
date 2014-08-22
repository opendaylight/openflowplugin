/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.lib.msg;

import org.opendaylight.of.lib.CommonUtils;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.opendaylight.of.lib.CommonUtils.notNull;
import static org.opendaylight.of.lib.msg.MessageType.BARRIER_REQUEST;
import static org.opendaylight.util.StringUtils.EOL;

/**
 * A {@link MessageFuture future} for a batch of flow-mod messages sent to
 * a specific datapath. Instances are created with the static method:
 * <pre>
 * List&lt;OfmFlowMod&gt; flows = ...;
 * DataPathId dpid = ...;
 * MessageBatchFuture batchFuture =
 *         MessageBatchFuture.createBatchFuture(flows, dpid);
 * </pre>
 * A barrier request is generated on the caller's behalf and assigned as
 * the principal message. Waiting on the batchFuture is the same as waiting
 * on the future for the barrier request. Once it is satisfied (and the waiter
 * unblocked), the result for the batch will reflect the aggregate state of
 * all messages associated with the batch.
 *
 * @author Simon Hunt
 */
public class MessageBatchFuture extends DataPathMessageFuture {
    private static final String E_NO_FLOWS = "no flow-mods submitted";
    private static final String FMT_PROBLEM =
            "Failed Msg Future: [dpid={},xid={},req={},problem={}]";
    private static final String EOLI = EOL + "  ";
    private static final String OP = " (";
    private static final String CP = ")";
    private static final String AGG_RES = EOL + "Aggregate Result => ";

    private final List<DataPathMessageFuture> flowFutures;
    private final List<DataPathMessageFuture> failedFutures = new ArrayList<>();
    private final List<String> errors = new ArrayList<>();
    private Result aggregateResult = Result.UNSATISFIED;

    /**
     * Constructs a message-batch future for the given batch of messages,
     * a newly created barrier request, and the specified datapath.
     *
     * @param flows the flows to send
     * @param barrier the barrier message for acknowledgement
     * @param dpid the target datapath
     */
    private MessageBatchFuture(DataPathId dpid, List<OfmFlowMod> flows,
                               OfmBarrierRequest barrier) {
        super(barrier, dpid);

        flowFutures = new ArrayList<>(flows.size());
        for (OfmFlowMod f: flows)
            flowFutures.add(new DataPathMessageFuture(f, dpid));
    }

    @Override
    public synchronized Result result() {
        return aggregateResult;
    }

    @Override
    protected void satisfied() {
        // make sure we have reconciled
        reconcileFlowFutures();
    }

    /**
     * Returns the futures for the flow mods associated with this batch.
     *
     * @return the flow mod futures
     */
    public List<DataPathMessageFuture> getFlowFutures() {
        return Collections.unmodifiableList(flowFutures);
    }

    /**
     * Returns the futures for the flow mods that failed.
     *
     * @return the failed flow mod futures
     */
    public List<DataPathMessageFuture> getFailedFutures() {
        return Collections.unmodifiableList(failedFutures);
    }

    /**
     * Returns a multi-line string representation of this batch of
     * futures.
     *
     * @return a multi-line string representation
     */
    public String toDebugString() {
        StringBuilder sb = new StringBuilder(super.toString());
        sb.append(OP).append(request()).append(CP);
        for (MessageFuture f: getFlowFutures())
            sb.append(EOLI).append(f).append(OP).append(f.request()).append(CP);
        sb.append(AGG_RES).append(aggregateResult);
        return sb.toString();
    }

    /**
     * Invoked by the caller once they have confirmation that the barrier
     * request has been satisfied with a barrier response. Note that this
     * method should only be invoked once; subsequent invocations will have
     * no effect.
     * <p>
     * This method processes the flow-mod futures and generates an error string
     * (suitable for logging) for each failed future. If all went well, an
     * empty list will be returned, indicating no errors.
     * <p>
     * This method also sets the aggregate result for the batch.
     *
     * @return the list of errors, if any
     */
    public synchronized List<String> reconcileFlowFutures() {
        if (aggregateResult != Result.UNSATISFIED)
            return errors;

        Result worstFound = Result.SUCCESS;
        for (DataPathMessageFuture f: flowFutures) {
            final Result r = f.result();

            if (r.isFailure()) {
                errors.add(StringUtils.format(FMT_PROBLEM, f.dpid(), f.xid(),
                        f.request(), f.problemString()));
                failedFutures.add(f);

                if (r == Result.EXCEPTION)
                    worstFound = Result.EXCEPTION;
                else if (worstFound != Result.EXCEPTION)
                    worstFound = Result.OFM_ERROR;
            }
        }
        aggregateResult = worstFound;
        return errors;
    }


    /**
     * Creates and returns a message batch future for the given messages
     * destined for the specified datapath. Note that a barrier request is
     * generated on the caller's behalf, and added to the set of messages
     * to be submitted.
     *
     * @param flows the batch of flows to send
     * @param dpid the target datapath
     * @return the batch message future
     * @throws NullPointerException if either parameter is null
     * @throws IllegalArgumentException if flows list is empty
     */
    public static MessageBatchFuture createBatchFuture(List<OfmFlowMod> flows,
                                                       DataPathId dpid) {
        notNull(flows, dpid);
        if (flows.isEmpty())
            throw new IllegalArgumentException(E_NO_FLOWS);
        for (OfmFlowMod fm: flows)
            CommonUtils.notMutable(fm);

        // assumption is that flow PV is correct
        final ProtocolVersion pv = flows.get(0).getVersion();
        return new MessageBatchFuture(dpid, flows, barrier(pv));
    }

    private static OfmBarrierRequest barrier(ProtocolVersion pv) {
        return (OfmBarrierRequest)
                MessageFactory.create(pv, BARRIER_REQUEST).toImmutable();
    }

}
