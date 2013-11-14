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
import java.util.List;
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
import org.opendaylight.openflowplugin.openflow.md.core.session.SessionContext;
import org.opendaylight.openflowplugin.openflow.md.queue.PopListener;
import org.opendaylight.openflowplugin.openflow.md.queue.QueueKeeperLightImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.Capabilities;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ErrorType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.HelloElementType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortReason;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoRequestMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemovedMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemovedMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortStatusMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortStatusMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.hello.Elements;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.hello.ElementsBuilder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * @author mirehak
 */
public class ConnectionConductorImplTest {

    protected static final Logger LOG = LoggerFactory
            .getLogger(ConnectionConductorImplTest.class);

    /** in [ms] */
    private final int maxProcessingTimeout = 500;

    protected ConnectionAdapterStackImpl adapter;
    private ConnectionConductorImpl connectionConductor;
    private MDController controller;
    private Stack<SwitchTestEvent> eventPlan;

    private Thread libSimulation;
    private ScheduledThreadPoolExecutor pool = new ScheduledThreadPoolExecutor(
            8);

    private QueueKeeperLightImpl queueKeeper;

    private PopListener<DataObject> popListener;

    private int experimenterMessageCounter;
    private int packetinMessageCounter;
    private int flowremovedMessageCounter;
    private int portstatusAddMessageCounter;
    private int portstatusDeleteMessageCounter;
    private int portstatusModifyMessageCounter;
    private int errorMessageCounter;

    private ErrorHandlerQueueImpl errorHandler;
    
    public void incrExperimenterMessageCounter() {
        this.experimenterMessageCounter++;
    }

    public void incrPacketinMessageCounter() {
        this.packetinMessageCounter++;
    }

    public void incrFlowremovedMessageCounter() {
        this.flowremovedMessageCounter++;
    }

    public void incrPortstatusAddMessageCounter() {
        this.portstatusAddMessageCounter++;
    }

    public void incrPortstatusDeleteMessageCounter() {
        this.portstatusDeleteMessageCounter++;
    }

    public void incrPortstatusModifyMessageCounter() {
        this.portstatusModifyMessageCounter++;
    }

    public void incrErrorMessageCounter() {
        this.errorMessageCounter++;
    }


    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        adapter = new ConnectionAdapterStackImpl();

        popListener = new PopListenerCountingImpl<>();
        
        queueKeeper = new QueueKeeperLightImpl();
        queueKeeper.init();

        connectionConductor = new ConnectionConductorImpl(adapter);
        connectionConductor.setQueueKeeper(queueKeeper);
        connectionConductor.init();
        errorHandler = new ErrorHandlerQueueImpl();
        pool.execute(errorHandler);
        connectionConductor.setErrorHandler(errorHandler);
        controller = new MDController();
        controller.init();
        queueKeeper.setTranslatorMapping(controller.getMessageTranslators());
        eventPlan = new Stack<>();
        adapter.setEventPlan(eventPlan);
        adapter.setProceedTimeout(5000L);
        adapter.checkListeners();
        
        controller.getMessageTranslators().putAll(assembleTranslatorMapping());
        queueKeeper.setPopListenersMapping(assemblePopListenerMapping());
    }

    /**
     * @return
     */
    private Map<Class<? extends DataObject>, Collection<PopListener<DataObject>>> assemblePopListenerMapping() {
        Map<Class<? extends DataObject>, Collection<PopListener<DataObject>>> mapping = new HashMap<>();
        Collection<PopListener<DataObject>> popListenerBag = new ArrayList<>();
        popListenerBag.add(popListener);
        //TODO: add testing registered types
        mapping.put(DataObject.class, popListenerBag);
        return mapping;
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        if (libSimulation != null) {
            libSimulation.join();
        }
        queueKeeper.shutdown();
        connectionConductor.shutdownPool();
        
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
        controller = null;
        errorHandler = null;
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
        eventPlan.add(0, EventFactory.createConnectionReadyCallback(connectionConductor));
        eventPlan.add(0, EventFactory.createDefaultNotificationEvent(42L,
                EventFactory.DEFAULT_VERSION, new HelloMessageBuilder()));
        eventPlan.add(0, EventFactory.createDefaultWaitForAllEvent(
                EventFactory.createDefaultWaitForRpcEvent(21, "helloReply"),
                EventFactory.createDefaultWaitForRpcEvent(43, "getFeatures")));
        eventPlan.add(0, EventFactory.createDefaultRpcResponseEvent(43,
                EventFactory.DEFAULT_VERSION, getFeatureResponseMsg()));

        executeNow();
        
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
        connectionConductor.setBitmapNegotiationEnable(false);
        eventPlan.add(0, EventFactory.createConnectionReadyCallback(connectionConductor));
        eventPlan.add(0, EventFactory.createDefaultNotificationEvent(42L,
                (short) 0x05, new HelloMessageBuilder()));
        eventPlan.add(0,
                EventFactory.createDefaultWaitForRpcEvent(21, "helloReply"));
        eventPlan.add(0, EventFactory.createDefaultNotificationEvent(43L,
                (short) 0x03, new HelloMessageBuilder()));
        eventPlan.add(0,
                EventFactory.createDefaultWaitForRpcEvent(44, "helloReply"));
        eventPlan.add(0, EventFactory.createDefaultNotificationEvent(44L,
                (short) 0x01, new HelloMessageBuilder()));
        eventPlan.add(0,
                EventFactory.createDefaultWaitForRpcEvent(45, "getFeatures"));

        eventPlan.add(0, EventFactory.createDefaultRpcResponseEvent(45,
                EventFactory.DEFAULT_VERSION, getFeatureResponseMsg()));

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
     * @throws InterruptedException
     */
    @Test
    public void testOnFlowRemovedMessage() throws InterruptedException {
        IMDMessageTranslator<OfHeader, DataObject> objFms = new FlowRemovedMessageService() ;
        controller.addMessageTranslator(FlowRemovedMessage.class, 4, objFms);
        
        // Complete HandShake
        eventPlan.add(0, EventFactory.createConnectionReadyCallback(connectionConductor));
        eventPlan.add(0, EventFactory.createDefaultNotificationEvent(42L,
                EventFactory.DEFAULT_VERSION, new HelloMessageBuilder()));
        eventPlan.add(0, EventFactory.createDefaultWaitForAllEvent(
                EventFactory.createDefaultWaitForRpcEvent(21, "helloReply"),
                EventFactory.createDefaultWaitForRpcEvent(43, "getFeatures")));
        eventPlan.add(0, EventFactory.createDefaultRpcResponseEvent(43,
          EventFactory.DEFAULT_VERSION, getFeatureResponseMsg()));
        execute(true);

        // Now send Flow Removed messages
        FlowRemovedMessageBuilder builder1 = new FlowRemovedMessageBuilder();
        builder1.setVersion((short) 4);
        builder1.setXid(1L);
        connectionConductor.onFlowRemovedMessage(builder1.build());
        synchronized (popListener) {
            LOG.debug("about to wait for popListener");
            popListener.wait(maxProcessingTimeout);
        }
        Assert.assertEquals(1, flowremovedMessageCounter);
        builder1.setXid(2L);
        connectionConductor.onFlowRemovedMessage(builder1.build());
        synchronized (popListener) {
            popListener.wait(maxProcessingTimeout);
        }
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
     * @throws InterruptedException
     */
    @Test
    public void testOnPacketInMessage() throws InterruptedException {
        IMDMessageTranslator<OfHeader, DataObject> objPms = new PacketInMessageService() ;
        controller.addMessageTranslator(PacketInMessage.class, 4, objPms);
        
        // Complete HandShake
        eventPlan.add(0, EventFactory.createConnectionReadyCallback(connectionConductor));
        eventPlan.add(0, EventFactory.createDefaultNotificationEvent(42L,
        EventFactory.DEFAULT_VERSION, new HelloMessageBuilder()));
        eventPlan.add(0, EventFactory.createDefaultWaitForAllEvent(
                EventFactory.createDefaultWaitForRpcEvent(21, "helloReply"),
                EventFactory.createDefaultWaitForRpcEvent(43, "getFeatures")));
        eventPlan.add(0, EventFactory.createDefaultRpcResponseEvent(43,
          EventFactory.DEFAULT_VERSION, getFeatureResponseMsg()));
        execute(true);

        // Now send PacketIn
        PacketInMessageBuilder builder1 = new PacketInMessageBuilder();
        builder1.setVersion((short) 4);
        builder1.setBufferId((long)1);
        connectionConductor.onPacketInMessage(builder1.build());
        synchronized (popListener) {
            popListener.wait(maxProcessingTimeout);
        }
        Assert.assertEquals(1, packetinMessageCounter);
        builder1.setBufferId((long)2);
        connectionConductor.onPacketInMessage(builder1.build());
        synchronized (popListener) {
            popListener.wait(maxProcessingTimeout);
        }
        Assert.assertEquals(2, packetinMessageCounter);
    }

    /**
     * Test method for
     * {@link org.opendaylight.openflowplugin.openflow.md.core.ConnectionConductorImpl#onPortStatusMessage(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortStatusMessage)}
     * .
     * @throws InterruptedException
     */
    @Test
    public void testOnPortStatusMessage() throws InterruptedException {
        
        IMDMessageTranslator<OfHeader, DataObject> objPSms = new PortStatusMessageService() ;
        controller.addMessageTranslator(PortStatusMessage.class, 4, objPSms);
        
        // Complete HandShake
        eventPlan.add(0, EventFactory.createConnectionReadyCallback(connectionConductor));
        eventPlan.add(0, EventFactory.createDefaultNotificationEvent(42L,
        EventFactory.DEFAULT_VERSION, new HelloMessageBuilder()));
        eventPlan.add(0, EventFactory.createDefaultWaitForAllEvent(
                EventFactory.createDefaultWaitForRpcEvent(21, "helloReply"),
                EventFactory.createDefaultWaitForRpcEvent(43, "getFeatures")));
        eventPlan.add(0, EventFactory.createDefaultRpcResponseEvent(43,
          EventFactory.DEFAULT_VERSION, getFeatureResponseMsg()));
        execute(true);

        // Send Port Status messages
        PortStatusMessageBuilder builder1 = new PortStatusMessageBuilder();
        builder1.setVersion((short) 4);
        PortFeatures features = new PortFeatures(true,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false);
        builder1.setPortNo(90L).setReason(PortReason.OFPPRADD).setCurrentFeatures(features);
        connectionConductor.onPortStatusMessage(builder1.build());
        synchronized (popListener) {
            popListener.wait(maxProcessingTimeout);
        }
        Assert.assertEquals(1, portstatusAddMessageCounter);
        builder1.setPortNo(90L).setReason(PortReason.OFPPRMODIFY).setCurrentFeatures(features);
        connectionConductor.onPortStatusMessage(builder1.build());
        synchronized (popListener) {
            popListener.wait(maxProcessingTimeout);
        }
        Assert.assertEquals(1, portstatusModifyMessageCounter);
        builder1.setPortNo(90L).setReason(PortReason.OFPPRDELETE).setCurrentFeatures(features);
        connectionConductor.onPortStatusMessage(builder1.build());
        synchronized (popListener) {
            popListener.wait(maxProcessingTimeout);
        }
        Assert.assertEquals(1, portstatusDeleteMessageCounter);
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
        connectionConductor.shutdownPool();
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

    //////// Start - Version Negotiation Test //////////////

    /**
     * Test of version negotiation Where switch version = 1.0
     *
     * @throws Exception
     */
    @Test
    public void testVersionNegotiation10() throws Exception {
        LOG.debug("testVersionNegotiation10");
        Short version = (short) 0x01;
        eventPlan.add(0, EventFactory.createConnectionReadyCallback(connectionConductor));
        eventPlan.add(0, EventFactory.createDefaultNotificationEvent(42, version, new HelloMessageBuilder()));
        eventPlan.add(0, EventFactory.createDefaultWaitForAllEvent(
                EventFactory.createDefaultWaitForRpcEvent(21, "helloReply"),
                EventFactory.createDefaultWaitForRpcEvent(43, "helloReply")));
        eventPlan.add(0, EventFactory.createDefaultWaitForRpcEvent(44, "getFeatures"));
        eventPlan.add(0,
                EventFactory.createDefaultRpcResponseEvent(44, EventFactory.DEFAULT_VERSION, getFeatureResponseMsg()));

        executeNow();
        Assert.assertEquals(version, connectionConductor.getVersion());
    }

    /**
     * Test of version negotiation Where switch version < 1.0
     *
     * @throws Exception
     */
    @Test
    public void testVersionNegotiation00() throws Exception {
        Short version = (short) 0x00;
        eventPlan.add(0, EventFactory.createDefaultNotificationEvent(42L, version, new HelloMessageBuilder()));
        executeNow();
        Assert.assertNull(connectionConductor.getVersion());
    }

    /**
     * Test of version negotiation Where 1.0 < switch version < 1.3
     *
     * @throws Exception
     */
    @Test
    public void testVersionNegotiation11() throws Exception {
        LOG.debug("testVersionNegotiation11");
        connectionConductor.setBitmapNegotiationEnable(false);
        Short version = (short) 0x02;
        Short expVersion = (short) 0x01;
        Assert.assertNull(connectionConductor.getVersion());
        
        eventPlan.add(0, EventFactory.createConnectionReadyCallback(connectionConductor));
        eventPlan.add(0, EventFactory.createDefaultNotificationEvent(42L, version, new HelloMessageBuilder()));
        eventPlan.add(0, EventFactory.createDefaultWaitForAllEvent(
                EventFactory.createDefaultWaitForRpcEvent(21, "helloReply"),
                EventFactory.createDefaultWaitForRpcEvent(43, "helloReply")));
        eventPlan.add(0, EventFactory.createDefaultNotificationEvent(44, expVersion, new HelloMessageBuilder()));
        eventPlan.add(0, EventFactory.createDefaultWaitForRpcEvent(45, "getFeatures"));
        eventPlan.add(0,
                EventFactory.createDefaultRpcResponseEvent(45, EventFactory.DEFAULT_VERSION, getFeatureResponseMsg()));
        executeNow();
        Assert.assertEquals(expVersion, connectionConductor.getVersion());

    }

    /**
     * Test of version negotiation Where switch version = 1.3
     *
     * @throws Exception
     */
    @Test
    public void testVersionNegotiation13() throws Exception {
        LOG.debug("testVersionNegotiation13");
        Short version = (short) 0x04;
        eventPlan.add(0, EventFactory.createConnectionReadyCallback(connectionConductor));
        eventPlan.add(0, EventFactory.createDefaultNotificationEvent(42L, version, new HelloMessageBuilder()));
        eventPlan.add(0, EventFactory.createDefaultWaitForAllEvent(
                EventFactory.createDefaultWaitForRpcEvent(21, "helloReply"),
                EventFactory.createDefaultWaitForRpcEvent(43, "getFeatures")));
        eventPlan.add(0,
                EventFactory.createDefaultRpcResponseEvent(43, EventFactory.DEFAULT_VERSION, getFeatureResponseMsg()));

        executeNow();
        Assert.assertEquals(version, connectionConductor.getVersion());
    }

    /**
     * Test of version negotiation Where switch version >= 1.3
     *
     * @throws Exception
     */
    @Test
    public void testVersionNegotiation15() throws Exception {
        LOG.debug("testVersionNegotiation15");
        connectionConductor.setBitmapNegotiationEnable(false);
        Short version = (short) 0x06;
        Short expVersion = (short) 0x04;
        Assert.assertNull(connectionConductor.getVersion());
        
        eventPlan.add(0, EventFactory.createConnectionReadyCallback(connectionConductor));
        eventPlan.add(0, EventFactory.createDefaultNotificationEvent(42L, version, new HelloMessageBuilder()));
        eventPlan.add(0, EventFactory.createDefaultWaitForRpcEvent(21, "helloReply"));
        eventPlan.add(0, EventFactory.createDefaultNotificationEvent(44, expVersion, new HelloMessageBuilder()));
        eventPlan.add(0, EventFactory.createDefaultWaitForRpcEvent(45, "getFeatures"));
        eventPlan.add(0,
                EventFactory.createDefaultRpcResponseEvent(45, EventFactory.DEFAULT_VERSION, getFeatureResponseMsg()));
        executeNow();
        Assert.assertEquals(expVersion, connectionConductor.getVersion());
    }

    /**
     * Test of version negotiation Where switch version > 1.3
     *
     * @throws Exception
     */
    @Test
    public void testVersionNegotiation15_MultipleCall() throws Exception {
        LOG.debug("testVersionNegotiation15_MultipleCall");
        connectionConductor.setBitmapNegotiationEnable(false);
        Short version = (short) 0x06;
        Assert.assertNull(connectionConductor.getVersion());

        eventPlan.add(0, EventFactory.createConnectionReadyCallback(connectionConductor));
        eventPlan.add(0, EventFactory.createDefaultWaitForRpcEvent(21, "helloReply"));
        eventPlan.add(0, EventFactory.createDefaultNotificationEvent(42, version, new HelloMessageBuilder()));
        eventPlan.add(0, EventFactory.createDefaultNotificationEvent(44, version, new HelloMessageBuilder()));
        executeNow();
        // TODO : check for connection termination
        Assert.assertNull(connectionConductor.getVersion());
    }

    /**
     * Test of version negotiation Where bitmap version {0x05,0x01}
     *
     * @throws Exception
     */
    @Test
    public void testVersionNegotiation10InBitmap() throws Exception {
        LOG.debug("testVersionNegotiation10InBitmap");
        Short version = (short) 0x01;
        
        eventPlan.add(0, EventFactory.createConnectionReadyCallback(connectionConductor));
        eventPlan.add(
                0,
                EventFactory.createDefaultNotificationEvent(42L, (short) 0x05,
                        getHelloBitmapMessage(Lists.newArrayList((short) 0x05, (short) 0x01))));
        eventPlan.add(0, EventFactory.createDefaultWaitForAllEvent(
                EventFactory.createDefaultWaitForRpcEvent(21, "helloReply"),
                EventFactory.createDefaultWaitForRpcEvent(43, "getFeatures")));
        eventPlan.add(0,
                EventFactory.createDefaultRpcResponseEvent(43, EventFactory.DEFAULT_VERSION, getFeatureResponseMsg()));

        executeNow();
        Assert.assertEquals(version, connectionConductor.getVersion());
    }

    /**
     * Test of version negotiation Where bitmap version {0x05,0x04}
     *
     * @throws Exception
     */
    @Test
    public void testVersionNegotiation13InBitmap() throws Exception {
        LOG.debug("testVersionNegotiation13InBitmap");
        Short version = (short) 0x04;
        
        eventPlan.add(0, EventFactory.createConnectionReadyCallback(connectionConductor));
        eventPlan.add(
                0,
                EventFactory.createDefaultNotificationEvent(42L, (short) 0x05,
                        getHelloBitmapMessage(Lists.newArrayList((short) 0x05, (short) 0x04))));
        eventPlan.add(0, EventFactory.createDefaultWaitForAllEvent(
                EventFactory.createDefaultWaitForRpcEvent(21, "helloReply"),
                EventFactory.createDefaultWaitForRpcEvent(43, "getFeatures")));
        eventPlan.add(0,
                EventFactory.createDefaultRpcResponseEvent(43, EventFactory.DEFAULT_VERSION, getFeatureResponseMsg()));

        executeNow();
        Assert.assertEquals(version, connectionConductor.getVersion());
    }

    /**
     * Test of version negotiation Where bitmap version {0x05,0x02}
     *
     * @throws Exception
     */
    @Test
    public void testVersionNegotiationNoCommonVersionInBitmap() throws Exception {
        eventPlan.add(
                0,
                EventFactory.createDefaultNotificationEvent(42L, (short) 0x05,
                        getHelloBitmapMessage(Lists.newArrayList((short) 0x05, (short) 0x02))));
        executeNow();
        Assert.assertNull(connectionConductor.getVersion());
    }

    private HelloMessageBuilder getHelloBitmapMessage(List<Short> versionOrder) {
        short highestVersion = versionOrder.get(0);
        int elementsCount = highestVersion / Integer.SIZE;
        ElementsBuilder elementsBuilder = new ElementsBuilder();

        List<Elements> elementList = new ArrayList<>();
        int orderIndex = versionOrder.size();
        int value = versionOrder.get(--orderIndex);
        for (int index = 0; index <= elementsCount; index++) {
            List<Boolean> booleanList = new ArrayList<>();
            for (int i = 0; i < Integer.SIZE; i++) {
                if (value == ((index * Integer.SIZE) + i)) {
                    booleanList.add(true);
                    value = (orderIndex == 0) ? highestVersion : versionOrder.get(--orderIndex);
                } else {
                    booleanList.add(false);
                }
            }
            elementsBuilder.setType(HelloElementType.forValue(1));
            elementsBuilder.setVersionBitmap(booleanList);
            elementList.add(elementsBuilder.build());
        }

        HelloMessageBuilder builder = new HelloMessageBuilder();
        builder.setXid(10L);
        builder.setVersion(highestVersion);
        builder.setElements(elementList);
        return builder;

    }

    private GetFeaturesOutputBuilder getFeatureResponseMsg() {
        GetFeaturesOutputBuilder getFeaturesOutputBuilder = new GetFeaturesOutputBuilder();
        getFeaturesOutputBuilder.setDatapathId(new BigInteger("102030405060"));
        getFeaturesOutputBuilder.setAuxiliaryId((short) 0);
        getFeaturesOutputBuilder.setBuffers(4L);
        getFeaturesOutputBuilder.setReserved(0L);
        getFeaturesOutputBuilder.setTables((short) 2);
        getFeaturesOutputBuilder.setCapabilities(createCapabilities(84));

        return getFeaturesOutputBuilder;
    }

    /**
     * @return
     */
    private static Capabilities createCapabilities(long input) {
        final Boolean FLOW_STATS = (input & (1 << 0)) != 0;
        final Boolean TABLE_STATS = (input & (1 << 1)) != 0;
        final Boolean PORT_STATS = (input & (1 << 2)) != 0;
        final Boolean GROUP_STATS = (input & (1 << 3)) != 0;
        final Boolean IP_REASM = (input & (1 << 5)) != 0;
        final Boolean QUEUE_STATS = (input & (1 << 6)) != 0;
        final Boolean PORT_BLOCKED = (input & (1 << 8)) != 0;
        Capabilities capabilities = new Capabilities(FLOW_STATS, GROUP_STATS, IP_REASM,
                PORT_BLOCKED, PORT_STATS, QUEUE_STATS, TABLE_STATS);
        return capabilities;
    }
    
    public class ExperimenterMessageService implements IMDMessageTranslator<OfHeader, DataObject> {
        @Override
        public DataObject translate(SwitchConnectionDistinguisher cookie, SessionContext sw, OfHeader msg) {
            LOG.debug("Received a packet in Experimenter Service");
            ConnectionConductorImplTest.this.incrExperimenterMessageCounter();
            return null;
        }
    }

    public class PacketInMessageService implements IMDMessageTranslator<OfHeader, DataObject> {
        @Override
        public DataObject translate(SwitchConnectionDistinguisher cookie, SessionContext sw, OfHeader msg) {
            LOG.debug("Received a packet in PacketIn Service");
            ConnectionConductorImplTest.this.incrPacketinMessageCounter();
            return null;
        }
    }

    public class FlowRemovedMessageService implements IMDMessageTranslator<OfHeader, DataObject> {
        @Override
        public DataObject translate(SwitchConnectionDistinguisher cookie, SessionContext sw, OfHeader msg) {
            LOG.debug("Received a packet in FlowRemoved Service");
            ConnectionConductorImplTest.this.incrFlowremovedMessageCounter();
            return null;
        }
    }

    public class PortStatusMessageService implements IMDMessageTranslator<OfHeader, DataObject> {
        @Override
        public DataObject translate(SwitchConnectionDistinguisher cookie, SessionContext sw, OfHeader msg) {
            LOG.debug("Received a packet in PortStatus Service");
            if ( (((PortStatusMessage)msg).getReason().equals(PortReason.OFPPRADD))  ) {
                ConnectionConductorImplTest.this.incrPortstatusAddMessageCounter();
            } else if (((PortStatusMessage)msg).getReason().equals(PortReason.OFPPRDELETE)){
                ConnectionConductorImplTest.this.incrPortstatusDeleteMessageCounter();
            } else if (((PortStatusMessage)msg).getReason().equals(PortReason.OFPPRMODIFY)) {
                ConnectionConductorImplTest.this.incrPortstatusModifyMessageCounter();
            }
            return null;
        }
    }
    
    public class ErrorMessageService implements IMDMessageTranslator<OfHeader, DataObject> {
        @Override
        public DataObject translate(SwitchConnectionDistinguisher cookie, SessionContext sw, OfHeader msg) {
            LOG.debug("Received a packet in Experimenter Service");
            ConnectionConductorImplTest.this.incrErrorMessageCounter();
            return null;
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
        ExperimenterMessageBuilder builder1 = new ExperimenterMessageBuilder();
        builder1.setVersion((short) 4);
        builder1.setExperimenter(84L).setExpType(4L);
        connectionConductor.onExperimenterMessage(builder1.build());
        synchronized (popListener) {
            popListener.wait(maxProcessingTimeout);
        }
        Assert.assertEquals(1, experimenterMessageCounter);

        builder1.setExperimenter(85L).setExpType(4L);
        connectionConductor.onExperimenterMessage(builder1.build());
        synchronized (popListener) {
            popListener.wait(maxProcessingTimeout);
        }
        Assert.assertEquals(2, experimenterMessageCounter);
    }

    /**
     * Test method for
     * {@link org.opendaylight.openflowplugin.openflow.md.core.ConnectionConductorImpl#onExperimenterMessage(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessage)}
     * .
     * @throws InterruptedException
     */
    @Test
    public void testOnErrorMessage() throws InterruptedException {
        ErrorMessageBuilder builder1 = new ErrorMessageBuilder();
        builder1.setVersion((short) 4);
        builder1.setCode(100);
        connectionConductor.onErrorMessage(builder1.build());
        synchronized (popListener) {
            popListener.wait(maxProcessingTimeout);
        }
        Assert.assertEquals(1, errorMessageCounter);
        builder1.setCode(200);
        connectionConductor.onErrorMessage(builder1.build());
        synchronized (popListener) {
            popListener.wait(maxProcessingTimeout);
        }
        Assert.assertEquals(2, errorMessageCounter);
    }

    /**
     * @return listener mapping for:
     * <ul>
     * <li>experimenter</li>
     * <li>error</li>
     * </ul>
     */
    private Map<TranslatorKey, Collection<IMDMessageTranslator<OfHeader, DataObject>>> assembleTranslatorMapping() {
        Map<TranslatorKey, Collection<IMDMessageTranslator<OfHeader, DataObject>>> translatorMapping = new HashMap<>();
        TranslatorKey tKey;
        
        IMDMessageTranslator<OfHeader, DataObject> objEms = new ExperimenterMessageService() ;
        Collection<IMDMessageTranslator<OfHeader, DataObject>> existingValues = new ArrayList<>();
        existingValues.add(objEms);
        tKey = new TranslatorKey(4, ExperimenterMessage.class.getName());
        translatorMapping.put(tKey, existingValues);
        IMDMessageTranslator<OfHeader, DataObject> objErms = new ErrorMessageService() ;
        existingValues.add(objErms);
        tKey = new TranslatorKey(4, ErrorMessage.class.getName());
        translatorMapping.put(tKey, existingValues);
        return translatorMapping;
    }

}
