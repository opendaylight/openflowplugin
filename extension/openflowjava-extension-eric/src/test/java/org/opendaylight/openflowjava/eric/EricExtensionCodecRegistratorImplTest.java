/*
 * Copyright (c) 2019 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.eric;

import java.util.LinkedList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntryDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntrySerializerKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.spi.connection.SwitchConnectionProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.EricExpClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;

@RunWith(MockitoJUnitRunner.class)
public class EricExtensionCodecRegistratorImplTest {

    public static final short VERSION = 4;
    public static final byte VERSION1 = EncodeConstants.OF10_VERSION_ID;
    public static final byte VERSION2 = EncodeConstants.OF13_VERSION_ID;

    private EricExtensionCodecRegistratorImpl ericExtensionCodecRegistrator;
    private final List<SwitchConnectionProvider> providers = new LinkedList<>();
    private MatchEntrySerializerKey<EricExpClass, MatchField> matchSerializerKey;
    private MatchEntryDeserializerKey matchDeserializerKey;

    @Mock
    OFSerializer<MatchEntry> matchSerializer;
    @Mock
    OFDeserializer<MatchEntry> matchDeserializer;
    @Mock
    SwitchConnectionProvider provider;

    @Before
    public void setUp() {
        providers.add(provider);
        matchSerializerKey = new MatchEntrySerializerKey<>(VERSION, EricExpClass.class, MatchField.class);
        //OxmClass 1, OxmField 2
        matchDeserializerKey = new MatchEntryDeserializerKey(VERSION, 1, 2);

        ericExtensionCodecRegistrator = new EricExtensionCodecRegistratorImpl(providers);
    }

    @Test
    public void registerMatchEntrySerializerTest() {
        ericExtensionCodecRegistrator.registerMatchEntrySerializer(matchSerializerKey, matchSerializer);
        Mockito.verify(provider).registerMatchEntrySerializer(matchSerializerKey, matchSerializer);
    }

    @Test
    public void unregisterMatchEntrySerializerTest() {
        ericExtensionCodecRegistrator.unregisterMatchEntrySerializer(matchSerializerKey);
        Mockito.verify(provider).unregisterSerializer(matchSerializerKey);
    }

    @Test
    public void registerMatchEntryDeserializerTest() {
        ericExtensionCodecRegistrator.registerMatchEntryDeserializer(matchDeserializerKey, matchDeserializer);
        Mockito.verify(provider).registerMatchEntryDeserializer(matchDeserializerKey, matchDeserializer);
    }

    @Test
    public void unregisterMatchEntryDeserializerTest() {
        ericExtensionCodecRegistrator.unregisterMatchEntryDeserializer(matchDeserializerKey);
        Mockito.verify(provider).unregisterDeserializer(matchDeserializerKey);
    }

}