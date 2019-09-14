/*
 * Copyright (c) 2019 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.eric;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.openflowjava.eric.api.EricConstants;
import org.opendaylight.openflowjava.eric.api.EricExtensionCodecRegistrator;
import org.opendaylight.openflowjava.eric.codec.match.Icmpv6NDOptionsTypeCodec;
import org.opendaylight.openflowjava.eric.codec.match.Icmpv6NDReservedCodec;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntryDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntrySerializerKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.EricExpClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.eric.match.rev180730.Icmpv6NdOptionsType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.eric.match.rev180730.Icmpv6NdReserved;

@RunWith(MockitoJUnitRunner.class)
public class EricExtensionsRegistratorTest {

    private EricExtensionsRegistrator ericExtensionsRegistrator;

    @Mock
    EricExtensionCodecRegistrator registrator;

    @Before
    public void setUp() {
        ericExtensionsRegistrator = new EricExtensionsRegistrator(registrator);
    }

    @Test
    public void registerEricExtensionsTest() {
        Mockito.verify(registrator).registerMatchEntrySerializer(Matchers
                .eq(new MatchEntrySerializerKey<>(EncodeConstants.OF13_VERSION_ID, EricExpClass.class,
                        Icmpv6NdReserved.class)), Matchers.any(Icmpv6NDReservedCodec.class));
        Mockito.verify(registrator).registerMatchEntryDeserializer(Matchers
                .eq(new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID,
                        EricConstants.ERICOXM_OF_EXPERIMENTER_ID, EricConstants.ERICOXM_OF_ICMPV6_ND_RESERVED)),
                Matchers.any(Icmpv6NDReservedCodec.class));

        Mockito.verify(registrator).registerMatchEntrySerializer(Matchers
                        .eq(new MatchEntrySerializerKey<>(EncodeConstants.OF13_VERSION_ID, EricExpClass.class,
                                Icmpv6NdOptionsType.class)), Matchers.any(Icmpv6NDOptionsTypeCodec.class));
        Mockito.verify(registrator).registerMatchEntryDeserializer(Matchers
                        .eq(new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID,
                                EricConstants.ERICOXM_OF_EXPERIMENTER_ID,
                                EricConstants.ERICOXM_OF_ICMPV6_ND_OPTIONS_TYPE)),
                Matchers.any(Icmpv6NDOptionsTypeCodec.class));
    }

    @Test
    public void unregisterExtensionsTest() {
        ericExtensionsRegistrator.close();

        Mockito.verify(registrator).unregisterMatchEntrySerializer(
                new MatchEntrySerializerKey<>(EncodeConstants.OF13_VERSION_ID, EricExpClass.class,
                        Icmpv6NdReserved.class));
        Mockito.verify(registrator).unregisterMatchEntryDeserializer(
                new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID, EricConstants.ERICOXM_OF_EXPERIMENTER_ID,
                        EricConstants.ERICOXM_OF_ICMPV6_ND_RESERVED));

        Mockito.verify(registrator).unregisterMatchEntrySerializer(
                new MatchEntrySerializerKey<>(EncodeConstants.OF13_VERSION_ID, EricExpClass.class,
                        Icmpv6NdOptionsType.class));
        Mockito.verify(registrator).unregisterMatchEntryDeserializer(
                new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID, EricConstants.ERICOXM_OF_EXPERIMENTER_ID,
                         EricConstants.ERICOXM_OF_ICMPV6_ND_OPTIONS_TYPE));
    }

}