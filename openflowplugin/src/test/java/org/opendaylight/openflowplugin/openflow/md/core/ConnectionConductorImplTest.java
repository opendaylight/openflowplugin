/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowplugin.openflow.md.core.plan.ConnectionAdapterStackImpl;
import org.opendaylight.openflowplugin.openflow.md.core.plan.EventFactory;
import org.opendaylight.openflowplugin.openflow.md.core.plan.SwitchTestEvent;
import org.opendaylight.openflowplugin.openflow.md.core.session.OFSessionUtil;
import org.opendaylight.openflowplugin.openflow.md.core.session.SessionContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ErrorType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortReason;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoRequestMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemovedMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemovedMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortStatusMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortStatusMessageBuilder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mirehak
 */
public class ConnectionConductorImplTest {

    private static final Logger LOG = LoggerFactory
            .getLogger(ConnectionConductorImplTest.class);

    protected ConnectionAdapterStackImpl adapter;
    private ConnectionConductorImpl connectionConductor;
    private Stack<SwitchTestEvent> eventPlan;

    private Thread libSimulation;
    private ScheduledThreadPoolExecutor pool = new ScheduledThreadPoolExecutor(
            8);
    private int experimenterMessageCounter;
    private int packetinMessageCounter;
    private int flowremovedMessageCounter;
    private int portstatusAddMessageCounter;
    private int portstatusDeleteMessageCounter;
    private int portstatusModifyMessageCounter;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        adapter = new ConnectionAdapterStackImpl();
        connectionConductor = new ConnectionConductorImpl(adapter);
        connectionConductor.init();
        eventPlan = new Stack<>();
        adapter.setEventPlan(eventPlan);
        adapter.setProceedTimeout(5000L);
        adapter.checkListeners();
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        if (libSimulation != null) {
            libSimulation.join();
        }
        for (Exception problem : adapter.getOccuredExceptions()) {
            LOG.error("during simulation on adapter side: "
                    + problem.getMessage());
        }
        Assert.assertEquals(0, adapter.getOccuredExceptions().size());
        adapter = null;
        if (LOG.isDebugEnabled()) {
            if (eventPlan.size() > 0) {
                LOG.debug("eventPlan size: " + eventPlan.size());
                for (SwitchTestEvent event : eventPlan) {
                    LOG.debug(" # EVENT:: " + event.toString());
                }
            }
        }
        Assert.assertTrue("plan is not finished", eventPlan.isEmpty());
        eventPlan = null;
    }

    /**
     * Test method for
     * {@link org.opendaylight.openflowplugin.openflow.md.core.ConnectionConductorImpl#onEchoRequestMessage(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoRequestMessage)}
     * .
     * @throws Exception
     */
    @Test
    public void testOnEchoRequestMessage() throws Exception {
        eventPlan.add(0, EventFactory.createDefaultNotificationEvent(42L,
                EventFactory.DEFAULT_VERSION, new EchoRequestMessageBuilder()));
        eventPlan.add(0,
                EventFactory.createDefaultWaitForRpcEvent(42, "echoReply"));
        executeNow();
    }

    /**
     * Test of handshake, covering version negotiation and features .
     * @throws Exception
     */
    @Test
    public void testHandshake1() throws Exception {
        eventPlan.add(0, EventFactory.createDefaultNotificationEvent(42L,
                EventFactory.DEFAULT_VERSION, new HelloMessageBuilder()));
        eventPlan.add(0,
                EventFactory.createDefaultWaitForRpcEvent(43, "helloReply"));
        eventPlan.add(0,
                EventFactory.createDefaultWaitForRpcEvent(44, "getFeatures"));
        GetFeaturesOutputBuilder getFeaturesOutputBuilder = new GetFeaturesOutputBuilder();
        getFeaturesOutputBuilder.setDatapathId(new BigInteger("102030405060"));
        getFeaturesOutputBuilder.setAuxiliaryId((short) 0);
        getFeaturesOutputBuilder.setBuffers(4L);
        getFeaturesOutputBuilder.setReserved(0L);
        getFeaturesOutputBuilder.setTables((short) 2);
        getFeaturesOutputBuilder.setCapabilities(84L);

        eventPlan.add(0, EventFactory.createDefaultRpcResponseEvent(44,
                EventFactory.DEFAULT_VERSION, getFeaturesOutputBuilder));

        execute(true);
        Assert.assertEquals(ConnectionConductor.CONDUCTOR_STATE.WORKING,
                connectionConductor.getConductorState());
        Assert.assertEquals((short) 0x04, connectionConductor.getVersion()
                .shortValue());
    }

    /**
     * Test of handshake, covering version negotiation and features .
     * @throws Exception
     */
    @Test
    public void testHandshake2() throws Exception {
        eventPlan.add(0, EventFactory.createDefaultNotificationEvent(42L,
                (short) 0x05, new HelloMessageBuilder()));
        eventPlan.add(0,
                EventFactory.createDefaultWaitForRpcEvent(43, "helloReply"));
        eventPlan.add(0, EventFactory.createDefaultNotificationEvent(43L,
                (short) 0x03, new HelloMessageBuilder()));
        eventPlan.add(0,
                EventFactory.createDefaultWaitForRpcEvent(44, "helloReply"));
        eventPlan.add(0, EventFactory.createDefaultNotificationEvent(44L,
                (short) 0x01, new HelloMessageBuilder()));
        eventPlan.add(0,
                EventFactory.createDefaultWaitForRpcEvent(45, "helloReply"));
        eventPlan.add(0,
                EventFactory.createDefaultWaitForRpcEvent(46, "getFeatures"));
        GetFeaturesOutputBuilder getFeaturesOutputBuilder = new GetFeaturesOutputBuilder();
        getFeaturesOutputBuilder.setDatapathId(new BigInteger("102030405060"));
        getFeaturesOutputBuilder.setAuxiliaryId((short) 0);
        getFeaturesOutputBuilder.setBuffers(4L);
        getFeaturesOutputBuilder.setReserved(0L);
        getFeaturesOutputBuilder.setTables((short) 2);
        getFeaturesOutputBuilder.setCapabilities(84L);

        eventPlan.add(0, EventFactory.createDefaultRpcResponseEvent(46,
                EventFactory.DEFAULT_VERSION, getFeaturesOutputBuilder));

        executeNow();
        Assert.assertEquals(ConnectionConductor.CONDUCTOR_STATE.WORKING,
                connectionConductor.getConductorState());
        Assert.assertEquals((short) 0x01, connectionConductor.getVersion()
                .shortValue());
    }

    /**
     * Test of handshake, covering version negotiation and features .
     * @throws Exception
     */
    @Test
    public void testHandshake3() throws Exception {
        eventPlan.add(0, EventFactory.createDefaultNotificationEvent(42L,
                (short) 0x00, new HelloMessageBuilder()));

        executeNow();
        Assert.assertEquals(ConnectionConductor.CONDUCTOR_STATE.HANDSHAKING,
                connectionConductor.getConductorState());
        Assert.assertNull(connectionConductor.getVersion());
    }

    /**
     * Test method for
     * {@link org.opendaylight.openflowplugin.openflow.md.core.ConnectionConductorImpl#onExperimenterMessage(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterMessage)}
     * .
     * @throws InterruptedException
     */
    @Test
    public void testOnExperimenterMessage1() throws InterruptedException {
        eventPlan.add(0,
                EventFactory.createDefaultWaitForRpcEvent(42, "experimenter"));
        ExperimenterMessageBuilder builder1 = new ExperimenterMessageBuilder();
        builder1.setExperimenter(84L).setExpType(4L);
        eventPlan.add(0, EventFactory.createDefaultNotificationEvent(42L,
                EventFactory.DEFAULT_VERSION, builder1));
        executeLater();

        Runnable sendExperimenterCmd = new Runnable() {

            @Override
            public void run() {
                ExperimenterInputBuilder builder2 = new ExperimenterInputBuilder();
                builder2.setExperimenter(84L).setExpType(4L);
                EventFactory.setupHeader(42L, builder2);
                adapter.experimenter(builder2.build());
            }
        };
        pool.schedule(sendExperimenterCmd,
                ConnectionAdapterStackImpl.JOB_DELAY, TimeUnit.MILLISECONDS);
    }

    /**
     * Test method for
     * {@link org.opendaylight.openflowplugin.openflow.md.core.ConnectionConductorImpl#onExperimenterMessage(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterMessage)}
     * .
     * @throws InterruptedException
     */
    @Test
    public void testOnExperimenterMessage2() throws InterruptedException {
        eventPlan.add(0,
                EventFactory.createDefaultWaitForRpcEvent(42, "experimenter"));
        ErrorMessageBuilder builder1 = new ErrorMessageBuilder();
        builder1.setType(ErrorType.BADREQUEST).setCode(3)
                .setData(new byte[] { 1, 2, 3 });

        eventPlan.add(0, EventFactory.createDefaultNotificationEvent(42L,
                EventFactory.DEFAULT_VERSION, builder1));

        executeLater();

        Runnable sendExperimenterCmd = new Runnable() {

            @Override
            public void run() {
                ExperimenterInputBuilder builder2 = new ExperimenterInputBuilder();
                builder2.setExperimenter(84L).setExpType(4L);
                EventFactory.setupHeader(42L, builder2);
                adapter.experimenter(builder2.build());
            }
        };
        pool.schedule(sendExperimenterCmd,
                ConnectionAdapterStackImpl.JOB_DELAY, TimeUnit.MILLISECONDS);
    }

    /**
     * Test method for
     * {@link org.opendaylight.openflowplugin.openflow.md.core.ConnectionConductorImpl#onFlowRemovedMessage(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemovedMessage)}
     * .
     */
    @Test
    public void testOnFlowRemovedMessage() {
        IMDMessageListener objFms = new FlowRemovedMessageService() ;
        Map<Class<? extends DataObject>, Collection<IMDMessageListener>> listenerMapping = new HashMap<Class<? extends DataObject>, Collection<IMDMessageListener>>();
        Collection<IMDMessageListener> existingValues = new ArrayList<IMDMessageListener>();
        existingValues.add(objFms);
        listenerMapping.put(FlowRemovedMessage.class, existingValues);
        connectionConductor.setListenerMapping(listenerMapping);
        FlowRemovedMessageBuilder builder1 = new FlowRemovedMessageBuilder();
        builder1.setXid(1L);
        connectionConductor.onFlowRemovedMessage(builder1.build());
        Assert.assertEquals(1, flowremovedMessageCounter);
        builder1.setXid(2L);
        connectionConductor.onFlowRemovedMessage(builder1.build());
        Assert.assertEquals(2, flowremovedMessageCounter);
    }

    /**
     * Test method for
     * {@link org.opendaylight.openflowplugin.openflow.md.core.ConnectionConductorImpl#onMultipartReplyMessage(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage)}
     * .
     */
    @Test
    public void testOnMultipartReplyMessage() {
        // fail("Not yet implemented");
        // TODO:: add test
    }

    /**
     * Test method for
     * {@link org.opendaylight.openflowplugin.openflow.md.core.ConnectionConductorImpl#onMultipartRequestMessage(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestMessage)}
     * .
     */
    @Test
    public void testOnMultipartRequestMessage() {
        // fail("Not yet implemented");
        // TODO:: add test
    }

    /**
     * Test method for
     * {@link org.opendaylight.openflowplugin.openflow.md.core.ConnectionConductorImpl#onPacketInMessage(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessage)}
     * .
     */
    @Test
    public void testOnPacketInMessage() {
        IMDMessageListener objPms = new PacketInMessageService() ;
        Map<Class<? extends DataObject>, Collection<IMDMessageListener>> listenerMapping = new HashMap<Class<? extends DataObject>, Collection<IMDMessageListener>>();
        Collection<IMDMessageListener> existingValues = new ArrayList<IMDMessageListener>();
        existingValues.add(objPms);
        listenerMapping.put(PacketInMessage.class, existingValues);
        connectionConductor.setListenerMapping(listenerMapping);
        PacketInMessageBuilder builder1 = new PacketInMessageBuilder();
        builder1.setBufferId((long)1);
        connectionConductor.onPacketInMessage(builder1.build());
        Assert.assertEquals(1, packetinMessageCounter);
        builder1.setBufferId((long)2);
        connectionConductor.onPacketInMessage(builder1.build());
        Assert.assertEquals(2, packetinMessageCounter);
    }

    /**
     * Test method for
     * {@link org.opendaylight.openflowplugin.openflow.md.core.ConnectionConductorImpl#onPortStatusMessage(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortStatusMessage)}
     * .
     */
    @Test
    public void testOnPortStatusMessage() {
        GetFeaturesOutputBuilder builder = new GetFeaturesOutputBuilder();
        builder.setDatapathId(new BigInteger("102030405060"));
        builder.setAuxiliaryId((short) 0);
        OFSessionUtil.registerSession(connectionConductor,
                builder.build(), (short)0x04);
        IMDMessageListener objPSms = new PortStatusMessageService() ;
        Map<Class<? extends DataObject>, Collection<IMDMessageListener>> listenerMapping = new HashMap<Class<? extends DataObject>, Collection<IMDMessageListener>>();
        Collection<IMDMessageListener> existingValues = new ArrayList<IMDMessageListener>();
        existingValues.add(objPSms);
        listenerMapping.put(PortStatusMessage.class, existingValues);
        connectionConductor.setListenerMapping(listenerMapping);
        PortStatusMessageBuilder builder1 = new PortStatusMessageBuilder();
        PortFeatures features = new PortFeatures(true,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false);
        builder1.setPortNo(90L).setReason(PortReason.OFPPRADD).setCurrentFeatures(features);
        connectionConductor.onPortStatusMessage(builder1.build());
        Assert.assertEquals(1, portstatusAddMessageCounter);
        builder1.setPortNo(90L).setReason(PortReason.OFPPRMODIFY).setCurrentFeatures(features);
        connectionConductor.onPortStatusMessage(builder1.build());
        Assert.assertEquals(1, portstatusModifyMessageCounter);
        builder1.setPortNo(90L).setReason(PortReason.OFPPRDELETE).setCurrentFeatures(features);
        connectionConductor.onPortStatusMessage(builder1.build());
        Assert.assertEquals(1, portstatusDeleteMessageCounter);
    }

    /**
     * Test method for
     * {@link org.opendaylight.openflowplugin.openflow.md.core.ConnectionConductorImpl#proposeVersion(short)}
     * .
     */
    @Test
    public void testProposeVersion() {
        short[] remoteVer = new short[] { 0x05, 0x04, 0x03, 0x02, 0x01, 0x8f,
                0xff };
        short[] expectedProposal = new short[] { 0x04, 0x04, 0x01, 0x01, 0x01,
                0x04, 0x04 };

        for (int i = 0; i < remoteVer.length; i++) {
            short actualProposal = connectionConductor
                    .proposeVersion(remoteVer[i]);
            Assert.assertEquals(
                    String.format("proposing for version: %04x", remoteVer[i]),
                    expectedProposal[i], actualProposal);
        }

        try {
            connectionConductor.proposeVersion((short) 0);
            Assert.fail("there should be no proposition for this version");
        } catch (Exception e) {
            // expected
        }
    }

    /**
     * @throws InterruptedException
     */
    private void executeLater() throws InterruptedException {
        execute(false);
    }

    /**
     * @throws InterruptedException
     */
    private void executeNow() throws InterruptedException {
        execute(true);
    }

    /**
     * @throws InterruptedException
     */
    private void execute(boolean join) throws InterruptedException {
        libSimulation = new Thread(adapter, "junit-adapter");
        libSimulation.start();
        if (join) {
            libSimulation.join();
        }
    }

    private class ExperimenterMessageService implements IMDMessageListener {
        @Override
        public void receive(SwitchConnectionDistinguisher cookie, SessionContext sw, DataObject msg) {
            LOG.debug("Received a packet in Experimenter Service");
            experimenterMessageCounter++;
        }
    }

    private class PacketInMessageService implements IMDMessageListener {
        @Override
        public void receive(SwitchConnectionDistinguisher cookie, SessionContext sw, DataObject msg) {
            LOG.debug("Received a packet in PacketIn Service");
            packetinMessageCounter++;
        }
    }

    private class FlowRemovedMessageService implements IMDMessageListener {
        @Override
        public void receive(SwitchConnectionDistinguisher cookie, SessionContext sw, DataObject msg) {
            LOG.debug("Received a packet in FlowRemoved Service");
            flowremovedMessageCounter++;
        }
    }

    private class PortStatusMessageService implements IMDMessageListener {
        @Override
        public void receive(SwitchConnectionDistinguisher cookie, SessionContext sw, DataObject msg) {
            LOG.debug("Received a packet in PortStatus Service");
            if ( (((PortStatusMessage)msg).getReason().equals(PortReason.OFPPRADD))  ) {
                portstatusAddMessageCounter++;
            } else if (((PortStatusMessage)msg).getReason().equals(PortReason.OFPPRDELETE)){
                portstatusDeleteMessageCounter++;
            } else if (((PortStatusMessage)msg).getReason().equals(PortReason.OFPPRMODIFY)) {
                portstatusModifyMessageCounter++;
            }
        }
    }

    /**
     * Test method for
     * {@link org.opendaylight.openflowplugin.openflow.md.core.ConnectionConductorImpl#onExperimenterMessage(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterMessage)}
     * .
     * @throws InterruptedException
     */
    @Test
    public void testOnExperimenterMessage() throws InterruptedException {
        IMDMessageListener objEms = new ExperimenterMessageService() ;
        Map<Class<? extends DataObject>, Collection<IMDMessageListener>> listenerMapping = new HashMap<Class<? extends DataObject>, Collection<IMDMessageListener>>();
        Collection<IMDMessageListener> existingValues = new ArrayList<IMDMessageListener>();
        existingValues.add(objEms);
        listenerMapping.put(ExperimenterMessage.class, existingValues);
        connectionConductor.setListenerMapping(listenerMapping);
        ExperimenterMessageBuilder builder1 = new ExperimenterMessageBuilder();
        builder1.setExperimenter(84L).setExpType(4L);
        connectionConductor.onExperimenterMessage(builder1.build());
        Assert.assertEquals(1, experimenterMessageCounter);
        builder1.setExperimenter(85L).setExpType(4L);
        connectionConductor.onExperimenterMessage(builder1.build());
        Assert.assertEquals(2, experimenterMessageCounter);
    }

}
