/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.common;

import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.msg.OpenflowMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple abstract base class for making assertions about
 * an openflow message.
 *
 * @author Simon Hunt
 */
public abstract class AbstractMsgAssertor {

    private final String name;
    private final DataPathId dpid;
    private final ProgrammableSink sink;

    /** Constructs a message assertor.
     *
     * @param friendlyname friendly name for display purposes
     * @param dpid the datapath id with which this message assertor is
     *             associated
     * @param sink the parent programmable sink
     */
    public AbstractMsgAssertor(String friendlyname, DataPathId dpid,
                               ProgrammableSink sink) {
        this.name = friendlyname;
        this.dpid = dpid;
        this.sink = sink;
    }

    private static final int FIRST_STACK_FRAME = 3;
    private static final int LAST_STACK_FRAME = 6;
    private static final String INDENT = "  ";

    /** Returns the friendly name.
     *
     * @return the friendly name
     */
    public String getName() {
        return name;
    }

    private List<String> context() {
        List<String> frames = new ArrayList<String>();
        try {
            throw new RuntimeException();
        } catch (RuntimeException e) {
            StackTraceElement[] trace = e.getStackTrace();
            for (int i=FIRST_STACK_FRAME; i<LAST_STACK_FRAME; i++)
                frames.add(INDENT + trace[i].toString());
        }
        return frames;
    }

    private void addError(String msg, Object exp, Object act) {
        sink.addError(dpid, context(), msg, exp, act);
    }

    private void addError(String msg) {
        sink.addError(dpid, context(), msg);
    }

    /** Records an assertion error if the expected object is not equivalent
     * to the actual object. This assertion succeeds
     * if {@code exp.equals(act)} returns true.
     *
     * @param msg the assertion failure message
     * @param exp the expected object
     * @param act the actual object
     */
    protected void assertEquals(String msg, Object exp, Object act) {
        if (!exp.equals(act))
            addError("not equal: " + msg, exp, act);
    }


    /** Records an assertion error if the specified object
     * reference is not null.
     *
     * @param msg the assertion failure message
     * @param o the object reference
     */
    protected void assertNull(String msg, Object o) {
        if (o != null)
            addError("not null: " + msg);
    }

    /** Records an assertion error if the specified object reference is null.
     *
     * @param msg the assertion failure message
     * @param o the object reference
     */
    protected void assertNotNull(String msg, Object o) {
        if (o == null)
            addError("null: " + msg);
    }

    /** Throws an assertion error if the specified condition is not true.
     *
     * @param msg the assertion failure message
     * @param condition the condition
     */
    protected void assertTrue(String msg, boolean condition) {
        if (!condition)
            addError("not true: " + msg);
    }

    /** Throws an assertion error if the specified condition is not false.
     *
     * @param msg the assertion failure message
     * @param condition the condition
     */
    protected void assertFalse(String msg, boolean condition) {
        if (condition)
            addError("not false: " + msg);
    }

    /** Concrete subclasses run assertions against a message.
     *
     * @param msg the message to assert stuff about
     */
    protected abstract void runAssertions(OpenflowMessage msg);
}
