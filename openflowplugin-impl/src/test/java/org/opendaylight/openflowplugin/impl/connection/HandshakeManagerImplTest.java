/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.connection;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyShort;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.connection.DeviceConnectionStatusProvider;
import org.opendaylight.openflowplugin.api.openflow.md.core.ErrorHandler;
import org.opendaylight.openflowplugin.api.openflow.md.core.HandshakeListener;
import org.opendaylight.openflowplugin.impl.common.DeviceConnectionRateLimiter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.HelloElementType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.hello.Elements;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.hello.ElementsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.provider.config.rev160510.OpenflowProviderConfigBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * testing handshake.
 */
@RunWith(MockitoJUnitRunner.class)
public class HandshakeManagerImplTest {

    private static final Logger LOG = LoggerFactory.getLogger(HandshakeManagerImplTest.class);
    private static final Uint16 DEVICE_CONNECTION_RATE_LIMIT_PER_MIN = Uint16.ZERO;
    private static final int DEVICE_CONNECTION_HOLD_TIME_IN_SECONDS = 60;

    private HandshakeManagerImpl handshakeManager;
    @Mock
    private ConnectionAdapter adapter;
    @Mock
    private ErrorHandler errorHandler;
    @Mock
    private HandshakeListener handshakeListener;
    @Mock
    private DeviceConnectionStatusProvider deviceConnectionStatusProvider;

    private DeviceConnectionRateLimiter deviceConnectionRateLimiter;

    private RpcResult<GetFeaturesOutput> resultFeatures;

    private final long helloXid = 42L;

    private int expectedErrors = 0;

    /**
     * invoked before every test method.
     */
    @Before
    public void setUp() {
        deviceConnectionRateLimiter = new DeviceConnectionRateLimiter(new OpenflowProviderConfigBuilder()
                .setDeviceConnectionRateLimitPerMin(DEVICE_CONNECTION_RATE_LIMIT_PER_MIN).build());
        handshakeManager = new HandshakeManagerImpl(adapter, OFConstants.OFP_VERSION_1_3, OFConstants.VERSION_ORDER,
                errorHandler, handshakeListener, false, deviceConnectionRateLimiter,
                DEVICE_CONNECTION_HOLD_TIME_IN_SECONDS, deviceConnectionStatusProvider);

        resultFeatures = RpcResultBuilder.success(new GetFeaturesOutputBuilder().setDatapathId(Uint64.ONE).build())
                .build();

        Mockito.when(adapter.hello(any(HelloInput.class)))
                .thenReturn(Futures.immediateFuture(RpcResultBuilder.success((HelloOutput) null).build()));
        Mockito.when(deviceConnectionStatusProvider.getDeviceLastConnectionTime(BigInteger.ONE))
                .thenReturn(LocalDateTime.now().minusSeconds(DEVICE_CONNECTION_HOLD_TIME_IN_SECONDS));
    }

    /**
     * invoked after each test method.
     */
    @After
    public void teardown() {
        // logging errors if occurred
        ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);
        Mockito.verify(errorHandler, Mockito.atMost(1)).handleException(errorCaptor.capture());
        for (Throwable problem : errorCaptor.getAllValues()) {
            LOG.warn(problem.getMessage(), problem);
        }

        Mockito.verify(errorHandler, Mockito.times(expectedErrors)).handleException(any(Throwable.class));
    }

    /**
     * Test method for
     * {@link org.opendaylight.openflowplugin.openflow.md.core
     * .HandshakeManagerImpl#proposeCommonBitmapVersion(java.util.List)}.
     */
    @Test
    public void testProposeCommonBitmapVersion() {
        Boolean[][] versions
                = new Boolean[][]{{true, true, true, false, false, false}, {true, true, true, false, false}};

        for (Boolean[] verasionList : versions) {
            ElementsBuilder elementsBuilder = new ElementsBuilder();
            elementsBuilder.setVersionBitmap(Lists.newArrayList(verasionList));
            Elements element = elementsBuilder.build();
            List<Elements> elements = Lists.newArrayList(element);
            Short proposal = handshakeManager.proposeCommonBitmapVersion(elements);
            Assert.assertEquals(Short.valueOf((short) 1), proposal);
        }
    }

    /**
     * Test method for
     * {@link org.opendaylight.openflowplugin.openflow.md.core.HandshakeManagerImpl#proposeNextVersion(short)}.
     */
    @Test
    @SuppressWarnings("checkstyle:Illegalcatch")
    public void testProposeNextVersion() {
        short[] remoteVer = new short[]{0x05, 0x04, 0x03, 0x02, 0x01, 0x8f, 0xff};
        short[] expectedProposal = new short[]{0x04, 0x04, 0x01, 0x01, 0x01, 0x04, 0x04};

        for (int i = 0; i < remoteVer.length; i++) {
            short actualProposal = handshakeManager.proposeNextVersion(remoteVer[i]);
            Assert.assertEquals(String.format("proposing for version: %04x", remoteVer[i]), expectedProposal[i],
                                actualProposal);
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
     * Test of version negotiation Where switch version = 1.0.
     *
     */
    @Test
    public void testVersionNegotiation10() {
        LOG.debug("testVersionNegotiation10");
        Short version = OFConstants.OFP_VERSION_1_0;

        Mockito.when(adapter.getFeatures(any(GetFeaturesInput.class)))
                .thenReturn(Futures.immediateFuture(resultFeatures));

        handshakeManager.shake(null);

        handshakeManager.shake(createHelloMessage(version, helloXid).build());

        Mockito.verify(handshakeListener).onHandshakeSuccessful(resultFeatures.getResult(), version);
    }

    /**
     * Test of version negotiation Where switch version = 1.0.
     *
     */
    @Test
    public void testVersionNegotiation10SwitchStarts() {
        LOG.debug("testVersionNegotiation10-ss");
        Short version = OFConstants.OFP_VERSION_1_0;

        Mockito.when(adapter.getFeatures(any(GetFeaturesInput.class)))
                .thenReturn(Futures.immediateFuture(resultFeatures));

        handshakeManager.shake(createHelloMessage(version, helloXid).build());

        Mockito.verify(handshakeListener).onHandshakeSuccessful(resultFeatures.getResult(), version);
    }

    /**
     * Test of version negotiation Where switch version < 1.0.
     * Switch delivers first helloMessage with version 0x00 = negotiation unsuccessful
     */
    @Test
    public void testVersionNegotiation00() {
        LOG.debug("testVersionNegotiation00");
        expectedErrors = 1;
        Short version = (short) 0x00;

        handshakeManager.shake(createHelloMessage(version, helloXid).build());

        Mockito.verify(handshakeListener, Mockito.never())
                .onHandshakeSuccessful(any(GetFeaturesOutput.class), anyShort());
    }

    /**
     * Test of version negotiation Where switch version < 1.0.
     * Switch delivers first helloMessage with version 0x00 = negotiation unsuccessful
     */
    @Test
    public void testVersionNegotiation00SwitchStarts() {
        LOG.debug("testVersionNegotiation00-ss");
        expectedErrors = 1;
        Short version = (short) 0x00;

        handshakeManager.shake(null);

        handshakeManager.shake(createHelloMessage(version, helloXid).build());

        Mockito.verify(handshakeListener, Mockito.never())
                .onHandshakeSuccessful(any(GetFeaturesOutput.class), anyShort());
    }

    /**
     * Test of version negotiation Where 1.0 < switch version < 1.3.
     *
     */
    @Test
    public void testVersionNegotiation11() {
        LOG.debug("testVersionNegotiation11");
        Short version = (short) 0x02;
        Short expVersion = (short) 0x01;

        Mockito.when(adapter.getFeatures(any(GetFeaturesInput.class)))
                .thenReturn(Futures.immediateFuture(resultFeatures));

        handshakeManager.shake(createHelloMessage(version, helloXid).build());

        handshakeManager.shake(createHelloMessage(expVersion, helloXid).build());

        Mockito.verify(handshakeListener).onHandshakeSuccessful(resultFeatures.getResult(), expVersion);
    }

    /**
     * Test of version negotiation Where 1.0 < switch version < 1.3.
     */
    @Test
    public void testVersionNegotiation11SwitchStarts() {
        LOG.debug("testVersionNegotiation11-ss");
        final Short version = (short) 0x02;
        final Short expVersion = (short) 0x01;

        Mockito.when(adapter.getFeatures(any(GetFeaturesInput.class)))
                .thenReturn(Futures.immediateFuture(resultFeatures));

        handshakeManager.shake(null);

        handshakeManager.shake(createHelloMessage(version, helloXid).build());

        handshakeManager.shake(createHelloMessage(expVersion, helloXid).build());

        Mockito.verify(handshakeListener).onHandshakeSuccessful(resultFeatures.getResult(), expVersion);
    }

    /**
     * Test of version negotiation Where switch version = 1.3.
     *
     */
    @Test
    public void testVersionNegotiation13() {
        LOG.debug("testVersionNegotiation13");
        Short version = OFConstants.OFP_VERSION_1_3;

        Mockito.when(adapter.getFeatures(any(GetFeaturesInput.class)))
                .thenReturn(Futures.immediateFuture(resultFeatures));

        handshakeManager.shake(createHelloMessage(version, helloXid).build());

        Mockito.verify(handshakeListener).onHandshakeSuccessful(resultFeatures.getResult(), version);
    }

    /**
     * Test of version negotiation Where switch version = 1.3.
     *
     */
    @Test
    public void testVersionNegotiation13SwitchStarts() {
        LOG.debug("testVersionNegotiation13-ss");
        Short version = OFConstants.OFP_VERSION_1_3;

        Mockito.when(adapter.getFeatures(any(GetFeaturesInput.class)))
                .thenReturn(Futures.immediateFuture(resultFeatures));

        handshakeManager.shake(null);

        handshakeManager.shake(createHelloMessage(version, helloXid).build());

        Mockito.verify(handshakeListener).onHandshakeSuccessful(resultFeatures.getResult(), version);
    }

    /**
     * Test of version negotiation Where switch version >= 1.3.
     *
     */
    @Test
    public void testVersionNegotiation15() {
        LOG.debug("testVersionNegotiation15");
        Short version = (short) 0x06;
        Short expVersion = OFConstants.OFP_VERSION_1_3;

        Mockito.when(adapter.getFeatures(any(GetFeaturesInput.class)))
                .thenReturn(Futures.immediateFuture(resultFeatures));

        handshakeManager.shake(createHelloMessage(version, helloXid).build());

        handshakeManager.shake(createHelloMessage(expVersion, helloXid).build());

        Mockito.verify(handshakeListener).onHandshakeSuccessful(resultFeatures.getResult(), expVersion);
    }

    /**
     * Test of version negotiation Where switch version >= 1.3.
     *
     */
    @Test
    public void testVersionNegotiation15SwitchStart() {
        LOG.debug("testVersionNegotiation15-ss");
        Short version = (short) 0x06;
        Short expVersion = OFConstants.OFP_VERSION_1_3;

        Mockito.when(adapter.getFeatures(any(GetFeaturesInput.class)))
                .thenReturn(Futures.immediateFuture(resultFeatures));

        handshakeManager.shake(createHelloMessage(version, helloXid).build());

        handshakeManager.shake(createHelloMessage(expVersion, helloXid).build());

        Mockito.verify(handshakeListener).onHandshakeSuccessful(resultFeatures.getResult(), expVersion);
    }

    /**
     * Test of version negotiation Where switch version > 1.3.
     *
     */
    @Test
    public void testVersionNegotiation15_MultipleCall() {
        LOG.debug("testVersionNegotiation15_MultipleCall");
        Short version = (short) 0x06;
        expectedErrors = 1;

        handshakeManager.shake(createHelloMessage(version, helloXid).build());

        handshakeManager.shake(createHelloMessage(version, helloXid).build());

        Mockito.verify(handshakeListener, Mockito.never())
                .onHandshakeSuccessful(any(GetFeaturesOutput.class), anyShort());
    }

    /**
     * Test of version negotiation Where switch version > 1.3.
     *
     */
    @Test
    public void testVersionNegotiation15_MultipleCallSwitchStarts() {
        LOG.debug("testVersionNegotiation15_MultipleCall-ss");
        Short version = (short) 0x06;
        expectedErrors = 1;

        handshakeManager.shake(null);

        handshakeManager.shake(createHelloMessage(version, helloXid).build());

        handshakeManager.shake(createHelloMessage(version, helloXid).build());

        Mockito.verify(handshakeListener, Mockito.never())
                .onHandshakeSuccessful(any(GetFeaturesOutput.class), anyShort());
    }

    /**
     * Test of version negotiation Where bitmap version {0x05,0x01}.
     *
     */
    @Test
    public void testVersionNegotiation10InBitmap() {
        LOG.debug("testVersionNegotiation10InBitmap");
        Short version = OFConstants.OFP_VERSION_1_0;
        handshakeManager.setUseVersionBitmap(true);

        HelloMessageBuilder helloMessage = createHelloMessage(version, helloXid);
        addVersionBitmap(Lists.newArrayList((short) 0x05, OFConstants.OFP_VERSION_1_0), helloMessage);

        Mockito.when(adapter.getFeatures(any(GetFeaturesInput.class)))
                .thenReturn(Futures.immediateFuture(resultFeatures));

        handshakeManager.shake(helloMessage.build());

        Mockito.verify(handshakeListener).onHandshakeSuccessful(resultFeatures.getResult(), version);
    }

    /**
     * Test of version negotiation Where bitmap version {0x05,0x01}.
     *
     */
    @Test
    public void testVersionNegotiation10InBitmapSwitchStarts() {
        LOG.debug("testVersionNegotiation10InBitmap-ss");
        Short version = OFConstants.OFP_VERSION_1_0;
        handshakeManager.setUseVersionBitmap(true);

        HelloMessageBuilder helloMessage = createHelloMessage(version, helloXid);
        addVersionBitmap(Lists.newArrayList((short) 0x05, OFConstants.OFP_VERSION_1_0), helloMessage);

        Mockito.when(adapter.getFeatures(any(GetFeaturesInput.class)))
                .thenReturn(Futures.immediateFuture(resultFeatures));

        handshakeManager.shake(null);

        handshakeManager.shake(helloMessage.build());

        Mockito.verify(handshakeListener).onHandshakeSuccessful(resultFeatures.getResult(), version);
    }

    /**
     * Test of version negotiation Where bitmap version {0x05,0x04}.
     *
     */
    @Test
    public void testVersionNegotiation13InBitmap() {
        LOG.debug("testVersionNegotiation13InBitmap");
        Short version = OFConstants.OFP_VERSION_1_3;
        handshakeManager.setUseVersionBitmap(true);

        HelloMessageBuilder helloMessage = createHelloMessage(version, helloXid);
        addVersionBitmap(Lists.newArrayList((short) 0x05, OFConstants.OFP_VERSION_1_3), helloMessage);

        Mockito.when(adapter.getFeatures(any(GetFeaturesInput.class)))
                .thenReturn(Futures.immediateFuture(resultFeatures));

        handshakeManager.shake(helloMessage.build());

        Mockito.verify(handshakeListener).onHandshakeSuccessful(resultFeatures.getResult(), version);
    }

    /**
     * Test of version negotiation Where bitmap version {0x05,0x04}.
     *
     */
    @Test
    public void testVersionNegotiation13InBitmapSwitchFirst() {
        LOG.debug("testVersionNegotiation13InBitmap-ss");
        Short version = OFConstants.OFP_VERSION_1_3;
        handshakeManager.setUseVersionBitmap(true);

        HelloMessageBuilder helloMessage = createHelloMessage(version, helloXid);
        addVersionBitmap(Lists.newArrayList((short) 0x05, OFConstants.OFP_VERSION_1_3), helloMessage);

        Mockito.when(adapter.getFeatures(any(GetFeaturesInput.class)))
                .thenReturn(Futures.immediateFuture(resultFeatures));

        handshakeManager.shake(null);

        handshakeManager.shake(helloMessage.build());

        Mockito.verify(handshakeListener).onHandshakeSuccessful(resultFeatures.getResult(), version);
    }

    /**
     * Test of version negotiation Where bitmap version {0x05,0x02}.
     *
     */
    @Test
    public void testVersionNegotiationNoCommonVersionInBitmap() {
        LOG.debug("testVersionNegotiationNoCommonVersionInBitmap");
        Short version = (short) 0x05;
        expectedErrors = 1;
        handshakeManager.setUseVersionBitmap(true);

        HelloMessageBuilder helloMessage = createHelloMessage(version, helloXid);
        addVersionBitmap(Lists.newArrayList((short) 0x05, (short) 0x02), helloMessage);

        handshakeManager.shake(helloMessage.build());

        Mockito.verify(handshakeListener, Mockito.never())
                .onHandshakeSuccessful(any(GetFeaturesOutput.class), anyShort());
    }

    /**
     * Test of version negotiation Where bitmap version {0x05,0x02}.
     *
     */
    @Test
    public void testVersionNegotiationNoCommonVersionInBitmapSwitchStarts() {
        LOG.debug("testVersionNegotiationNoCommonVersionInBitmap-ss");
        Short version = (short) 0x05;
        expectedErrors = 1;
        handshakeManager.setUseVersionBitmap(true);

        HelloMessageBuilder helloMessage = createHelloMessage(version, helloXid);
        addVersionBitmap(Lists.newArrayList((short) 0x05, (short) 0x02), helloMessage);

        handshakeManager.shake(null);

        handshakeManager.shake(helloMessage.build());

        Mockito.verify(handshakeListener, Mockito.never())
                .onHandshakeSuccessful(any(GetFeaturesOutput.class), anyShort());
    }


    /**
     * Creates hello message.
     *
     * @param ofpVersion10 version
     * @param helloXid hello xid
     * @return builder
     */
    private static HelloMessageBuilder createHelloMessage(short ofpVersion10, long helloXid) {
        return new HelloMessageBuilder().setVersion(Uint8.valueOf(ofpVersion10)).setXid(Uint32.valueOf(helloXid));
    }

    /**
     * Adds version bitmap.
     * @param versionOrder version order
     * @param helloBuilder hello builder
     * @return builder
     */
    private static HelloMessageBuilder addVersionBitmap(List<Short> versionOrder, HelloMessageBuilder helloBuilder) {
        short highestVersion = versionOrder.get(0);
        int elementsCount = highestVersion / Integer.SIZE;
        ElementsBuilder elementsBuilder = new ElementsBuilder();

        List<Elements> elementList = new ArrayList<>();
        int orderIndex = versionOrder.size();
        int value = versionOrder.get(--orderIndex);
        for (int index = 0; index <= elementsCount; index++) {
            List<Boolean> booleanList = new ArrayList<>();
            for (int i = 0; i < Integer.SIZE; i++) {
                if (value == index * Integer.SIZE + i) {
                    booleanList.add(true);
                    value = orderIndex == 0 ? highestVersion : versionOrder.get(--orderIndex);
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
