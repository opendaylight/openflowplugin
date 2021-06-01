/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization;

import static org.opendaylight.openflowjava.protocol.api.util.EncodeConstants.EMPTY_VALUE;
import static org.opendaylight.openflowjava.protocol.api.util.EncodeConstants.OF_VERSION_1_0;
import static org.opendaylight.openflowjava.protocol.api.util.EncodeConstants.OF_VERSION_1_3;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.keys.MessageCodeKey;
import org.opendaylight.openflowjava.protocol.impl.util.MatchDeserializer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.grouping.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.v10.grouping.MatchV10;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Unit tests for DeserializerRegistryImpl.
 *
 * @author madamjak
 */
public class DeserializerRegistryImplTest {
    /**
     * Test - register deserializer without arguments.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testRegisterDeserializerNoArgs() {
        DeserializerRegistryImpl serReg = new DeserializerRegistryImpl();
        serReg.registerDeserializer(null, null);
    }

    /**
     * Test - register deserializer with no key.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testRegisterDeserializerNoKey() {
        DeserializerRegistryImpl serReg = new DeserializerRegistryImpl();
        serReg.registerDeserializer(null, new MatchDeserializer());
    }

    /**
     * Test - register deserializer with no deserializer.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testRegisterDeserializerNoDeserializer() {
        DeserializerRegistryImpl serReg = new DeserializerRegistryImpl();
        serReg.registerDeserializer(new MessageCodeKey(OF_VERSION_1_3, EMPTY_VALUE, Match.class), null);
    }

    /**
     * Test - unregister deserializer without MessageTypeKey.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testUnRegisterDeserializerNoMessageTypeKey() {
        DeserializerRegistryImpl derserReg = new DeserializerRegistryImpl();
        derserReg.init();
        derserReg.unregisterDeserializer(null);
    }

    /**
     * Test - unregister deserializer.
     */
    @Test
    public void testUnRegisterDeserializer() {
        DeserializerRegistryImpl derserReg = new DeserializerRegistryImpl();
        derserReg.init();
        Assert.assertTrue("Wrong - unregister serializer",derserReg.unregisterDeserializer(
                new MessageCodeKey(OF_VERSION_1_3, EMPTY_VALUE, Match.class)));
        Assert.assertFalse("Wrong - unregister serializer",derserReg.unregisterDeserializer(
                new MessageCodeKey(OF_VERSION_1_0, EMPTY_VALUE, Match.class)));
    }

    /**
     * Test - get deserializer.
     */
    @Test(expected = IllegalStateException.class)
    public void testGetDeserializer() {
        DeserializerRegistryImpl registry = new DeserializerRegistryImpl();
        registry.init();
        registry.getDeserializer(new MessageCodeKey(Uint8.MAX_VALUE, EMPTY_VALUE, MatchV10.class));
        Assert.fail();
    }
}
