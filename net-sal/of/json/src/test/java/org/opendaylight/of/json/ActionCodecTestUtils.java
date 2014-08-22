/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.json;

import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.GroupId;
import org.opendaylight.of.lib.dt.QueueId;
import org.opendaylight.of.lib.instr.*;
import org.opendaylight.of.lib.match.MFieldBasicIp;
import org.opendaylight.of.lib.match.MatchField;
import org.opendaylight.util.net.BigPortNumber;
import org.opendaylight.util.net.EthernetType;
import org.opendaylight.util.net.IpAddress;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.instr.ActionFactory.createAction;
import static org.opendaylight.of.lib.instr.ActionFactory.createActionSetField;
import static org.opendaylight.of.lib.instr.ActionType.*;
import static org.opendaylight.of.lib.match.OxmBasicFieldType.IPV4_DST;
import static org.opendaylight.util.junit.TestTools.AM_NEQ;
import static org.opendaylight.util.junit.TestTools.AM_WRCL;

/**
 * Utility methods for {@link ActionCodecTest}.
 *
 * @author Shaila Shree
 */
public class ActionCodecTestUtils extends AbstractCodecTest {
    private static final int EXP_TTL = 10;
    private static final QueueId EXP_QUEUE_ID = qid(20);
    private static final GroupId EXP_GROUP_ID = gid(10);
    private static final IpAddress EXP_IP_ADDRESS = ip("15.255.124.141");
    private static final int EXP_ID = 0x2481;
    private static final byte[] EXP_DATA = {1, 2, 3, 4, 5, 6, 7, 8};

    public static final String AM_DUP = "Duplicate Action : ";
    public static final String AM_UNKNOWN_ACTION = "Action of unknown type : ";
    public static final String AM_UNKNOWN_VERSION =
                                "Action of protocol version : ";
    private static final BigPortNumber EXP_PORT = bpn(1987);

    public static Action createTestAction() {
        return createAction(V_1_3, SET_MPLS_TTL, EXP_TTL);
    }

    public static void verifyTestAction(ActSetMplsTtl setMplsTtl) {
        assertEquals(AM_NEQ, V_1_3, setMplsTtl.getVersion());
        assertEquals(AM_NEQ, SET_MPLS_TTL, setMplsTtl.getActionType());
        assertEquals(AM_NEQ, EXP_TTL, setMplsTtl.getTtl());
    }

    public static List<Action> createAllActions(ProtocolVersion version) {
        List<Action> actions = null;

        switch (version) {
            case V_1_0:
                actions = createActionsV10();
                break;
            case V_1_1:
            case V_1_2:
            case V_1_3:
                actions = createActionsV13();
                break;
        }

        return actions;
    }

    public static void verifyAllActions(ProtocolVersion version,
                                        List<Action> actions) {
        switch (version) {
            case V_1_0:
                verifyActionsV10(actions);
                break;
            case V_1_1:
            case V_1_2:
            case V_1_3:
                verifyActionsV13(actions);
                break;
            default:
                fail(AM_UNKNOWN_VERSION + version.toDisplayString());
        }
    }

    public static List<Action> createRandomActions1(ProtocolVersion version) {
        List<Action> actions = null;

        switch (version) {
            case V_1_0:
                actions = createRandomActionsV101();
                break;
            case V_1_1:
            case V_1_2:
            case V_1_3:
                actions = createRandomActionsV131();
                break;
        }

        return actions;
    }

    public static void verifyRandomActions1(ProtocolVersion version,
                                            List<Action> actions) {
        switch (version) {
            case V_1_0:
                verifyRandomActionsV101(actions);
                break;
            case V_1_1:
            case V_1_2:
            case V_1_3:
                verifyRandomActionsV131(actions);
                break;
            default:
                fail(AM_UNKNOWN_VERSION + version.toDisplayString());
        }
    }

    public static List<Action> createRandomActions2(ProtocolVersion version) {
        List<Action> actions = null;

        switch (version) {
            case V_1_0:
                actions = createRandomActionsV102();
                break;
            case V_1_1:
            case V_1_2:
            case V_1_3:
                actions = createRandomActionsV132();
                break;
        }

        return actions;
    }

    public static void verifyRandomActions2(ProtocolVersion version,
                                            List<Action> actions) {
        switch (version) {
            case V_1_0:
                verifyRandomActionsV102(actions);
                break;
            case V_1_1:
            case V_1_2:
            case V_1_3:
                verifyRandomActionsV132(actions);
                break;
            default:
                fail(AM_UNKNOWN_VERSION + version.toDisplayString());
        }
    }

    public static void verifyGroupRandomActions(List<Action> actions){
        Set<ActionType> hit = new HashSet<ActionType>();

        assertEquals(AM_NEQ, 3, actions.size());

        for(Action action: actions) {
            ActionType type = action.getActionType();

            if (hit.contains(type)) {
                fail(AM_DUP + type.name());
            }
            hit.add(type);

            assertEquals(AM_NEQ, V_1_3, action.getVersion());

            switch(type) {
                case OUTPUT:
                    validateActOutput(action);
                    break;
                case DEC_NW_TTL:
                    assertTrue(AM_WRCL, action instanceof ActDecNwTtl);
                    break;
                case SET_FIELD:
                    validateActSetField(action);
                    break;
                default:
                    fail(AM_UNKNOWN_ACTION + type.name());
                    break;
            }
        }

    }

    private static List<Action> createRandomActionsV101() {
        List<Action> actions = new ArrayList<Action>();

        actions.add(createAction(V_1_0, OUTPUT, EXP_PORT));
        return actions;
    }

    private static void verifyRandomActionsV101(List<Action> actions) {
        Set<ActionType> hit = new HashSet<ActionType>();

        assertEquals(AM_NEQ, 1, actions.size());

        for (Action action: actions) {
            ActionType type = action.getActionType();

            if (hit.contains(type))
                fail(AM_DUP + type.name());

            hit.add(type);

            assertEquals(AM_NEQ, V_1_0, action.getVersion());

            switch (type) {
                case OUTPUT:
                    validateActOutput(action);
                    break;
                default:
                    fail(AM_UNKNOWN_ACTION + type.name());
                    break;
            }
        }
    }

    private static List<Action> createRandomActionsV102() {
        List<Action> actions = new ArrayList<Action>();

        actions.add(createAction(V_1_0, SET_QUEUE, EXP_QUEUE_ID, EXP_PORT));
        actions.add(createActionSetField(V_1_0, IPV4_DST, EXP_IP_ADDRESS));

        return actions;
    }

    private static void verifyRandomActionsV102(List<Action> actions) {
        Set<ActionType> hit = new HashSet<ActionType>();

        assertEquals(AM_NEQ, 2, actions.size());

        for (Action action: actions) {
            ActionType type = action.getActionType();

            if (hit.contains(type))
                fail(AM_DUP + type.name());

            hit.add(type);

            assertEquals(AM_NEQ, V_1_0, action.getVersion());

            switch (type) {
                case SET_QUEUE:
                    validateActSetQueue10(action);
                    break;
                case SET_FIELD:
                    validateActSetField(action);
                    break;
                default:
                    fail(AM_UNKNOWN_ACTION + type.name());
                    break;
            }
        }
    }

    private static List<Action> createRandomActionsV131() {
        List<Action> actions = new ArrayList<Action>();

        actions.add(createAction(V_1_3, OUTPUT, EXP_PORT));
        actions.add(createAction(V_1_3, COPY_TTL_IN));
        actions.add(createAction(V_1_3, DEC_MPLS_TTL));

        return actions;
    }

    private static void verifyRandomActionsV131(List<Action> actions) {
        Set<ActionType> hit = new HashSet<ActionType>();
        assertEquals(AM_NEQ, 3, actions.size());

        for (Action action: actions) {
            ActionType type = action.getActionType();

            if (hit.contains(type))
                fail(AM_DUP + type.name());

            hit.add(type);

            assertEquals(AM_NEQ, V_1_3, action.getVersion());

            switch (type) {
                case OUTPUT:
                    validateActOutput(action);
                    break;
                case COPY_TTL_IN:
                    assertTrue(AM_WRCL, action instanceof ActCopyTtlIn);
                    break;
                case DEC_MPLS_TTL:
                    assertTrue(AM_WRCL, action instanceof ActDecMplsTtl);
                    break;
                default:
                    fail(AM_UNKNOWN_ACTION + type.name());
                    break;
            }
        }
    }

    private static List<Action> createRandomActionsV132() {
        List<Action> actions = new ArrayList<Action>();

        actions.add(createAction(V_1_3, POP_VLAN));
        actions.add(createAction(V_1_3, GROUP, EXP_GROUP_ID));
        actions.add(createActionSetField(V_1_3, IPV4_DST, EXP_IP_ADDRESS));

        return actions;
    }

    private static void verifyRandomActionsV132(List<Action> actions) {
        Set<ActionType> hit = new HashSet<ActionType>();
        assertEquals(AM_NEQ, 3, actions.size());

        for (Action action: actions) {
            ActionType type = action.getActionType();

            if (hit.contains(type))
                fail(AM_DUP + type.name());

            hit.add(type);

            assertEquals(AM_NEQ, V_1_3, action.getVersion());

            switch (type) {
                case POP_VLAN:
                    assertTrue(AM_WRCL, action instanceof ActPopVlan);
                    break;
                case GROUP:
                    validateActGroup(action);
                    break;
                case SET_FIELD:
                    validateActSetField(action);
                    break;
                default:
                    fail(AM_UNKNOWN_ACTION + type.name());
                    break;
            }
        }
    }

    private static List<Action> createActionsV10() {
        List<Action> actions = new ArrayList<Action>();

        actions.add(createAction(V_1_0, OUTPUT, EXP_PORT));
        actions.add(createAction(V_1_0, SET_QUEUE, EXP_QUEUE_ID, EXP_PORT));
        actions.add(createActionSetField(V_1_0, IPV4_DST, EXP_IP_ADDRESS));

        return actions;
    }

    private static void verifyActionsV10(List<Action> actions) {
        Set<ActionType> hit = new HashSet<ActionType>();

        assertEquals(AM_NEQ, 3, actions.size());

        for (Action action: actions) {
            ActionType type = action.getActionType();

            if (hit.contains(type))
                fail(AM_DUP + type.name());

            hit.add(type);

            assertEquals(AM_NEQ, V_1_0, action.getVersion());

            switch (type) {
                case OUTPUT:
                    validateActOutput(action);
                    break;
                case SET_QUEUE:
                    validateActSetQueue10(action);
                    break;
                case SET_FIELD:
                    validateActSetField(action);
                    break;
                default:
                    fail(AM_UNKNOWN_ACTION + type.name());
                    break;
            }
        }
    }

    private static void validateActSetQueue10(Action action) {
        assertTrue(AM_WRCL, action instanceof ActSetQueue);

        ActSetQueue setQueue = (ActSetQueue) action;
        assertEquals(AM_NEQ, EXP_QUEUE_ID, setQueue.getQueueId());
        assertEquals(AM_NEQ, EXP_PORT, setQueue.getPort());
    }

    private static List<Action> createActionsV13() {
        List<Action> actions = new ArrayList<Action>();

        actions.add(createAction(V_1_3, OUTPUT, EXP_PORT));
        actions.add(createAction(V_1_3, COPY_TTL_OUT));
        actions.add(createAction(V_1_3, COPY_TTL_IN));
        actions.add(createAction(V_1_3, SET_MPLS_TTL, EXP_TTL));
        actions.add(createAction(V_1_3, DEC_MPLS_TTL));
        actions.add(createAction(V_1_3, PUSH_VLAN, EthernetType.VLAN));
        actions.add(createAction(V_1_3, POP_VLAN));
        actions.add(createAction(V_1_3, PUSH_MPLS, EthernetType.MPLS_U));
        actions.add(createAction(V_1_3, POP_MPLS, EthernetType.MPLS_U));
        actions.add(createAction(V_1_3, SET_QUEUE, EXP_QUEUE_ID));
        actions.add(createAction(V_1_3, GROUP, EXP_GROUP_ID));
        actions.add(createAction(V_1_3, SET_NW_TTL, EXP_TTL));
        actions.add(createAction(V_1_3, DEC_NW_TTL));
        actions.add(createActionSetField(V_1_3, IPV4_DST, EXP_IP_ADDRESS));
        actions.add(createAction(V_1_3, PUSH_PBB, EthernetType.PBB));
        actions.add(createAction(V_1_3, POP_PBB));
        actions.add(createAction(V_1_3, EXPERIMENTER, EXP_ID, EXP_DATA));

        return actions;
    }

    private static void verifyActionsV13(List<Action> actions) {
        Set<ActionType> hit = new HashSet<ActionType>();
        assertEquals(AM_NEQ, 17, actions.size());

        for (Action action: actions) {
            ActionType type = action.getActionType();

            if (hit.contains(type))
                fail(AM_DUP + type.name());

            hit.add(type);

            assertEquals(AM_NEQ, V_1_3, action.getVersion());

            switch (type) {
                case OUTPUT:
                    validateActOutput(action);
                    break;
                case COPY_TTL_OUT:
                    assertTrue(AM_WRCL, action instanceof ActCopyTtlOut);
                    break;
                case COPY_TTL_IN:
                    assertTrue(AM_WRCL, action instanceof ActCopyTtlIn);
                    break;
                case DEC_MPLS_TTL:
                    assertTrue(AM_WRCL, action instanceof ActDecMplsTtl);
                    break;
                case POP_VLAN:
                    assertTrue(AM_WRCL, action instanceof ActPopVlan);
                    break;
                case DEC_NW_TTL:
                    assertTrue(AM_WRCL, action instanceof ActDecNwTtl);
                    break;
                case POP_PBB:
                    assertTrue(AM_WRCL, action instanceof ActPopPbb);
                    break;
                case SET_MPLS_TTL:
                    validateActSetMplsTtl(action);
                    break;
                case PUSH_VLAN:
                    validateActPushVlan(action);
                    break;
                case PUSH_MPLS:
                    validateActPushMpls(action);
                    break;
                case POP_MPLS:
                    validateActPopMpls(action);
                    break;
                case SET_QUEUE:
                    validateActSetQueue13(action);
                    break;
                case GROUP:
                    validateActGroup(action);
                    break;
                case SET_NW_TTL:
                    validateActSetNwTtl(action);
                    break;
                case SET_FIELD:
                    validateActSetField(action);
                    break;
                case PUSH_PBB:
                    validateActPopPbb(action);
                    break;
                case EXPERIMENTER:
                    validateActExperimenter( action);
                    break;
                default:
                    fail(AM_UNKNOWN_ACTION + type.name());
                    break;
            }
        }
    }

    private static void validateActOutput(Action action) {
        assertTrue(AM_WRCL, action instanceof ActOutput);
        assertEquals(AM_NEQ, EXP_PORT, ((ActOutput) action).getPort());
    }

    private static void validateActSetMplsTtl(Action action) {
        assertTrue(AM_WRCL, action instanceof ActSetMplsTtl);
        assertEquals(AM_NEQ, EXP_TTL, ((ActSetMplsTtl) action).getTtl());
    }

    private static void validateActPushVlan(Action action) {
        assertTrue(AM_WRCL, action instanceof ActPushVlan);
        assertEquals(AM_NEQ, EthernetType.VLAN,
                ((ActPushVlan) action).getEthernetType());
    }

    private static void validateActSetField(Action action) {
        assertTrue(AM_WRCL, action instanceof ActSetField);
        MatchField matchField = ((ActSetField) action).getField();

        MFieldBasicIp basicIp = (MFieldBasicIp)matchField;
        assertEquals(AM_NEQ, EXP_IP_ADDRESS, basicIp.getIpAddress());
    }

    private static void validateActPushMpls(Action action) {
        assertTrue(AM_WRCL, action instanceof ActPushMpls);
        assertEquals(AM_NEQ, EthernetType.MPLS_U,
                ((ActPushMpls) action).getEthernetType());
    }

    private static void validateActPopMpls(Action action) {
        assertTrue(AM_WRCL, action instanceof ActPopMpls);
        assertEquals(AM_NEQ, EthernetType.MPLS_U,
                ((ActPopMpls) action).getEthernetType());
    }

    private static void validateActGroup(Action action) {
        assertTrue(AM_WRCL, action instanceof ActGroup);
        assertEquals(AM_NEQ, EXP_GROUP_ID, ((ActGroup)action).getGroupId());
    }

    private static void validateActSetQueue13(Action action) {
        assertTrue(AM_WRCL, action instanceof ActSetQueue);
        assertEquals(AM_NEQ, EXP_QUEUE_ID, ((ActSetQueue)action).getQueueId());
    }

    private static void validateActSetNwTtl(Action action) {
        assertTrue(AM_WRCL, action instanceof ActSetNwTtl);
        assertEquals(AM_NEQ, EXP_TTL, ((ActSetNwTtl) action).getTtl());
    }

    private static void validateActExperimenter(Action action) {
        assertTrue(AM_WRCL, action instanceof ActExperimenter);

        ActExperimenter actExp = (ActExperimenter)action;
        assertEquals(AM_NEQ, EXP_ID, actExp.getId());
        assertArrayEquals(AM_NEQ, EXP_DATA, actExp.getData());
    }

    private static void validateActPopPbb(Action action) {
        assertTrue(AM_WRCL, action instanceof ActPushPbb);
        assertEquals(AM_NEQ, EthernetType.PBB,
                ((ActPushPbb) action).getEthernetType());
    }
}
