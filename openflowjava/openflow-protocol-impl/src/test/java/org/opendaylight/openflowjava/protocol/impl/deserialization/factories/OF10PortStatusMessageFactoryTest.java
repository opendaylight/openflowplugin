/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.deserialization.factories;

import io.netty.buffer.ByteBuf;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.keys.MessageCodeKey;
import org.opendaylight.openflowjava.protocol.impl.deserialization.DeserializerRegistryImpl;
import org.opendaylight.openflowjava.protocol.impl.util.BufferHelper;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortConfigV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortFeaturesV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortReason;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortStateV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortStatusMessage;

/**
 * @author michal.polkorab
 *
 */
public class OF10PortStatusMessageFactoryTest {

    private OFDeserializer<PortStatusMessage> statusFactory;

    /**
     * Initializes deserializer registry and lookups correct deserializer
     */
    @Before
    public void startUp() {
        DeserializerRegistry registry = new DeserializerRegistryImpl();
        registry.init();
        statusFactory = registry.getDeserializer(
                new MessageCodeKey(EncodeConstants.OF10_VERSION_ID, 12, PortStatusMessage.class));
    }

    /**
     * Testing {@link OF10PortStatusMessageFactory} for correct translation into POJO
     */
    @Test
    public void test(){
        ByteBuf bb = BufferHelper.buildBuffer("00 00 00 00 00 00 00 00 "
                + "00 10 01 01 05 01 04 02 41 4C 4F 48 41 00 00 00 00 00 00 00 00 00 00 "
                + "00 00 00 00 15 00 00 00 01 00 00 00 31 00 00 04 42 00 00 03 0C 00 00 08 88");
        PortStatusMessage builtByFactory = BufferHelper.deserialize(statusFactory, bb);

        BufferHelper.checkHeaderV10(builtByFactory);
        Assert.assertEquals("Wrong reason", PortReason.OFPPRADD, builtByFactory.getReason());
        Assert.assertEquals("Wrong port - port-no", 16, builtByFactory.getPortNo().intValue());
        Assert.assertEquals("Wrong builtByFactory - hw-addr", new MacAddress("01:01:05:01:04:02"), builtByFactory.getHwAddr());
        Assert.assertEquals("Wrong builtByFactory - name", new String("ALOHA"), builtByFactory.getName());
        Assert.assertEquals("Wrong builtByFactory - config", new PortConfigV10(true, false, false, true, false, false, true),
                builtByFactory.getConfigV10());
        Assert.assertEquals("Wrong builtByFactory - state", new PortStateV10(false, true, false, false, false, false, true, false),
                builtByFactory.getStateV10());
        Assert.assertEquals("Wrong builtByFactory - curr", new PortFeaturesV10(false, false, false, false, true, true, true,
                false, false, false, false, false), builtByFactory.getCurrentFeaturesV10());
        Assert.assertEquals("Wrong builtByFactory - advertised", new PortFeaturesV10(false, false, true, true, false, false,
                false, false, false, false, true, false), builtByFactory.getAdvertisedFeaturesV10());
        Assert.assertEquals("Wrong builtByFactory - supbuiltByFactoryed", new PortFeaturesV10(true, true, false, false, false, false,
                false, true, false, true, false, false), builtByFactory.getSupportedFeaturesV10());
        Assert.assertEquals("Wrong builtByFactory - peer", new PortFeaturesV10(true, false, false, false, false, false, false,
                false, true, false, false, true), builtByFactory.getPeerFeaturesV10());
    }
}
