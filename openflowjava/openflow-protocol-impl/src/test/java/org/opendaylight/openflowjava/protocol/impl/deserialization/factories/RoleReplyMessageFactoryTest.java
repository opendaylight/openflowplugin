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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.RoleRequestOutput;

/**
 * @author timotej.kubas
 * @author michal.polkorab
 */
public class RoleReplyMessageFactoryTest {

    private OFDeserializer<RoleRequestOutput> roleFactory;

    /**
     * Initializes deserializer registry and lookups correct deserializer
     */
    @Before
    public void startUp() {
        DeserializerRegistry registry = new DeserializerRegistryImpl();
        registry.init();
        roleFactory = registry.getDeserializer(
                new MessageCodeKey(EncodeConstants.OF13_VERSION_ID, 25, RoleRequestOutput.class));
    }

   /**
    * Testing of {@link RoleReplyMessageFactory} for correct translation into POJO
    */
    @Test
    public void test(){
        ByteBuf bb = BufferHelper.buildBuffer("00 00 00 02 00 00 00 00 00 01 02 03 04 05 06 07");
        RoleRequestOutput builtByFactory = BufferHelper.deserialize(roleFactory, bb);

        BufferHelper.checkHeaderV13(builtByFactory);

        Assert.assertEquals("Wrong role", 0x02, builtByFactory.getRole().getIntValue());
        Assert.assertEquals("Wrong generationId", 0x01020304050607L, builtByFactory.getGenerationId().longValue());
    }
}
