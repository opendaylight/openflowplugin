/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.flow.impl;

import org.junit.Test;
import org.opendaylight.of.controller.impl.AbstractTest;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.instr.ActionType;
import org.opendaylight.of.lib.msg.*;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.of.controller.flow.GroupEventType.GROUP_MOD_PUSHED;
import static org.opendaylight.of.controller.flow.GroupEventType.GROUP_MOD_PUSH_FAILED;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.instr.ActionFactory.createAction;
import static org.opendaylight.of.lib.msg.MessageType.GROUP_MOD;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for GroupEvt.
 *
 * @author Simon Hunt
 */
public class GroupEvtTest extends AbstractTest {

    private static final DataPathId DPID = dpid("13/00bb00:aa00aa");

    private static Bucket createBucket(int watchPort, int outPort) {
        MutableBucket b = BucketFactory.createMutableBucket(V_1_3)
                .watchPort(bpn(watchPort));
        b.addAction(createAction(V_1_3, ActionType.OUTPUT, bpn(outPort)));
        return (Bucket) b.toImmutable();
    }

    private static final Bucket BKT1 = createBucket(5, 6);
    private static final Bucket BKT2 = createBucket(6, 7);

    private static OfmGroupMod createGroupMod() {
        OfmMutableGroupMod m = (OfmMutableGroupMod)
                MessageFactory.create(V_1_3, GROUP_MOD, GroupModCommand.ADD);
        m.groupType(GroupType.FF).groupId(gid(13))
                .addBucket(BKT1).addBucket(BKT2);
        return (OfmGroupMod) m.toImmutable();
    }

    private static final OfmGroupMod OFM_GROUP_MOD = createGroupMod();

    @Test
    public void groupModPushed() {
        print(EOL + "groupModPushed()");
        GroupEvt e = new GroupEvt(GROUP_MOD_PUSHED, DPID, OFM_GROUP_MOD);
        print(e);
        assertEquals(AM_NEQ, GROUP_MOD_PUSHED, e.type());
        assertEquals(AM_NEQ, DPID, e.dpid());
        assertEquals(AM_NEQ, OFM_GROUP_MOD, e.groupMod());
        print(e.groupMod().toDebugString());
    }
    @Test
    public void groupModPushFailed() {
        print(EOL + "groupModPushFailed()");
        GroupEvt e = new GroupEvt(GROUP_MOD_PUSH_FAILED, DPID, OFM_GROUP_MOD);
        print(e);
        assertEquals(AM_NEQ, GROUP_MOD_PUSH_FAILED, e.type());
        assertEquals(AM_NEQ, DPID, e.dpid());
        assertEquals(AM_NEQ, OFM_GROUP_MOD, e.groupMod());
        print(e.groupMod().toDebugString());
    }
}
