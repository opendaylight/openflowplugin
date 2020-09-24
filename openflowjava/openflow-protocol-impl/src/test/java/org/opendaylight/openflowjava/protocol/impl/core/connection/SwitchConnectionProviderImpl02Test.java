/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.core.connection;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListenableFuture;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendaylight.openflowjava.protocol.api.connection.OpenflowDiagStatusProvider;
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
import org.opendaylight.openflowjava.protocol.impl.core.ServerFacade;
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
public class SwitchConnectionProviderImpl02Test {
    @Mock SwitchConnectionHandler handler;
    @Mock OFGeneralSerializer serializer;
    @Mock OFGeneralDeserializer deserializer;
    @Mock OFDeserializer<ErrorMessage> deserializerError;
    @Mock OFDeserializer<ExperimenterDataOfChoice> deserializerExpMsg;
    @Mock OFDeserializer<ExperimenterDataOfChoice> deserializerMultipartRplMsg;
    @Mock OFDeserializer<QueueProperty> deserializerQueueProperty;
    @Mock OFDeserializer<MeterBandExperimenterCase> deserializerMeterBandExpCase;
    @Mock OFSerializer<ExperimenterDataOfChoice> serializerExperimenterInput;
    @Mock OFSerializer<ExperimenterDataOfChoice> serializerMultipartRequestExpCase;
    @Mock OFSerializer<MeterBandExperimenterCase> serializerMeterBandExpCase;
    @Mock ConnectionConfigurationImpl config;
    @Mock OpenflowDiagStatusProvider openflowPluginDiagStatusProvider;
    private static final int CHANNEL_OUTBOUND_QUEUE_SIZE = 1024;
    private static final int SWITCH_IDLE_TIMEOUT = 2000;
    private TlsConfiguration tlsConfiguration;
    private SwitchConnectionProviderImpl provider;

    /**
     * Creates new {@link SwitchConnectionProvider} instance for each test.
     *
     * @param protocol communication protocol
     */
    public void startUp(final TransportProtocol protocol) throws UnknownHostException {
        MockitoAnnotations.initMocks(this);
        config = null;
        if (protocol != null) {
            createConfig(protocol);
        }
        provider = new SwitchConnectionProviderImpl(config,openflowPluginDiagStatusProvider);
    }

    private void createConfig(final TransportProtocol protocol) throws UnknownHostException {
        InetAddress startupAddress = InetAddress.getLocalHost();

        tlsConfiguration = null;
        if (protocol.equals(TransportProtocol.TLS)) {
            tlsConfiguration = new TlsConfigurationImpl(KeystoreType.JKS,
                    "/selfSignedSwitch", PathType.CLASSPATH, KeystoreType.JKS,
                    "/selfSignedController", PathType.CLASSPATH,
                    Lists.newArrayList("TLS_RSA_WITH_AES_128_CBC_SHA", "TLS_RSA_WITH_AES_128_CBC_SHA256")) ;
        }
        config = new ConnectionConfigurationImpl(startupAddress, 0, tlsConfiguration, SWITCH_IDLE_TIMEOUT, true, false,
                CHANNEL_OUTBOUND_QUEUE_SIZE);
        config.setTransferProtocol(protocol);
    }


    /**
     * Test getServerFacade.
     */
    @Test
    public void testServerFacade() throws UnknownHostException {
        startUp(TransportProtocol.TCP);
        final ListenableFuture<Boolean> future = provider.startup();
        final ServerFacade serverFacade = provider.getServerFacade();
        Assert.assertNotNull("Wrong -- getServerFacade return null",serverFacade);
    }

    /**
     * Test shutdown on unconfigured provider.
     */
    @Test(expected = IllegalStateException.class)
    public void testShutdownUnconfigured() throws UnknownHostException {
        startUp(TransportProtocol.TCP);
        provider.shutdown();
    }

    /**
     * Test unregister by wrong key.
     */
    @Test
    public void testUnregisterWrongKeys() throws UnknownHostException {
        startUp(TransportProtocol.TCP);
        final ExperimenterInstructionSerializerKey testSerKey
            = new ExperimenterInstructionSerializerKey(EncodeConstants.OF10_VERSION_ID,42L);
        Assert.assertFalse("Wrong -- unregisterSerializer",provider.unregisterSerializer(testSerKey));
        final ExperimenterInstructionDeserializerKey tesDeserKey
            = new ExperimenterInstructionDeserializerKey(EncodeConstants.OF10_VERSION_ID,24L);
        Assert.assertFalse("Wrong -- unregisterDeserializer",provider.unregisterDeserializer(tesDeserKey));
    }

    /**
     * Test register and unregister method.
     */
    @Test
    public void testUnregisterExistingKeys() throws UnknownHostException {
        startUp(TransportProtocol.TCP);
        // -- registerActionSerializer
        final ExperimenterActionSerializerKey key1
            = new ExperimenterActionSerializerKey(EncodeConstants.OF10_VERSION_ID, Uint32.valueOf(42),
                TestSubType.class);
        provider.registerActionSerializer(key1, serializer);
        Assert.assertTrue("Wrong -- unregister ActionSerializer", provider.unregisterSerializer(key1));
        Assert.assertFalse("Wrong -- unregister ActionSerializer by not existing key",
                provider.unregisterSerializer(key1));
        // -- registerActionDeserializer
        final ExperimenterActionDeserializerKey key2
            = new ExperimenterActionDeserializerKey(EncodeConstants.OF10_VERSION_ID, 42L);
        provider.registerActionDeserializer(key2, deserializer);
        Assert.assertTrue("Wrong -- unregister ActionDeserializer", provider.unregisterDeserializer(key2));
        Assert.assertFalse("Wrong -- unregister ActionDeserializer by not existing key",
                provider.unregisterDeserializer(key2));
        // -- registerInstructionSerializer
        final ExperimenterInstructionSerializerKey key3
            = new ExperimenterInstructionSerializerKey(EncodeConstants.OF10_VERSION_ID,42L);
        provider.registerInstructionSerializer(key3, serializer);
        Assert.assertTrue("Wrong -- unregister InstructionSerializer", provider.unregisterSerializer(key3));
        Assert.assertFalse("Wrong -- unregister InstructionSerializer by not existing key",
                provider.unregisterSerializer(key3));
        // -- registerInstructionDeserializer
        final ExperimenterInstructionDeserializerKey key4
            = new ExperimenterInstructionDeserializerKey(EncodeConstants.OF10_VERSION_ID,42L);
        provider.registerInstructionDeserializer(key4, deserializer);
        Assert.assertTrue("Wrong -- unregister InstructionDeserializer", provider.unregisterDeserializer(key4));
        Assert.assertFalse("Wrong -- unregister InstructionDeserializer by not existing key",
                provider.unregisterDeserializer(key4));
        // -- registerMatchEntryDeserializer
        final MatchEntryDeserializerKey key5
            = new MatchEntryDeserializerKey(EncodeConstants.OF10_VERSION_ID, 0x8000, 42);
        provider.registerMatchEntryDeserializer(key5, deserializer);
        Assert.assertTrue("Wrong -- unregister MatchEntryDeserializer", provider.unregisterDeserializer(key5));
        Assert.assertFalse("Wrong -- unregister MatchEntryDeserializer by not existing key",
                provider.unregisterDeserializer(key5));
        // -- registerErrorDeserializer
        final ExperimenterIdDeserializerKey key6
            = new ExperimenterIdDeserializerKey(EncodeConstants.OF10_VERSION_ID, 42L, ErrorMessage.class);
        provider.registerErrorDeserializer(key6, deserializerError);
        Assert.assertTrue("Wrong -- unregister ErrorDeserializer", provider.unregisterDeserializer(key6));
        Assert.assertFalse("Wrong -- unregister ErrorDeserializer by not existing key",
                provider.unregisterDeserializer(key6));
        // -- registerExperimenterMessageDeserializer
        final ExperimenterIdDeserializerKey key7
            = new ExperimenterIdDeserializerKey(EncodeConstants.OF10_VERSION_ID, 42L, ExperimenterMessage.class);
        provider.registerExperimenterMessageDeserializer(key7, deserializerExpMsg);
        Assert.assertTrue("Wrong -- unregister ExperimenterMessageDeserializer", provider.unregisterDeserializer(key7));
        Assert.assertFalse("Wrong -- unregister ExperimenterMessageDeserializer by not existing key",
                provider.unregisterDeserializer(key7));
        // -- registerMultipartReplyMessageDeserializer
        final ExperimenterIdDeserializerKey key8
            = new ExperimenterIdDeserializerKey(EncodeConstants.OF10_VERSION_ID, 42L, MultipartReplyMessage.class);
        provider.registerMultipartReplyMessageDeserializer(key8, deserializerMultipartRplMsg);
        Assert.assertTrue("Wrong -- unregister MultipartReplyMessageDeserializer",
                provider.unregisterDeserializer(key8));
        Assert.assertFalse("Wrong -- unregister MultipartReplyMessageDeserializer by not existing key",
                provider.unregisterDeserializer(key8));
        // -- registerMultipartReplyTFDeserializer
        final ExperimenterIdDeserializerKey key9 =
                new ExperimenterIdDeserializerKey(EncodeConstants.OF10_VERSION_ID, 42L, MultipartReplyMessage.class);
        provider.registerMultipartReplyTFDeserializer(key9, deserializer);
        Assert.assertTrue("Wrong -- unregister MultipartReplyTFDeserializer", provider.unregisterDeserializer(key9));
        Assert.assertFalse("Wrong -- unregister MultipartReplyTFDeserializer by non existing key",
                provider.unregisterDeserializer(key9));
        // -- registerQueuePropertyDeserializer
        final ExperimenterIdDeserializerKey key10
            = new ExperimenterIdDeserializerKey(EncodeConstants.OF10_VERSION_ID, 42L, QueueProperty.class);
        provider.registerQueuePropertyDeserializer(key10, deserializerQueueProperty);
        Assert.assertTrue("Wrong -- unregister QueuePropertyDeserializer", provider.unregisterDeserializer(key10));
        Assert.assertFalse("Wrong -- unregister QueuePropertyDeserializer by not existing key",
                provider.unregisterDeserializer(key10));
        // -- registerMeterBandDeserializer
        final ExperimenterIdDeserializerKey key11
            = new ExperimenterIdDeserializerKey(EncodeConstants.OF10_VERSION_ID, 42L, MeterBandExperimenterCase.class);
        provider.registerMeterBandDeserializer(key11, deserializerMeterBandExpCase);
        Assert.assertTrue("Wrong -- unregister MeterBandDeserializer", provider.unregisterDeserializer(key11));
        Assert.assertFalse("Wrong -- unregister MeterBandDeserializer by not existing key",
                provider.unregisterDeserializer(key11));
        // -- registerExperimenterMessageSerializer
        ExperimenterIdSerializerKey<ExperimenterDataOfChoice> key12
                = new ExperimenterIdSerializerKey<>(EncodeConstants.OF10_VERSION_ID, 42L,
                        ExperimenterDataOfChoice.class);
        provider.registerExperimenterMessageSerializer(key12, serializerExperimenterInput);
        Assert.assertTrue("Wrong -- unregister ExperimenterMessageSerializer", provider.unregisterSerializer(key12));
        Assert.assertFalse("Wrong -- unregister ExperimenterMessageSerializer by not existing key",
                provider.unregisterSerializer(key12));
        //registerMultipartRequestSerializer
        ExperimenterIdSerializerKey<ExperimenterDataOfChoice> key13
                = new ExperimenterIdSerializerKey<>(EncodeConstants.OF10_VERSION_ID, 42L,
                        ExperimenterDataOfChoice.class);
        provider.registerMultipartRequestSerializer(key13, serializerMultipartRequestExpCase);
        Assert.assertTrue("Wrong -- unregister MultipartRequestSerializer", provider.unregisterSerializer(key13));
        Assert.assertFalse("Wrong -- unregister MultipartRequestSerializer by not existing key",
                provider.unregisterSerializer(key13));
        // -- registerMultipartRequestTFSerializer
        final ExperimenterIdSerializerKey<TableFeatureProperties> key14
            = new ExperimenterIdSerializerKey<>(EncodeConstants.OF10_VERSION_ID,42L,TableFeatureProperties.class);
        provider.registerMultipartRequestTFSerializer(key14, serializer);
        Assert.assertTrue("Wrong -- unregister MultipartRequestTFSerializer", provider.unregisterSerializer(key14));
        Assert.assertFalse("Wrong -- unregister MultipartRequestTFSerializer by not existing key",
                provider.unregisterSerializer(key14));
        // -- registerMeterBandSerializer
        final ExperimenterIdMeterSubTypeSerializerKey<MeterBandExperimenterCase> key15
            = new ExperimenterIdMeterSubTypeSerializerKey<>(EncodeConstants.OF10_VERSION_ID,42L,
                    MeterBandExperimenterCase.class,null);
        provider.registerMeterBandSerializer(key15, serializerMeterBandExpCase);
        Assert.assertTrue("Wrong -- unregister MeterBandSerializer", provider.unregisterSerializer(key15));
        Assert.assertFalse("Wrong -- unregister MeterBandSerializer by not existing key",
                provider.unregisterSerializer(key15));
        // -- registerMatchEntrySerializer
        final MatchEntrySerializerKey<OpenflowBasicClass, InPort> key16
            = new MatchEntrySerializerKey<>(EncodeConstants.OF13_VERSION_ID, OpenflowBasicClass.class, InPort.class);
        provider.registerMatchEntrySerializer(key16, serializer);
        Assert.assertTrue("Wrong -- unregister MatchEntrySerializer", provider.unregisterSerializer(key16));
        Assert.assertFalse("Wrong -- unregister MatchEntrySerializer by not existing key",
                provider.unregisterSerializer(key15));
        // -- registerSerializer
        final MessageTypeKey key17 = new MessageTypeKey<>(EncodeConstants.OF13_VERSION_ID, TestSubType.class);
        provider.registerSerializer(key17, serializer);
        // -- registerDeserializer
        final MessageCodeKey key18 = new MessageCodeKey(EncodeConstants.OF13_VERSION_ID, 42, TestSubType.class);
        provider.registerDeserializer(key18, deserializer);
    }

    private interface TestSubType extends ExperimenterActionSubType {
        // empty class - only used in test for comparation
    }
}
