/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.md.core.ConnectionConductor;
import org.opendaylight.openflowplugin.api.openflow.md.core.ErrorHandler;
import org.opendaylight.openflowplugin.api.openflow.md.core.HandshakeListener;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.SessionContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.HelloElementType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.hello.Elements;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.hello.ElementsBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * testing handshake
 */
@RunWith(MockitoJUnitRunner.class)
public class HandshakeManagerImplTest {

    private static final Logger LOG = LoggerFactory
            .getLogger(HandshakeManagerImplTest.class);

    private HandshakeManagerImpl handshakeManager;
    @Mock
    private ConnectionAdapter adapter;
    @Mock
    private ErrorHandler errorHandler;
    @Mock
    private HandshakeListener handshakeListener;

    private RpcResult<GetFeaturesOutput> resultFeatures;

    private long helloXid = 42L;

    private int expectedErrors = 0;

    /**
     * invoked before every test method
     */
    @Before
    public void setUp() {
        handshakeManager = new HandshakeManagerImpl(adapter, OFConstants.OFP_VERSION_1_3,
                ConnectionConductor.VERSION_ORDER);
        handshakeManager.setErrorHandler(errorHandler);
        handshakeManager.setHandshakeListener(handshakeListener);
        handshakeManager.setUseVersionBitmap(false);

        resultFeatures = RpcResultBuilder.success(new GetFeaturesOutputBuilder().build()).build();

        Mockito.when(adapter.hello(Matchers.any(HelloInput.class)))
            .thenReturn(Futures.immediateFuture(
                    RpcResultBuilder.success((Void) null).build()));
    }

    /**
     * invoked after each test method
     */
    @After
    public void teardown() {
        // logging errors if occurred
        ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);
        Mockito.verify(errorHandler, Mockito.atMost(1)).handleException(
                errorCaptor.capture(), Matchers.any(SessionContext.class));
        for (Throwable problem : errorCaptor.getAllValues()) {
            LOG.warn(problem.getMessage(), problem);
        }

        Mockito.verify(errorHandler, Mockito.times(expectedErrors)).handleException(
                Matchers.any(Throwable.class), Matchers.any(SessionContext.class));
    }

    /**
     * Test method for {@link org.opendaylight.openflowplugin.openflow.md.core.HandshakeManagerImpl#proposeCommonBitmapVersion(java.util.List)}.
     */
    @Test
    public void testProposeCommonBitmapVersion() {
        Boolean[][] versions = new Boolean[][] {
                {true, true, true, false, false, false},
                {true, true, true, false, false}
        };

        for (Boolean[] verasionList : versions) {
            ElementsBuilder elementsBuilder = new ElementsBuilder();
            elementsBuilder.setVersionBitmap(Lists.newArrayList(verasionList));
            Elements element = elementsBuilder.build();
            List<Elements> elements = Lists.newArrayList(element );
            Short proposal = handshakeManager.proposeCommonBitmapVersion(elements);
            Assert.assertEquals(Short.valueOf((short)1), proposal);
        }
    }

    /**
     * Test method for {@link org.opendaylight.openflowplugin.openflow.md.core.HandshakeManagerImpl#proposeNextVersion(short)}.
     */
    @Test
    public void testProposeNextVersion() {
        short[] remoteVer = new short[] { 0x05, 0x04, 0x03, 0x02, 0x01, 0x8f,
                0xff };
        short[] expectedProposal = new short[] { 0x04, 0x04, 0x01, 0x01, 0x01,
                0x04, 0x04 };

        for (int i = 0; i < remoteVer.length; i++) {
            short actualProposal = handshakeManager
                    .proposeNextVersion(remoteVer[i]);
            Assert.assertEquals(
                    String.format("proposing for version: %04x", remoteVer[i]),
                    expectedProposal[i], actualProposal);
        }

        try {
            handshakeManager.proposeNextVersion((short) 0);
            Assert.fail("there should be no proposition for this version");
        } catch (Exception e) {
            // expected
        }
    }

    //////// Version Negotiation Tests //////////////

    /**
     * Test of version negotiation Where switch version = 1.0
     *
     * @throws Exception
     */
    @Test
    public void testVersionNegotiation10() throws Exception {
        LOG.debug("testVersionNegotiation10");
        Short version = OFConstants.OFP_VERSION_1_0;

        Mockito.when(adapter.getFeatures(Matchers.any(GetFeaturesInput.class)))
            .thenReturn(Futures.immediateFuture(resultFeatures));

        handshakeManager.shake(null);

        handshakeManager.shake(createHelloMessage(version, helloXid).build());

        Mockito.verify(handshakeListener).onHandshakeSuccessful(resultFeatures.getResult(), version);
    }

    /**
     * Test of version negotiation Where switch version = 1.0
     *
     * @throws Exception
     */
    @Test
    public void testVersionNegotiation10SwitchStarts() throws Exception {
        LOG.debug("testVersionNegotiation10-ss");
        Short version = OFConstants.OFP_VERSION_1_0;

        Mockito.when(adapter.getFeatures(Matchers.any(GetFeaturesInput.class)))
            .thenReturn(Futures.immediateFuture(resultFeatures));

        handshakeManager.shake(createHelloMessage(version, helloXid).build());

        Mockito.verify(handshakeListener).onHandshakeSuccessful(resultFeatures.getResult(), version);
    }

    /**
     * Test of version negotiation Where switch version < 1.0
     * Switch delivers first helloMessage with version 0x00 = negotiation unsuccessful
     * @throws Exception
     */
    @Test
    public void testVersionNegotiation00() throws Exception {
        LOG.debug("testVersionNegotiation00");
        expectedErrors = 1;
        Short version = (short) 0x00;

        handshakeManager.shake(createHelloMessage(version, helloXid).build());

        Mockito.verify(handshakeListener, Mockito.never()).onHandshakeSuccessful(
                Matchers.any(GetFeaturesOutput.class), Matchers.anyShort());
    }

    /**
     * Test of version negotiation Where switch version < 1.0
     * Switch delivers first helloMessage with version 0x00 = negotiation unsuccessful
     * @throws Exception
     */
    @Test
    public void testVersionNegotiation00SwitchStarts() throws Exception {
        LOG.debug("testVersionNegotiation00-ss");
        expectedErrors = 1;
        Short version = (short) 0x00;

        handshakeManager.shake(null);

        handshakeManager.shake(createHelloMessage(version, helloXid).build());

        Mockito.verify(handshakeListener, Mockito.never()).onHandshakeSuccessful(
                Matchers.any(GetFeaturesOutput.class), Matchers.anyShort());
    }

    /**
     * Test of version negotiation Where 1.0 < switch version < 1.3
     *
     * @throws Exception
     */
    @Test
    public void testVersionNegotiation11() throws Exception {
        LOG.debug("testVersionNegotiation11");
        Short version = (short) 0x02;
        Short expVersion = (short) 0x01;

        Mockito.when(adapter.getFeatures(Matchers.any(GetFeaturesInput.class)))
            .thenReturn(Futures.immediateFuture(resultFeatures));

        handshakeManager.shake(createHelloMessage(version, helloXid).build());

        handshakeManager.shake(createHelloMessage(expVersion, helloXid).build());

        Mockito.verify(handshakeListener).onHandshakeSuccessful(
                resultFeatures.getResult(), expVersion);
    }

    /**
     * Test of version negotiation Where 1.0 < switch version < 1.3
     *
     * @throws Exception
     */
    @Test
    public void testVersionNegotiation11SwitchStarts() throws Exception {
        LOG.debug("testVersionNegotiation11-ss");
        Short version = (short) 0x02;
        Short expVersion = (short) 0x01;

        Mockito.when(adapter.getFeatures(Matchers.any(GetFeaturesInput.class)))
            .thenReturn(Futures.immediateFuture(resultFeatures));

        handshakeManager.shake(null);

        handshakeManager.shake(createHelloMessage(version, helloXid).build());

        handshakeManager.shake(createHelloMessage(expVersion, helloXid).build());

        Mockito.verify(handshakeListener).onHandshakeSuccessful(
                resultFeatures.getResult(), expVersion);
    }

    /**
     * Test of version negotiation Where switch version = 1.3
     *
     * @throws Exception
     */
    @Test
    public void testVersionNegotiation13() throws Exception {
        LOG.debug("testVersionNegotiation13");
        Short version = OFConstants.OFP_VERSION_1_3;

        Mockito.when(adapter.getFeatures(Matchers.any(GetFeaturesInput.class)))
            .thenReturn(Futures.immediateFuture(resultFeatures));

        handshakeManager.shake(createHelloMessage(version, helloXid).build());

        Mockito.verify(handshakeListener).onHandshakeSuccessful(
                resultFeatures.getResult(), version);
    }

    /**
     * Test of version negotiation Where switch version = 1.3
     *
     * @throws Exception
     */
    @Test
    public void testVersionNegotiation13SwitchStarts() throws Exception {
        LOG.debug("testVersionNegotiation13-ss");
        Short version = OFConstants.OFP_VERSION_1_3;

        Mockito.when(adapter.getFeatures(Matchers.any(GetFeaturesInput.class)))
            .thenReturn(Futures.immediateFuture(resultFeatures));

        handshakeManager.shake(null);

        handshakeManager.shake(createHelloMessage(version, helloXid).build());

        Mockito.verify(handshakeListener).onHandshakeSuccessful(
                resultFeatures.getResult(), version);
    }

    /**
     * Test of version negotiation Where switch version >= 1.3
     *
     * @throws Exception
     */
    @Test
    public void testVersionNegotiation15() throws Exception {
        LOG.debug("testVersionNegotiation15");
        Short version = (short) 0x06;
        Short expVersion =  OFConstants.OFP_VERSION_1_3;

        Mockito.when(adapter.getFeatures(Matchers.any(GetFeaturesInput.class)))
            .thenReturn(Futures.immediateFuture(resultFeatures));

        handshakeManager.shake(createHelloMessage(version, helloXid).build());

        handshakeManager.shake(createHelloMessage(expVersion, helloXid).build());

        Mockito.verify(handshakeListener).onHandshakeSuccessful(
                resultFeatures.getResult(), expVersion);
    }

    /**
     * Test of version negotiation Where switch version >= 1.3
     *
     * @throws Exception
     */
    @Test
    public void testVersionNegotiation15SwitchStart() throws Exception {
        LOG.debug("testVersionNegotiation15-ss");
        Short version = (short) 0x06;
        Short expVersion =  OFConstants.OFP_VERSION_1_3;

        Mockito.when(adapter.getFeatures(Matchers.any(GetFeaturesInput.class)))
            .thenReturn(Futures.immediateFuture(resultFeatures));

        handshakeManager.shake(createHelloMessage(version, helloXid).build());

        handshakeManager.shake(createHelloMessage(expVersion, helloXid).build());

        Mockito.verify(handshakeListener).onHandshakeSuccessful(
                resultFeatures.getResult(), expVersion);
    }

    /**
     * Test of version negotiation Where switch version > 1.3
     *
     * @throws Exception
     */
    @Test
    public void testVersionNegotiation15_MultipleCall() throws Exception {
        LOG.debug("testVersionNegotiation15_MultipleCall");
        Short version = (short) 0x06;
        expectedErrors = 1;

        handshakeManager.shake(createHelloMessage(version, helloXid).build());

        handshakeManager.shake(createHelloMessage(version, helloXid).build());

        Mockito.verify(handshakeListener, Mockito.never()).onHandshakeSuccessful(
                Matchers.any(GetFeaturesOutput.class), Matchers.anyShort());
    }

    /**
     * Test of version negotiation Where switch version > 1.3
     *
     * @throws Exception
     */
    @Test
    public void testVersionNegotiation15_MultipleCallSwitchStarts() throws Exception {
        LOG.debug("testVersionNegotiation15_MultipleCall-ss");
        Short version = (short) 0x06;
        expectedErrors = 1;

        handshakeManager.shake(null);

        handshakeManager.shake(createHelloMessage(version, helloXid).build());

        handshakeManager.shake(createHelloMessage(version, helloXid).build());

        Mockito.verify(handshakeListener, Mockito.never()).onHandshakeSuccessful(
                Matchers.any(GetFeaturesOutput.class), Matchers.anyShort());
    }

    /**
     * Test of version negotiation Where bitmap version {0x05,0x01}
     *
     * @throws Exception
     */
    @Test
    public void testVersionNegotiation10InBitmap() throws Exception {
        LOG.debug("testVersionNegotiation10InBitmap");
        Short version = OFConstants.OFP_VERSION_1_0;
        handshakeManager.setUseVersionBitmap(true);

        HelloMessageBuilder helloMessage = createHelloMessage(version, helloXid);
        addVersionBitmap(Lists.newArrayList((short) 0x05, OFConstants.OFP_VERSION_1_0), helloMessage);

        Mockito.when(adapter.getFeatures(Matchers.any(GetFeaturesInput.class)))
            .thenReturn(Futures.immediateFuture(resultFeatures));

        handshakeManager.shake(helloMessage.build());

        Mockito.verify(handshakeListener).onHandshakeSuccessful(
                resultFeatures.getResult(), version);
    }

    /**
     * Test of version negotiation Where bitmap version {0x05,0x01}
     *
     * @throws Exception
     */
    @Test
    public void testVersionNegotiation10InBitmapSwitchStarts() throws Exception {
        LOG.debug("testVersionNegotiation10InBitmap-ss");
        Short version = OFConstants.OFP_VERSION_1_0;
        handshakeManager.setUseVersionBitmap(true);

        HelloMessageBuilder helloMessage = createHelloMessage(version, helloXid);
        addVersionBitmap(Lists.newArrayList((short) 0x05, OFConstants.OFP_VERSION_1_0), helloMessage);

        Mockito.when(adapter.getFeatures(Matchers.any(GetFeaturesInput.class)))
            .thenReturn(Futures.immediateFuture(resultFeatures));

        handshakeManager.shake(null);

        handshakeManager.shake(helloMessage.build());

        Mockito.verify(handshakeListener).onHandshakeSuccessful(
                resultFeatures.getResult(), version);
    }

    /**
     * Test of version negotiation Where bitmap version {0x05,0x04}
     *
     * @throws Exception
     */
    @Test
    public void testVersionNegotiation13InBitmap() throws Exception {
        LOG.debug("testVersionNegotiation13InBitmap");
        Short version = OFConstants.OFP_VERSION_1_3;
        handshakeManager.setUseVersionBitmap(true);

        HelloMessageBuilder helloMessage = createHelloMessage(version, helloXid);
        addVersionBitmap(Lists.newArrayList((short) 0x05, OFConstants.OFP_VERSION_1_3), helloMessage);

        Mockito.when(adapter.getFeatures(Matchers.any(GetFeaturesInput.class)))
            .thenReturn(Futures.immediateFuture(resultFeatures));

        handshakeManager.shake(helloMessage.build());

        Mockito.verify(handshakeListener).onHandshakeSuccessful(
                resultFeatures.getResult(), version);
    }

    /**
     * Test of version negotiation Where bitmap version {0x05,0x04}
     *
     * @throws Exception
     */
    @Test
    public void testVersionNegotiation13InBitmapSwitchFirst() throws Exception {
        LOG.debug("testVersionNegotiation13InBitmap-ss");
        Short version = OFConstants.OFP_VERSION_1_3;
        handshakeManager.setUseVersionBitmap(true);

        HelloMessageBuilder helloMessage = createHelloMessage(version, helloXid);
        addVersionBitmap(Lists.newArrayList((short) 0x05, OFConstants.OFP_VERSION_1_3), helloMessage);

        Mockito.when(adapter.getFeatures(Matchers.any(GetFeaturesInput.class)))
            .thenReturn(Futures.immediateFuture(resultFeatures));

        handshakeManager.shake(null);

        handshakeManager.shake(helloMessage.build());

        Mockito.verify(handshakeListener).onHandshakeSuccessful(
                resultFeatures.getResult(), version);
    }

    /**
     * Test of version negotiation Where bitmap version {0x05,0x02}
     *
     * @throws Exception
     */
    @Test
    public void testVersionNegotiationNoCommonVersionInBitmap() throws Exception {
        LOG.debug("testVersionNegotiationNoCommonVersionInBitmap");
        Short version = (short) 0x05;
        expectedErrors = 1;
        handshakeManager.setUseVersionBitmap(true);

        HelloMessageBuilder helloMessage = createHelloMessage(version, helloXid);
        addVersionBitmap(Lists.newArrayList((short) 0x05, (short) 0x02), helloMessage);

        handshakeManager.shake(helloMessage.build());

        Mockito.verify(handshakeListener, Mockito.never()).onHandshakeSuccessful(
                Matchers.any(GetFeaturesOutput.class), Matchers.anyShort());
    }

    /**
     * Test of version negotiation Where bitmap version {0x05,0x02}
     *
     * @throws Exception
     */
    @Test
    public void testVersionNegotiationNoCommonVersionInBitmapSwitchStarts() throws Exception {
        LOG.debug("testVersionNegotiationNoCommonVersionInBitmap-ss");
        Short version = (short) 0x05;
        expectedErrors = 1;
        handshakeManager.setUseVersionBitmap(true);

        HelloMessageBuilder helloMessage = createHelloMessage(version, helloXid);
        addVersionBitmap(Lists.newArrayList((short) 0x05, (short) 0x02), helloMessage);

        handshakeManager.shake(null);

        handshakeManager.shake(helloMessage.build());

        Mockito.verify(handshakeListener, Mockito.never()).onHandshakeSuccessful(
                Matchers.any(GetFeaturesOutput.class), Matchers.anyShort());
    }


    /**
     * @param ofpVersion10
     * @param helloXid
     * @return
     */
    private static HelloMessageBuilder createHelloMessage(short ofpVersion10, long helloXid) {
        return new HelloMessageBuilder().setVersion(ofpVersion10).setXid(helloXid);
    }

    /**
     * @param versionOrder
     * @param helloBuilder
     * @return
     */
    private static HelloMessageBuilder addVersionBitmap(List<Short> versionOrder,
            HelloMessageBuilder helloBuilder) {
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

        helloBuilder.setElements(elementList);
        return helloBuilder;
    }

}
