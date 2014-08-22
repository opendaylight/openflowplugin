/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.instr;

import org.opendaylight.of.lib.match.MatchField;
import org.opendaylight.of.lib.match.MatchLookup;
import org.opendaylight.of.lib.match.OxmBasicFieldType;
import org.opendaylight.util.SafeMap;

import static org.junit.Assert.*;
import static org.opendaylight.of.lib.instr.ActionType.*;
import static org.opendaylight.util.junit.TestTools.AM_NEQ;
import static org.opendaylight.util.junit.TestTools.AM_WRCL;

/**
 * Utility test class.
 *
 * @author Simon Hunt
 */
public class ActionLookup {

    // mapping of action type to concrete class
    private static final SafeMap<ActionType,
            Class<? extends Action>> A_CLS =
            new SafeMap.Builder<ActionType,
                    Class<? extends Action>>(Action.class)
                    .add(OUTPUT, ActOutput.class)
                    .add(COPY_TTL_OUT, ActCopyTtlOut.class)
                    .add(COPY_TTL_IN, ActCopyTtlIn.class)
                    .add(SET_MPLS_TTL, ActSetMplsTtl.class)
                    .add(DEC_MPLS_TTL, ActDecMplsTtl.class)
                    .add(PUSH_VLAN, ActPushVlan.class)
                    .add(POP_VLAN, ActPopVlan.class)
                    .add(PUSH_MPLS, ActPushMpls.class)
                    .add(POP_MPLS, ActPopMpls.class)
                    .add(SET_QUEUE, ActSetQueue.class)
                    .add(GROUP, ActGroup.class)
                    .add(SET_NW_TTL, ActSetNwTtl.class)
                    .add(DEC_NW_TTL, ActDecNwTtl.class)
                    .add(SET_FIELD, ActSetField.class)
                    .add(PUSH_PBB, ActPushPbb.class)
                    .add(POP_PBB, ActPopPbb.class)
                    .add(EXPERIMENTER, ActExperimenter.class)
                    .build();

    // mapping of action type to base abstract class
    //  NOTE: unlisted types will default to Action.class
    private static final SafeMap<ActionType,
            Class<? extends Action>> A_BASE_CLS =
            new SafeMap.Builder<ActionType,
                    Class<? extends Action>>(Action.class)
                    .add(OUTPUT, ActOutput.class)
                    .add(SET_MPLS_TTL, ActionTtl.class)
                    .add(PUSH_VLAN, ActionEther.class)
                    .add(PUSH_MPLS, ActionEther.class)
                    .add(POP_MPLS, ActionEther.class)
                    .add(SET_QUEUE, ActSetQueue.class)
                    .add(GROUP, ActGroup.class)
                    .add(SET_NW_TTL, ActionTtl.class)
                    .add(SET_FIELD, ActSetField.class)
                    .add(PUSH_PBB, ActionEther.class)
                    .add(EXPERIMENTER, ActExperimenter.class)
                    .build();

    /** Verifies that the specified action is of the correct type, and
     * contains the specified value (if any).
     *
     * @param a the action
     * @param expType the expected type
     * @param expValue the expected value (null for no value)
     */
    public static void verifyAction(Action a, ActionType expType,
                                    Object expValue) {
        assertEquals(AM_NEQ, expType, a.getActionType());
        assertTrue(AM_WRCL, A_CLS.get(expType).isInstance(a));

        Class<? extends Action> base = A_BASE_CLS.get(expType);

        //===============================================================
        // NOTE: Not a recommended pattern for production code, but this
        //       is convenient for testing actions concisely.
        //
        //  ++++ DO NOT REPLICATE THIS PATTERN IN PRODUCTION CODE ++++
        //===============================================================
        if (base == Action.class) {
            // nothing more to verify
        } else if (base == ActOutput.class) {
            ActOutput ao = (ActOutput) a;
            assertEquals(AM_NEQ, expValue, ao.getPort());

        } else if (base == ActGroup.class) {
            ActGroup ag = (ActGroup) a;
            assertEquals(AM_NEQ, expValue, ag.getGroupId());

        } else if (base == ActSetQueue.class) {
            ActSetQueue asq = (ActSetQueue) a;
            assertEquals(AM_NEQ, expValue, asq.getQueueId());

        } else if (base == ActionTtl.class) {
            ActionTtl at = (ActionTtl) a;
            assertEquals(AM_NEQ, expValue, at.getTtl());

        } else if (base == ActionEther.class) {
            ActionEther ae = (ActionEther) a;
            assertEquals(AM_NEQ, expValue, ae.getEthernetType());

        } else if (base == ActionU32.class) {
            ActionU32 au = (ActionU32) a;
            assertEquals(AM_NEQ, expValue, au.value);

        } else if (base == ActSetField.class) {
            fail("Use verifyActionSetField() instead");

        } else if (base == ActExperimenter.class) {
            ActExperimenter ae = (ActExperimenter) a;
            assertEquals(AM_NEQ, expValue, ae.getExpId());

        } else {
            fail("Did not match base class: " + base);
        }

    }

    /** Verifies that the specified action is of the correct type.
     *
     * @param a the action
     * @param expType the expected type
     */
    public static void verifyAction(Action a, ActionType expType) {
        verifyAction(a, expType, null);
    }

    /** Verifies that the set field action contains the correct field
     * information.
     *
     * @param a the set field action
     * @param expFt the expected match field type
     * @param expValue the expected match field value
     */
    public static void verifyActionSetField(Action a,
                                            OxmBasicFieldType expFt,
                                            Object expValue) {
        assertEquals(AM_NEQ, SET_FIELD, a.getActionType());
        assertTrue(AM_WRCL, ActSetField.class.isInstance(a));

        MatchField mf = ((ActSetField)a).getField();
        MatchLookup.verifyField(mf, expFt, expValue);
    }
}
