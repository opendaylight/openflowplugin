/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.impl;

import org.junit.After;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.of.controller.flow.*;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.msg.OfmFlowMod;
import org.opendaylight.of.lib.msg.OfmFlowRemoved;
import org.opendaylight.of.lib.msg.OfmGroupMod;
import org.opendaylight.of.lib.msg.OfmMeterMod;
import org.opendaylight.util.event.DefaultEventSinkBroker;
import org.opendaylight.util.event.Event;
import org.opendaylight.util.event.EventDispatchService;
import org.opendaylight.util.event.EventSink;

import static junit.framework.Assert.assertEquals;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit testing EventManager.
 *
 * @author Simon Hunt
 */
public class EventManagerTest extends AbstractControllerTest {

    private static class MockEvent {
        private final long id;
        MockEvent(long id) { this.id = id; }
        public DataPathId dpid() { return null; }
        public long ts() { return id; }
        @Override public String toString() {
            return "{" + getClass().getSimpleName() + ":" + id + "}";
        }
    }

    private static class FlowEventAdapter extends MockEvent implements FlowEvent {
        FlowEventAdapter(long id) { super(id); }
        @Override public OfmFlowMod flowMod() { return null; }
        @Override public OfmFlowRemoved flowRemoved() { return null; }
        @Override public FlowEventType type() { return null; }
    }

    private static class MockFlowEvent extends FlowEventAdapter {
        MockFlowEvent(long id) { super(id); }
        @Override public FlowEventType type() { return FlowEventType.FLOW_MOD_PUSHED; }
    }

    private static class MockGroupEvent extends MockEvent implements GroupEvent {
        MockGroupEvent(long id) { super(id); }
        @Override public OfmGroupMod groupMod() { return null; }
        @Override public GroupEventType type() { return null; }
    }

    private static class MockMeterEvent extends MockEvent implements MeterEvent {
        MockMeterEvent(long id) { super(id); }
        @Override public OfmMeterMod meterMod() { return null; }
        @Override public MeterEventType type() { return null; }
    }


    private static class TestEventDispatcher extends DefaultEventSinkBroker
            implements EventDispatchService {

        int count = 0;

        @Override
        public void post(Event event) {
            print("posting event {}", event);
            count++;
            EventSink sink = get(event.getClass());
            if (sink != null) {
                print("dispatching via sink {}", sink);
                sink.dispatch(event);
            } else {
                print("Failed to find sink for {}", event.getClass());
            }
        }
    }

    private static class FlowList implements FlowListener {
        int eventsSeen = 0;
        long lastIdSeen;

        @Override
        public void event(FlowEvent event) {
            eventsSeen++;
            lastIdSeen = event.ts();
        }
    }

    private static class GroupList implements GroupListener {
        int eventsSeen = 0;
        long lastIdSeen;

        @Override
        public void event(GroupEvent event) {
            eventsSeen++;
            lastIdSeen = event.ts();
        }
    }

    private static class MeterList implements MeterListener {
        int eventsSeen = 0;
        long lastIdSeen;

        @Override
        public void event(MeterEvent event) {
            eventsSeen++;
            lastIdSeen = event.ts();
        }
    }

    // ======================================================================

    private TestEventDispatcher dispatch;
    private FlowList fl;
    private GroupList glA;
    private GroupList glB;
    private MeterList ml;

    @BeforeClass
    public static void classSetUp() {
        Assume.assumeTrue(!isUnderCoverage());
        setUpLogger();
    }

    @After
    public void tearDown() {
        cmgr.shutdown();
    }

    // ======================================================================
    // === HELPER methods

    private void initController() {
        alertSink = new AlertLogger();
        roleAdvisor = new MockRoleAdvisor();
        dispatch = new TestEventDispatcher();

        cmgr = new ControllerManager(DEFAULT_CTRL_CFG, alertSink, PH_SINK,
                FM_ADV, roleAdvisor, dispatch);
        cs = cmgr;
        cmgr.startIOProcessing();
        lmgr = cmgr.getListenerManager();
        print("... controller activated ...");
    }


    // ======================================================================

    @Test
    public void flowEvents() {
        print(EOL + "flowEvents()");
        initController();

        fl = new FlowList();
        dispatch.post(new MockFlowEvent(25));
        assertEquals(AM_NEQ, 1, dispatch.count);
        assertEquals(AM_NEQ, 0, fl.eventsSeen);

        cs.addFlowListener(fl);
        dispatch.post(new MockFlowEvent(35));
        assertEquals(AM_NEQ, 2, dispatch.count);
        assertEquals(AM_NEQ, 1, fl.eventsSeen);
        assertEquals(AM_NEQ, 35, fl.lastIdSeen);

        dispatch.post(new MockGroupEvent(100));
        assertEquals(AM_NEQ, 3, dispatch.count);
        assertEquals(AM_NEQ, 1, fl.eventsSeen);
        assertEquals(AM_NEQ, 35, fl.lastIdSeen);

        cs.removeFlowListener(fl);
        dispatch.post(new MockFlowEvent(42));
        assertEquals(AM_NEQ, 4, dispatch.count);
        assertEquals(AM_NEQ, 1, fl.eventsSeen);
        assertEquals(AM_NEQ, 35, fl.lastIdSeen);
    }

    @Test
    public void groupEvents() {
        print(EOL + "groupEvents()");
        initController();
        glA = new GroupList();
        glB = new GroupList();
        cs.addGroupListener(glA);
        cs.addGroupListener(glB);

        dispatch.post(new MockFlowEvent(7));
        assertEquals(AM_NEQ, 1, dispatch.count);
        assertEquals(AM_NEQ, 0, glA.eventsSeen);
        assertEquals(AM_NEQ, 0, glB.eventsSeen);

        dispatch.post(new MockGroupEvent(9));
        assertEquals(AM_NEQ, 2, dispatch.count);
        assertEquals(AM_NEQ, 1, glA.eventsSeen);
        assertEquals(AM_NEQ, 9, glA.lastIdSeen);
        assertEquals(AM_NEQ, 1, glB.eventsSeen);
        assertEquals(AM_NEQ, 9, glB.lastIdSeen);

        cs.removeGroupListener(glA);
        dispatch.post(new MockGroupEvent(11));
        assertEquals(AM_NEQ, 3, dispatch.count);
        assertEquals(AM_NEQ, 1, glA.eventsSeen);
        assertEquals(AM_NEQ, 9, glA.lastIdSeen);
        assertEquals(AM_NEQ, 2, glB.eventsSeen);
        assertEquals(AM_NEQ, 11, glB.lastIdSeen);
    }

    @Test
    public void meterEvents() {
        print(EOL + "meterEvents()");
        initController();
        ml = new MeterList();
        cs.addMeterListener(ml);

        dispatch.post(new MockFlowEvent(21));
        dispatch.post(new MockGroupEvent(22));
        dispatch.post(new MockMeterEvent(23));
        assertEquals(AM_NEQ, 3, dispatch.count);
        assertEquals(AM_NEQ, 1, ml.eventsSeen);
        assertEquals(AM_NEQ, 23, ml.lastIdSeen);
    }
}
