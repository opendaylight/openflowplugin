/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.serialization;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.util.OF13MatchSerializer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.grouping.Match;

/**
 * Unit tests for SerializerRegistryImpl.
 *
 * @author madamjak
 */
public class SerializerRegistryImplTest {

    private static final short OF13 = EncodeConstants.OF13_VERSION_ID;
    private static final short OF10 = EncodeConstants.OF10_VERSION_ID;

    /**
     * Test - register serializer without arguments.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testRegisterSerializerNoArgs() {

        SerializerRegistryImpl serReg = new SerializerRegistryImpl();
        serReg.registerSerializer(null, null);
    }

    /**
     * Test - unregister serializer without MessageTypeKey.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testUnRegisterSerializerNoMessageTypeKey() {
        SerializerRegistryImpl serReg = new SerializerRegistryImpl();
        serReg.init();
        serReg.registerSerializer(new MessageTypeKey<>(OF13, Match.class), new OF13MatchSerializer(serReg));
        serReg.unregisterSerializer(null);
    }

    /**
     * Test - unregister serializer.
     */
    @Test
    public void testUnRegisterSerializer() {
        SerializerRegistryImpl serReg = new SerializerRegistryImpl();
        serReg.init();
        serReg.registerSerializer(new MessageTypeKey<>(OF13, Match.class), new OF13MatchSerializer(serReg));
        assertTrue("Wrong - unregister serializer",
                serReg.unregisterSerializer(new MessageTypeKey<>(OF13, Match.class)));

        serReg.registerSerializer(new MessageTypeKey<>(OF13, Match.class), new OF13MatchSerializer(serReg));
        assertFalse("Wrong - unregister serializer",
                serReg.unregisterSerializer(new MessageTypeKey<>(OF10, Match.class)));
    }
}
