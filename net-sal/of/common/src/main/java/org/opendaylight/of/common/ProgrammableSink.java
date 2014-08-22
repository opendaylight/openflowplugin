/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.common;

import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.msg.MessageType;
import org.opendaylight.of.lib.msg.OpenflowMessage;
import org.opendaylight.util.ResourceUtils;
import org.opendaylight.util.net.IpAddress;

import java.util.*;

import static org.opendaylight.util.StringUtils.EOL;

/**
 * A utility implementation of the {@link MessageSink} interface which can
 * be used in unit tests of OpenFlow message interactions.
 * <p>
 *
 * @author Simon Hunt
 * @author Scott Simes
 */
public class ProgrammableSink implements MessageSink {

    private static final String ARROW = " => ";
    private static final String COLON = ": ";

    private static final ResourceBundle RES = ResourceUtils.getBundledResource(
            ProgrammableSink.class, "programmableSink");

    private static final String E_SINK_NOT_READY = RES
            .getString("e_sink_not_ready");
    private static final String E_REPLAY_MODE = RES.getString("e_replay_mode");
    private static final String E_UNEX_DPID = RES.getString("e_unex_dpid");
    private static final String E_UNEX_MSG = RES.getString("e_unex_msg");
    private static final String E_UNEX_TYPE = RES.getString("e_unex_type");
    private static final String E_UNEX_XID = RES.getString("e_unex_xid");
    private static final String E_UNMET = RES.getString("e_unmet");

    /** Strict mode means that unexpected messages will cause test failure. */
    private boolean strict = true;

    /** Our default dpid. */
    private final DataPathId defaultDpid;

    /** The programmed expectations. */
    private Map<DataPathId, List<Expectation>> expects;

    /** The currently running replay iterators. */
    private Map<DataPathId, Iterator<Expectation>> iterators;

    /** Running lists of expected transaction ids. */
    private Map<DataPathId, List<Long>> xids;

    /** Accumulated lists of assertion errors. */
    private Map<DataPathId, List<String>> assertErrs;

    /** Synchronization lock for the maps. */
    private final Object mapLock = new Object();

    /** Constructs a programmable message sink.
     * The datapath id given here is used in all method calls that
     * do not explicitly specify a datapath id. This can be null, as long
     * as each expectation is added with an {@code expect(...)} method that
     * explicitly specifies the datapath from which the message is expected.
     *
     * @param defaultDpid the default dpid
     */
    public ProgrammableSink(DataPathId defaultDpid) {
        this.defaultDpid = defaultDpid;
        expects = new TreeMap<DataPathId, List<Expectation>>();
    }

    /** Adds the expectation that the next message is of the specified type,
     * (and expected from the default datapath).
     *
     * @param type the expected message type
     */
    public void expect(MessageType type) {
        addExpectation(defaultDpid, new Expectation(type));
    }

    /** Adds the expectation that the next message is of the specified type.
     *
     * @param dpid the datapath from which the message is expected
     * @param type the expected message type
     */
    public void expect(DataPathId dpid, MessageType type) {
        addExpectation(dpid, new Expectation(type));
    }

    /** Adds the expectation that the next message is of the specified type,
     * and that the assertions embodied in the message assertor, when run
     * against the message, are all true, (and expected from the default
     * datapath).
     *
     * @param type the expected message type
     * @param ma the message assertor
     */
    public void expect(MessageType type, AbstractMsgAssertor ma) {
        addExpectation(defaultDpid, new Expectation(type, ma));
    }

    /** Adds the expectation that the next message is of the specified type,
     * and that the assertions embodied in the message assertor, when run
     * against the message, are all true.
     *
     * @param dpid the datapath from which the message is expected
     * @param type the expected message type
     * @param ma the message assertor
     */
    public void expect(DataPathId dpid, MessageType type, AbstractMsgAssertor ma) {
        addExpectation(dpid, new Expectation(type, ma));
    }

    /** Adds the given expectation to the list for the given datapath id.
     *
     * @param dpid the datapath id
     * @param expectation the expectation to add
     */
    private void addExpectation(DataPathId dpid, Expectation expectation) {
        if (dpid == null || expectation == null)
            throw new NullPointerException("null parameter(s)");
        synchronized (mapLock) {
            if (iterators != null)
                throw new IllegalStateException(E_REPLAY_MODE);

            List<Expectation> exps = expects.get(dpid);
            if (exps == null) {
                exps = new ArrayList<Expectation>();
                expects.put(dpid, exps);
            }
            exps.add(expectation);
        } // sync
    }

    /** Switches the sink into replay mode. */
    public void replay() {
        synchronized (mapLock) {
            if (iterators != null)
                throw new IllegalStateException(E_REPLAY_MODE);

            iterators = new TreeMap<DataPathId, Iterator<Expectation>>();
            xids = new TreeMap<DataPathId, List<Long>>();
            assertErrs = new TreeMap<DataPathId, List<String>>();
            for (Map.Entry<DataPathId, List<Expectation>> e: expects.entrySet()) {
                DataPathId dpid = e.getKey();
                iterators.put(dpid, e.getValue().iterator());
                xids.put(dpid, new ArrayList<Long>());
                assertErrs.put(dpid, new ArrayList<String>());
            }
        } // sync
    }

    /** Ends the replay. Throws an AssertionError if there are
     * unmet expectations remaining.
     */
    public void endReplay() {
        synchronized (mapLock) {
            if (iterators == null)
                throw new IllegalStateException(E_SINK_NOT_READY);

            StringBuilder sb = new StringBuilder();
            for (Map.Entry<DataPathId, List<String>> e:
                    assertErrs.entrySet()) {
                DataPathId dpid = e.getKey();
                List<String> errors = e.getValue();
                for (String s: errors)
                    sb.append(EOL).append(dpid).append(ARROW).append(s);
            }

            for (Map.Entry<DataPathId, Iterator<Expectation>> e:
                    iterators.entrySet()) {
                DataPathId dpid = e.getKey();
                Iterator<Expectation> iter = e.getValue();
                while (iter.hasNext()) {
                    Expectation exp = iter.next();
                    String name = exp.expAssertions == null
                            ? exp.expType.toString()
                            : exp.expAssertions.getName();
                    sb.append(EOL).append(dpid).append(ARROW)
                            .append(E_UNMET).append(name);
                }
            }
            if (sb.length() > 0)
                throw new AssertionError(sb);
        } // sync
    }


    /** {@inheritDoc}
     * <p>
     * This default implementation does nothing.
     *
     * @param dpid the datapath id
     * @param negotiated the negotiated version
     * @param ip the IP address of the switch
     */
    @Override
    public void dataPathAdded(DataPathId dpid, ProtocolVersion negotiated,
                              IpAddress ip) { }

    /** {@inheritDoc}
     * <p>
     * This default implementation does nothing.
     *
     * @param dpid the datapath id
     * @param negotiated the negotiated version
     * @param ip the IP address of the switch
     */
    @Override
    public void dataPathRemoved(DataPathId dpid, ProtocolVersion negotiated,
                                IpAddress ip) { }

    /** {@inheritDoc}
     * <p>
     * This default implementation does nothing.
     *
     * @param dpid the datapath id
     * @param negotiated the negotiated version
     * @param ip the IP address of the switch
     */
    @Override
    public void dataPathRevoked(DataPathId dpid, ProtocolVersion negotiated,
                                IpAddress ip) { }

    /** Incoming OpenFlow messages are routed through this API method.
     * Note that this is called from another thread (not the main test
     * thread), so we need to just accumulate assertion failures, not
     * throw them.
     *
     * @param msg the OpenFlow message
     * @param dpid the source datapath ID
     * @param auxId the auxiliary connection ID
     * @param negotiated the negotiated version
     */
    @Override
    public void msgRx(OpenflowMessage msg, DataPathId dpid, int auxId,
                      ProtocolVersion negotiated) {
        synchronized (mapLock) {
            if (iterators == null)
                throw new IllegalStateException(E_SINK_NOT_READY);

            List<String> errors = assertErrs.get(dpid);
            Iterator<Expectation> iter = iterators.get(dpid);
            if (!strict && iter == null)
                return;

            if (strict && iter == null) {
                errors.add(err(E_UNEX_DPID, dpid, msg));
                return;
            }

            if (!strict && !iter.hasNext())
                return;

            if (strict && !iter.hasNext()) {
                errors.add(err(E_UNEX_MSG, dpid, msg));
                return;
            }

            // see if we need to match xid...
            long expXid = 0;
            List<Long> expXids = xids.get(dpid);
            if (expXids != null && expXids.size() > 0)
                expXid = expXids.remove(0);

            // we have an expectation to meet...
            Expectation exp = iter.next();
            if (msg.getType() != exp.expType)
                errors.add(err(E_UNEX_TYPE, dpid, msg));
            if (expXid > 0 && msg.getXid() != expXid)
                errors.add(err(E_UNEX_XID + expXid, dpid, msg));
            if (exp.expAssertions != null)
                exp.expAssertions.runAssertions(msg);
        }
    }

    /** Convenience method for creating an assertion error.
     *
     * @param s the message prefix string
     * @param id the datapath id
     * @param msg the openflow message
     * @return an assertion error, ready to throw
     */
    private String err(String s, DataPathId id, OpenflowMessage msg) {
        return s + COLON + id + ARROW + msg;
    }

    /** Adds an assertion error to the accumulation list for the given
     * datapath.
     *
     * @param dpid the datapath id
     * @param context list of error context messages
     * @param msg the error message
     */
    public void addError(DataPathId dpid, List<String> context, String msg) {
        synchronized (mapLock) {
            List<String> errors = assertErrs.get(dpid);
            if (errors != null) {
                errors.add(msg);
                errors.addAll(context);
            }
        }
    }

    /** Adds an assertion error to the accumulation list for the given
     * datapath.
     *
     * @param dpid the datapath id
     * @param context list of error context messages
     * @param msg the error message
     * @param exp the expected value
     * @param act the actual value
     */
    public void addError(DataPathId dpid, List<String> context, String msg,
                         Object exp, Object act) {
        synchronized (mapLock) {
            List<String> errors = assertErrs.get(dpid);
            if (errors != null) {
                errors.add(msg + EXP + exp + ACT + act + END);
                errors.addAll(context);
            }
        }
    }

    private static final String EXP = ": expected <";
    private static final String ACT = "> actual <";
    private static final String END = ">";

    /** Associates an expected transaction id with a datapath.
     * Adds the xid to the end of the list of expected xids for the datapath.
     *
     * @param dpid the datapath id
     * @param xid the expected transaction id
     */
    public void assocXid(DataPathId dpid, long xid) {
        synchronized (mapLock) {
            xids.get(dpid).add(xid);
        }
    }

    /** Setting the sink to non-strict mode means that the sink will ignore
     * any message received that it is not expecting, rather than failing
     * the test.
     * <p>
     * Returns a reference to itself to allow for method chaining.
     * For example:
     * <pre>
     *     sink.setNonStrict().replay();
     * </pre>
     * @return self
     */
    public ProgrammableSink setNonStrict() {
        strict = false;
        return this;
    }


    /** Encapsulates an expectation. */
    private static class Expectation {
        final MessageType expType;
        final AbstractMsgAssertor expAssertions;

        /** Constructs the expectation that the message is of the given type.
         *
         * @param type the expected message type
         */
        Expectation(MessageType type) {
            expType = type;
            expAssertions = null;
        }

        /** Constructs the expectation that the message is of the given type,
         * and that the assertions embodied in the message assertor are true.
         *
         * @param type the expected message type
         * @param ma a message assertor
         */
        Expectation(MessageType type, AbstractMsgAssertor ma) {
            expType = type;
            expAssertions = ma;
        }
    }
}