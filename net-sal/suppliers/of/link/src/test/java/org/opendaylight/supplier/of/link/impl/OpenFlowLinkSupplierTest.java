/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.supplier.of.link.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.net.device.DeviceSupplier;
import org.opendaylight.net.device.DeviceSupplierService;
import org.opendaylight.net.device.impl.DeviceManager;
import org.opendaylight.net.dispatch.impl.TestEventDispatcher;
import org.opendaylight.net.link.impl.LinkManager;
import org.opendaylight.net.model.DefaultInterfaceInfo;
import org.opendaylight.net.model.DeviceId;
import org.opendaylight.net.model.InterfaceInfo;
import org.opendaylight.net.model.UriSchemes;
import org.opendaylight.net.supplier.SupplierId;
import org.opendaylight.of.controller.ControllerServiceAdapter;
import org.opendaylight.of.controller.pkt.MessageContext;
import org.opendaylight.of.controller.pkt.MessageContextAdapter;
import org.opendaylight.of.controller.pkt.SequencedPacketListener;
import org.opendaylight.of.controller.pkt.SequencedPacketListenerRole;
import org.opendaylight.of.lib.OpenflowException;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.dt.DataPathInfo;
import org.opendaylight.of.lib.dt.DataPathInfoAdapter;
import org.opendaylight.of.lib.msg.*;
import org.opendaylight.util.driver.DefaultDeviceInfo;
import org.opendaylight.util.driver.DefaultDeviceType;
import org.opendaylight.util.driver.DeviceInfo;
import org.opendaylight.util.event.EventDispatchService;
import org.opendaylight.util.net.BigPortNumber;
import org.opendaylight.util.net.MacAddress;
import org.opendaylight.util.packet.ProtocolId;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import static org.opendaylight.net.model.Interface.State.UP;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.msg.MessageType.PACKET_OUT;
import static org.opendaylight.util.CommonUtils.itemList;
import static org.opendaylight.util.CommonUtils.itemSet;
import static org.opendaylight.util.junit.TestTools.delay;
import static org.opendaylight.util.net.MacAddress.mac;

/**
 * Suite of tests for the link supplier.
 *
 * @author Thomas Vachuska
 */
public class OpenFlowLinkSupplierTest {

    private static final long CID = 1;

    private static final DeviceId DID1 = DeviceId.valueOf("11:11:11:11:11:11:11:11");
    private static final BigPortNumber P1 = BigPortNumber.bpn(1);
    private static final MacAddress MAC1 = mac("11:11:11:11:11:11");

    private static final DeviceId DID2 = DeviceId.valueOf("22:22:22:22:22:22:22:22");
    private static final BigPortNumber P2 = BigPortNumber.bpn(2);
    private static final MacAddress MAC2 = mac("22:22:22:22:22:22");

    private final OpenFlowLinkSupplier supplier = new OpenFlowLinkSupplier(CID);
    private final MockControllerService controllerService = new MockControllerService();
    private final DefaultDeviceType mockDeviceType = new MockDefaultDeviceType();

    // We're using real link & device managers and viewing them as both
    // service and suppliers brokers
    private TestLinkManager linkManager = new TestLinkManager();
    private TestDeviceManager deviceManager = new TestDeviceManager();

    private final DeviceSupplier mockDeviceSupplier = new MockDeviceSupplier();
    private DeviceSupplierService deviceSupplierService;

    @Before
    public void setUp() {
        supplier.controllerService = controllerService;

        deviceManager.setDispatcher(new TestEventDispatcher());
        deviceManager.activate();
        supplier.deviceService = deviceManager;

        linkManager.setDispatcher(new TestEventDispatcher());
        linkManager.activate();
        supplier.linkService = linkManager;

        supplier.broker = linkManager;
        supplier.activate();

        assertEquals("supplier did not register", 1, linkManager.getSuppliers().size());
        assertNotNull("listener did not register", controllerService.listener);

        deviceSupplierService = deviceManager.registerSupplier(mockDeviceSupplier);
    }

    @After
    public void tearDown() {
        supplier.deactivate();

        assertEquals("supplier did not unregister", 0, linkManager.getSuppliers().size());
        assertNull("listener did not unregister", controllerService.listener);
    }


    @Test
    public void basic() {
        // Create a device with a single interface and make sure we see response
        createTestDevice(DID1, P1, MAC1);
        validateMessages(PACKET_OUT, PACKET_OUT);
        controllerService.messages.clear();

        // Create anoter device with a single interface and make sure we see response
        createTestDevice(DID2, P2, MAC2);
        validateMessages(PACKET_OUT, PACKET_OUT);

        // FIXME: finish implementation

        // Now pretend that the first device received a direct BDDP packet
        // sent out from the second device

        // Now assert that we have a link
    }

    private DataPathId dpid(DeviceId id) {
        return DataPathId.dpid(id.fingerprint());
    }

    private void createTestDevice(DeviceId id, BigPortNumber port, MacAddress mac) {
        DeviceInfo di = new DefaultDeviceInfo(mockDeviceType);
        URI uri = URI.create(UriSchemes.OPENFLOW.toString() + ":" + id.fingerprint());
        deviceSupplierService.createOrUpdateDevice(id, itemSet(uri), di);
        InterfaceInfo ii = new DefaultInterfaceInfo(id, port).state(itemSet(UP)).mac(mac);
        deviceSupplierService.updateInterfaces(id, itemList(ii));
    }

    private void validateMessages(MessageType... types) {
        delay(300);
        List<MockMessage> messages = controllerService.messages;
        for (int i = 0; i < types.length && i < messages.size(); i++)
            assertEquals("incorrect message type", types[i], messages.get(i).msg.getType());
        assertEquals("incorrect number of messages", types.length, messages.size());
        messages.clear();
    }

    private static final String DEFAULT_DEVICE_TYPE_NAME = "mock";

    private class MockDefaultDeviceType extends DefaultDeviceType {
        MockDefaultDeviceType() {
            super(null, DEFAULT_DEVICE_TYPE_NAME);
        }
    }

    private class MockDeviceSupplier implements DeviceSupplier {
        @Override
        public SupplierId supplierId() {
            return new SupplierId("foo");
        }
    }

    private class TestDeviceManager extends DeviceManager {
        void setDispatcher(EventDispatchService eds) {
            this.dispatchService = eds;
        }
    }

    private class TestLinkManager extends LinkManager {
        void setDispatcher(EventDispatchService eds) {
            this.dispatchService = eds;
        }
    }



    // Mock controller service to interact with
    private class MockControllerService extends ControllerServiceAdapter {
        private SequencedPacketListener listener;
        private List<MockMessage> messages = new ArrayList<>();


        @Override
        public DataPathInfo getDataPathInfo(DataPathId dpid) {
            return new MockDataPathInfoAdapter(dpid);
        }

        @Override
        public void addPacketListener(SequencedPacketListener listener, SequencedPacketListenerRole role, int altitude) {
            this.listener = listener;
        }

        @Override
        public void addPacketListener(SequencedPacketListener listener, SequencedPacketListenerRole role, int altitude, Set<ProtocolId> interest) {
            this.listener = listener;
        }

        @Override
        public void removePacketListener(SequencedPacketListener listener) {
            assertEquals("incorrect listener", this.listener, listener);
            this.listener = null;
        }

        @Override
        public MessageFuture send(OpenflowMessage msg, DataPathId dpid) throws OpenflowException {
            messages.add(new MockMessage(dpid, msg));
            MessageFuture mf = new DefaultMessageFuture(msg);
            mf.setSuccess();
            return mf;
        }
    }

    private class MockDataPathInfoAdapter extends DataPathInfoAdapter {
        final DataPathId dpid;
        public MockDataPathInfoAdapter(DataPathId dpid) { this.dpid = dpid; }
        @Override public DataPathId dpid() { return dpid; }
        @Override public ProtocolVersion negotiated() { return V_1_0; }
    }

    private class MockMessage {
        final DataPathId dpid;
        final OpenflowMessage msg;
        MockMessage(DataPathId dpid, OpenflowMessage msg) {
            this.dpid = dpid;
            this.msg = msg;
        }
    }

    private class MockMessageContext extends MessageContextAdapter implements MessageContext {
        final OfmPacketIn packetIn;

        public MockMessageContext(OfmMutablePacketIn mpi) {
            packetIn = (OfmPacketIn) mpi.toImmutable();
        }

    }
}
