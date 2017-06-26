/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.util;

import io.netty.buffer.ByteBuf;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.keys.MessageCodeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.deserialization.DeserializerRegistryImpl;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowWildcardsV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.v10.grouping.MatchV10;

/**
 * @author michal.polkorab
 *
 */
public class OF10MatchDeserializerTest {

    private OFDeserializer<MatchV10> matchDeserializer;

    /**
     * Initializes deserializer registry and lookups correct deserializer
     */
    @Before
    public void startUp() {
        DeserializerRegistry registry = new DeserializerRegistryImpl();
        registry.init();
        matchDeserializer = registry.getDeserializer(
                new MessageCodeKey(EncodeConstants.OF10_VERSION_ID, EncodeConstants.EMPTY_VALUE, MatchV10.class));
    }

    /**
     * Testing correct deserialization of ofp_match
     */
    @Test
    public void test() {
        ByteBuf message = BufferHelper.buildBuffer("00 24 08 91 00 20 AA BB CC DD EE FF "
                + "AA BB CC DD EE FF 00 05 10 00 00 08 07 06 00 00 10 11 12 13 01 02 03 04 "
                + "50 50 20 20");
        message.skipBytes(4); // skip XID
        MatchV10 match = matchDeserializer.deserialize(message);
        Assert.assertEquals("Wrong wildcards", new FlowWildcardsV10(false, false, true, false,
                false, true, false, true, true, false), match.getWildcards());
        Assert.assertEquals("Wrong srcMask", 24, match.getNwSrcMask().shortValue());
        Assert.assertEquals("Wrong dstMask", 16, match.getNwDstMask().shortValue());
        Assert.assertEquals("Wrong in-port", 32, match.getInPort().intValue());
        Assert.assertEquals("Wrong dl-src", new MacAddress("aa:bb:cc:dd:ee:ff"), match.getDlSrc());
        Assert.assertEquals("Wrong dl-dst", new MacAddress("aa:bb:cc:dd:ee:ff"), match.getDlDst());
        Assert.assertEquals("Wrong dl-vlan", 5, match.getDlVlan().intValue());
        Assert.assertEquals("Wrong dl-vlan-pcp", 16, match.getDlVlanPcp().shortValue());
        Assert.assertEquals("Wrong dl-type", 8, match.getDlType().intValue());
        Assert.assertEquals("Wrong nw-tos", 7, match.getNwTos().shortValue());
        Assert.assertEquals("Wrong nw-proto", 6, match.getNwProto().shortValue());
        Assert.assertEquals("Wrong nw-src", new Ipv4Address("16.17.18.19"), match.getNwSrc());
        Assert.assertEquals("Wrong nw-dst", new Ipv4Address("1.2.3.4"), match.getNwDst());
        Assert.assertEquals("Wrong tp-src", 20560, match.getTpSrc().shortValue());
        Assert.assertEquals("Wrong tp-dst", 8224, match.getTpDst().shortValue());
    }

    /**
     * Testing correct deserialization of ofp_match
     */
    @Test
    public void test2() {
        ByteBuf message = BufferHelper.buildBuffer("00 3F FF FF 00 20 AA BB CC DD EE FF "
                + "AA BB CC DD EE FF 00 05 10 00 00 08 07 06 00 00 10 11 12 13 01 02 03 04 "
                + "50 50 20 20");
        message.skipBytes(4); // skip XID
        MatchV10 match = matchDeserializer.deserialize(message);
        Assert.assertEquals("Wrong wildcards", new FlowWildcardsV10(true, true, true, true,
                true, true, true, true, true, true), match.getWildcards());
        Assert.assertEquals("Wrong srcMask", 0, match.getNwSrcMask().shortValue());
        Assert.assertEquals("Wrong dstMask", 0, match.getNwDstMask().shortValue());
        Assert.assertEquals("Wrong in-port", 32, match.getInPort().intValue());
        Assert.assertEquals("Wrong dl-src", new MacAddress("aa:bb:cc:dd:ee:ff"), match.getDlSrc());
        Assert.assertEquals("Wrong dl-dst", new MacAddress("aa:bb:cc:dd:ee:ff"), match.getDlDst());
        Assert.assertEquals("Wrong dl-vlan", 5, match.getDlVlan().intValue());
        Assert.assertEquals("Wrong dl-vlan-pcp", 16, match.getDlVlanPcp().shortValue());
        Assert.assertEquals("Wrong dl-type", 8, match.getDlType().intValue());
        Assert.assertEquals("Wrong nw-tos", 7, match.getNwTos().shortValue());
        Assert.assertEquals("Wrong nw-proto", 6, match.getNwProto().shortValue());
        Assert.assertEquals("Wrong nw-src", new Ipv4Address("16.17.18.19"), match.getNwSrc());
        Assert.assertEquals("Wrong nw-dst", new Ipv4Address("1.2.3.4"), match.getNwDst());
        Assert.assertEquals("Wrong tp-src", 20560, match.getTpSrc().shortValue());
        Assert.assertEquals("Wrong tp-dst", 8224, match.getTpDst().shortValue());
    }

}
