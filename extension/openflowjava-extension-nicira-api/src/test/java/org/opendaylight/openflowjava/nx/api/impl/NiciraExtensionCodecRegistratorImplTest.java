/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.nx.api.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowjava.nx.api.NiciraActionDeserializerKey;
import org.opendaylight.openflowjava.nx.api.NiciraActionSerializerKey;
import org.opendaylight.openflowjava.nx.api.NiciraUtil;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFGeneralSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.keys.ActionSerializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntryDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntrySerializerKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.spi.connection.SwitchConnectionProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PopVlanCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Nxm0Class;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;

@RunWith(MockitoJUnitRunner.class)
public class NiciraExtensionCodecRegistratorImplTest {


    NiciraExtensionCodecRegistratorImpl niciraExtensionCodecRegistrator;
    List<SwitchConnectionProvider> providers = new LinkedList<>();
    NiciraActionSerializerKey actionSerializerKey;
    NiciraActionDeserializerKey actionDeserializerKey;
    MatchEntrySerializerKey matchSerializerKey;
    MatchEntryDeserializerKey matchDeserializerKey;

    @Mock
    OFSerializer<Action> actionSerializer;
    @Mock
    OFDeserializer<Action> actionDeserializer;
    @Mock
    OFSerializer<MatchEntry> matchSerializer;
    @Mock
    OFDeserializer<MatchEntry> matchDeserializer;


    public static final short VERSION = 4;
    public static final byte VERSION1 = EncodeConstants.OF10_VERSION_ID;
    public static final byte VERSION2 = EncodeConstants.OF13_VERSION_ID;



    @Mock
    SwitchConnectionProvider provider;

    @Before
    public void setUp() {
        providers.add(provider);
        actionSerializerKey = new NiciraActionSerializerKey(VERSION, PopVlanCase.class);
        //subtype = 10
        actionDeserializerKey = new NiciraActionDeserializerKey(VERSION, 10);
        matchSerializerKey = new MatchEntrySerializerKey(VERSION, Nxm0Class.class, MatchField.class);
        //OxmClass 1, OxmField 2
        matchDeserializerKey = new MatchEntryDeserializerKey(VERSION, 1, 2);

        niciraExtensionCodecRegistrator = new NiciraExtensionCodecRegistratorImpl(providers);
    }

    @Test
    public void niciraExtensionsCodecRegistratorImplTest() {
        Mockito.verify(provider).registerActionDeserializer(Matchers.eq(ActionDeserializer.OF10_DESERIALIZER_KEY), Matchers.any(ActionDeserializer.class));
        Mockito.verify(provider).registerActionDeserializer(Matchers.eq(ActionDeserializer.OF13_DESERIALIZER_KEY), Matchers.any(ActionDeserializer.class));
    }

    @Test
    public void registerActionSerializerTest() {
        niciraExtensionCodecRegistrator.registerActionSerializer(actionSerializerKey, actionSerializer);
        ActionSerializerKey key1 = NiciraUtil.createOfJavaKeyFrom(actionSerializerKey);
        Mockito.verify(provider).registerActionSerializer(Matchers.eq(key1), Matchers.any(OFGeneralSerializer.class));
    }

    @Test
    public void unregisterActionSerializerTest() {
        niciraExtensionCodecRegistrator.registerActionSerializer(actionSerializerKey, actionSerializer);
        ActionSerializerKey key1 = NiciraUtil.createOfJavaKeyFrom(actionSerializerKey);
        niciraExtensionCodecRegistrator.unregisterActionSerializer(actionSerializerKey);
        Mockito.verify(provider).unregisterSerializer(Matchers.eq(key1));
    }

    @Test
    public void registerActionDeserializerTest() {
        assertTrue(niciraExtensionCodecRegistrator.isEmptyActionDeserializers());
        niciraExtensionCodecRegistrator.registerActionDeserializer(actionDeserializerKey, actionDeserializer);
        assertFalse(niciraExtensionCodecRegistrator.isEmptyActionDeserializers());
    }

    @Test
    public void unregisterActionDeserializerTest() {
        niciraExtensionCodecRegistrator.registerActionDeserializer(actionDeserializerKey, actionDeserializer);
        assertFalse(niciraExtensionCodecRegistrator.isEmptyActionDeserializers());
        niciraExtensionCodecRegistrator.unregisterActionDeserializer(actionDeserializerKey);
        assertTrue(niciraExtensionCodecRegistrator.isEmptyActionDeserializers());
    }

    @Test
    public void registerMatchEntrySerializerTest() {
        niciraExtensionCodecRegistrator.registerMatchEntrySerializer(matchSerializerKey, matchSerializer);
        Mockito.verify(provider).registerMatchEntrySerializer(matchSerializerKey, matchSerializer);
    }

    @Test
    public void unregisterMatchEntrySerializerTest() {
        niciraExtensionCodecRegistrator.unregisterMatchEntrySerializer(matchSerializerKey);
        Mockito.verify(provider).unregisterSerializer(matchSerializerKey);
    }

    @Test
    public void registerMatchEntryDeserializerTest() {
        niciraExtensionCodecRegistrator.registerMatchEntryDeserializer(matchDeserializerKey, matchDeserializer);
        Mockito.verify(provider).registerMatchEntryDeserializer(matchDeserializerKey, matchDeserializer);
    }

    @Test
    public void unregisterMatchEntryDeserializerTest() {
        niciraExtensionCodecRegistrator.unregisterMatchEntryDeserializer(matchDeserializerKey);
        Mockito.verify(provider).unregisterDeserializer(matchDeserializerKey);
    }


}