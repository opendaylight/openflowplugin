/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.core.connection;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import java.net.InetAddress;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.infrautils.diagstatus.DiagStatusService;
import org.opendaylight.infrautils.diagstatus.ServiceRegistration;
import org.opendaylight.openflowjava.protocol.api.connection.SwitchConnectionHandler;
import org.opendaylight.openflowjava.protocol.api.connection.TlsConfiguration;
import org.opendaylight.openflowjava.protocol.api.connection.TlsConfigurationImpl;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFGeneralDeserializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFGeneralSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.keys.ExperimenterActionDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.ExperimenterActionSerializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.ExperimenterIdDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.ExperimenterIdMeterSubTypeSerializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.ExperimenterIdSerializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.ExperimenterInstructionDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.ExperimenterInstructionSerializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntryDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntrySerializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.MessageCodeKey;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.core.SwitchConnectionProviderImpl;
import org.opendaylight.openflowjava.protocol.spi.connection.SwitchConnectionProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.ExperimenterActionSubType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.config.rev140630.KeystoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.config.rev140630.PathType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.config.rev140630.TransportProtocol;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.InPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.experimenter.core.ExperimenterDataOfChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.MeterBandExperimenterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.queue.property.header.QueueProperty;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.table.features.properties.grouping.TableFeatureProperties;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Unit tests for SwitchConnectionProviderImpl02.
 *
 * @author madamjak
 * @author michal.polkorab
 */
@RunWith(MockitoJUnitRunner.class)
public class SwitchConnectionProviderImpl02Test {
    private static final int CHANNEL_OUTBOUND_QUEUE_SIZE = 1024;
    private static final int SWITCH_IDLE_TIMEOUT = 2000;

    @Mock
    DiagStatusService diagStatusService;
    @Mock
    ServiceRegistration diagReg;
    @Mock
    SwitchConnectionHandler handler;
    @Mock
    OFGeneralSerializer serializer;
    @Mock
    OFGeneralDeserializer deserializer;
    @Mock
    OFDeserializer<ErrorMessage> deserializerError;
    @Mock
    OFDeserializer<ExperimenterDataOfChoice> deserializerExpMsg;
    @Mock
    OFDeserializer<ExperimenterDataOfChoice> deserializerMultipartRplMsg;
    @Mock
    OFDeserializer<QueueProperty> deserializerQueueProperty;
    @Mock
    OFDeserializer<MeterBandExperimenterCase> deserializerMeterBandExpCase;
    @Mock
    OFSerializer<ExperimenterDataOfChoice> serializerExperimenterInput;
    @Mock
    OFSerializer<ExperimenterDataOfChoice> serializerMultipartRequestExpCase;
    @Mock
    OFSerializer<MeterBandExperimenterCase> serializerMeterBandExpCase;
    @Mock
    ConnectionConfigurationImpl config;

    private TlsConfiguration tlsConfiguration;
    private SwitchConnectionProviderImpl provider;

    /**
     * Creates new {@link SwitchConnectionProvider} instance for each test.
     *
     * @param protocol communication protocol
     */
    public void startUp(final TransportProtocol protocol) throws Exception {
        config = null;
        if (protocol != null) {
            createConfig(protocol);
        }
        doReturn(diagReg).when(diagStatusService).register(any());
        provider = new SwitchConnectionProviderImpl(diagStatusService, config);
    }

    private void createConfig(final TransportProtocol protocol) throws Exception {
        InetAddress startupAddress = InetAddress.getLocalHost();

        tlsConfiguration = null;
        if (protocol.equals(TransportProtocol.TLS)) {
            tlsConfiguration = new TlsConfigurationImpl(KeystoreType.JKS,
                    "/selfSignedSwitch", PathType.CLASSPATH, KeystoreType.JKS,
                    "/selfSignedController", PathType.CLASSPATH,
                    List.of("TLS_RSA_WITH_AES_128_CBC_SHA", "TLS_RSA_WITH_AES_128_CBC_SHA256"));
        }
        config = new ConnectionConfigurationImpl(startupAddress, 0, tlsConfiguration, SWITCH_IDLE_TIMEOUT, true, false,
                CHANNEL_OUTBOUND_QUEUE_SIZE);
        config.setTransferProtocol(protocol);
    }

    /**
     * Test getServerFacade.
     */
    @Test
    public void testServerFacade() throws Exception {
        startUp(TransportProtocol.TCP);
        final var future = provider.startup(handler);
        final var serverFacade = provider.getServerFacade();
        assertNotNull("Wrong -- getServerFacade return null", serverFacade);
    }

    /**
     * Test shutdown on unconfigured provider.
     */
    @Test
    public void testShutdownUnconfigured() throws Exception {
        startUp(TransportProtocol.TCP);
        assertThrows(IllegalStateException.class, provider::shutdown);
    }

    /**
     * Test unregister by wrong key.
     */
    @Test
    public void testUnregisterWrongKeys() throws Exception {
        startUp(TransportProtocol.TCP);
        final var testSerKey = new ExperimenterInstructionSerializerKey(EncodeConstants.OF_VERSION_1_0, 42L);
        assertFalse("Wrong -- unregisterSerializer",provider.unregisterSerializer(testSerKey));
        final var tesDeserKey = new ExperimenterInstructionDeserializerKey(EncodeConstants.OF_VERSION_1_0, 24L);
        assertFalse("Wrong -- unregisterDeserializer",provider.unregisterDeserializer(tesDeserKey));
    }

    /**
     * Test register and unregister method.
     */
    @Test
    public void testUnregisterExistingKeys() throws Exception {
        startUp(TransportProtocol.TCP);
        // -- registerActionSerializer
        final var key1 = new ExperimenterActionSerializerKey(EncodeConstants.OF_VERSION_1_0, Uint32.valueOf(42),
            TestSubType.VALUE);
        provider.registerActionSerializer(key1, serializer);
        assertTrue("Wrong -- unregister ActionSerializer", provider.unregisterSerializer(key1));
        assertFalse("Wrong -- unregister ActionSerializer by not existing key",
                provider.unregisterSerializer(key1));
        // -- registerActionDeserializer
        final var key2 = new ExperimenterActionDeserializerKey(EncodeConstants.OF_VERSION_1_0, 42L);
        provider.registerActionDeserializer(key2, deserializer);
        assertTrue("Wrong -- unregister ActionDeserializer", provider.unregisterDeserializer(key2));
        assertFalse("Wrong -- unregister ActionDeserializer by not existing key",
                provider.unregisterDeserializer(key2));
        // -- registerInstructionSerializer
        final ExperimenterInstructionSerializerKey key3 =
            new ExperimenterInstructionSerializerKey(EncodeConstants.OF_VERSION_1_0, 42L);
        provider.registerInstructionSerializer(key3, serializer);
        assertTrue("Wrong -- unregister InstructionSerializer", provider.unregisterSerializer(key3));
        assertFalse("Wrong -- unregister InstructionSerializer by not existing key",
                provider.unregisterSerializer(key3));
        // -- registerInstructionDeserializer
        final var key4 = new ExperimenterInstructionDeserializerKey(EncodeConstants.OF_VERSION_1_0, 42L);
        provider.registerInstructionDeserializer(key4, deserializer);
        assertTrue("Wrong -- unregister InstructionDeserializer", provider.unregisterDeserializer(key4));
        assertFalse("Wrong -- unregister InstructionDeserializer by not existing key",
                provider.unregisterDeserializer(key4));
        // -- registerMatchEntryDeserializer
        final var key5 = new MatchEntryDeserializerKey(EncodeConstants.OF_VERSION_1_0, 0x8000, 42);
        provider.registerMatchEntryDeserializer(key5, deserializer);
        assertTrue("Wrong -- unregister MatchEntryDeserializer", provider.unregisterDeserializer(key5));
        assertFalse("Wrong -- unregister MatchEntryDeserializer by not existing key",
                provider.unregisterDeserializer(key5));
        // -- registerErrorDeserializer
        final var key6 = new ExperimenterIdDeserializerKey(EncodeConstants.OF_VERSION_1_0, Uint32.valueOf(42),
            ErrorMessage.class);
        provider.registerErrorDeserializer(key6, deserializerError);
        assertTrue("Wrong -- unregister ErrorDeserializer", provider.unregisterDeserializer(key6));
        assertFalse("Wrong -- unregister ErrorDeserializer by not existing key",
                provider.unregisterDeserializer(key6));
        // -- registerExperimenterMessageDeserializer
        final var key7 = new ExperimenterIdDeserializerKey(EncodeConstants.OF_VERSION_1_0, Uint32.valueOf(42),
            ExperimenterMessage.class);
        provider.registerExperimenterMessageDeserializer(key7, deserializerExpMsg);
        assertTrue("Wrong -- unregister ExperimenterMessageDeserializer", provider.unregisterDeserializer(key7));
        assertFalse("Wrong -- unregister ExperimenterMessageDeserializer by not existing key",
                provider.unregisterDeserializer(key7));
        // -- registerMultipartReplyMessageDeserializer
        final var key8 = new ExperimenterIdDeserializerKey(EncodeConstants.OF_VERSION_1_0,
                Uint32.valueOf(42), MultipartReplyMessage.class);
        provider.registerMultipartReplyMessageDeserializer(key8, deserializerMultipartRplMsg);
        assertTrue("Wrong -- unregister MultipartReplyMessageDeserializer",
                provider.unregisterDeserializer(key8));
        assertFalse("Wrong -- unregister MultipartReplyMessageDeserializer by not existing key",
                provider.unregisterDeserializer(key8));
        // -- registerMultipartReplyTFDeserializer
        final var key9 = new ExperimenterIdDeserializerKey(EncodeConstants.OF_VERSION_1_0, Uint32.valueOf(42),
            MultipartReplyMessage.class);
        provider.registerMultipartReplyTFDeserializer(key9, deserializer);
        assertTrue("Wrong -- unregister MultipartReplyTFDeserializer", provider.unregisterDeserializer(key9));
        assertFalse("Wrong -- unregister MultipartReplyTFDeserializer by non existing key",
                provider.unregisterDeserializer(key9));
        // -- registerQueuePropertyDeserializer
        final var key10 = new ExperimenterIdDeserializerKey(EncodeConstants.OF_VERSION_1_0, Uint32.valueOf(42),
            QueueProperty.class);
        provider.registerQueuePropertyDeserializer(key10, deserializerQueueProperty);
        assertTrue("Wrong -- unregister QueuePropertyDeserializer", provider.unregisterDeserializer(key10));
        assertFalse("Wrong -- unregister QueuePropertyDeserializer by not existing key",
                provider.unregisterDeserializer(key10));
        // -- registerMeterBandDeserializer
        final var key11 = new ExperimenterIdDeserializerKey(EncodeConstants.OF_VERSION_1_0, Uint32.valueOf(42),
            MeterBandExperimenterCase.class);
        provider.registerMeterBandDeserializer(key11, deserializerMeterBandExpCase);
        assertTrue("Wrong -- unregister MeterBandDeserializer", provider.unregisterDeserializer(key11));
        assertFalse("Wrong -- unregister MeterBandDeserializer by not existing key",
                provider.unregisterDeserializer(key11));
        // -- registerExperimenterMessageSerializer
        final var key12 = new ExperimenterIdSerializerKey<>(EncodeConstants.OF_VERSION_1_0, Uint32.valueOf(42),
            ExperimenterDataOfChoice.class);
        provider.registerExperimenterMessageSerializer(key12, serializerExperimenterInput);
        assertTrue("Wrong -- unregister ExperimenterMessageSerializer", provider.unregisterSerializer(key12));
        assertFalse("Wrong -- unregister ExperimenterMessageSerializer by not existing key",
                provider.unregisterSerializer(key12));
        //registerMultipartRequestSerializer
        final var key13 = new ExperimenterIdSerializerKey<>(EncodeConstants.OF_VERSION_1_0, Uint32.valueOf(42),
            ExperimenterDataOfChoice.class);
        provider.registerMultipartRequestSerializer(key13, serializerMultipartRequestExpCase);
        assertTrue("Wrong -- unregister MultipartRequestSerializer", provider.unregisterSerializer(key13));
        assertFalse("Wrong -- unregister MultipartRequestSerializer by not existing key",
                provider.unregisterSerializer(key13));
        // -- registerMultipartRequestTFSerializer
        final var key14 = new ExperimenterIdSerializerKey<>(EncodeConstants.OF_VERSION_1_0, Uint32.valueOf(42),
            TableFeatureProperties.class);
        provider.registerMultipartRequestTFSerializer(key14, serializer);
        assertTrue("Wrong -- unregister MultipartRequestTFSerializer", provider.unregisterSerializer(key14));
        assertFalse("Wrong -- unregister MultipartRequestTFSerializer by not existing key",
                provider.unregisterSerializer(key14));
        // -- registerMeterBandSerializer
        final var key15 = new ExperimenterIdMeterSubTypeSerializerKey<>(EncodeConstants.OF_VERSION_1_0,
            Uint32.valueOf(42), MeterBandExperimenterCase.class,null);
        provider.registerMeterBandSerializer(key15, serializerMeterBandExpCase);
        assertTrue("Wrong -- unregister MeterBandSerializer", provider.unregisterSerializer(key15));
        assertFalse("Wrong -- unregister MeterBandSerializer by not existing key",
                provider.unregisterSerializer(key15));
        // -- registerMatchEntrySerializer
        final var key16 = new MatchEntrySerializerKey<>(EncodeConstants.OF_VERSION_1_3, OpenflowBasicClass.VALUE,
            InPort.VALUE);
        provider.registerMatchEntrySerializer(key16, serializer);
        assertTrue("Wrong -- unregister MatchEntrySerializer", provider.unregisterSerializer(key16));
        assertFalse("Wrong -- unregister MatchEntrySerializer by not existing key",
                provider.unregisterSerializer(key15));
        // -- registerSerializer
        final var key17 = new MessageTypeKey<>(EncodeConstants.OF_VERSION_1_3, TestSubType.class);
        provider.registerSerializer(key17, serializer);
        // -- registerDeserializer
        final var key18 = new MessageCodeKey(EncodeConstants.OF_VERSION_1_3, 42, TestSubType.class);
        provider.registerDeserializer(key18, deserializer);
    }

    private interface TestSubType extends ExperimenterActionSubType {
        TestSubType VALUE = () -> TestSubType.class;
    }
}
