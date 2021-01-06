/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.api.keys.experimenter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.keys.ExperimenterIdDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.MessageCodeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterMessage;

/**
 * Unit tests for ExperimenterIdDeserializerKey.
 *
 * @author michal.polkorab
 */
public class ExperimenterIdDeserializerKeyTest {

    /**
     * Test ExperimenterIdDeserializerKey equals and hashCode.
     */
    @Test
    public void test() {
        ExperimenterIdDeserializerKey<?> key1 =
                new ExperimenterIdDeserializerKey<>(EncodeConstants.OF10_VERSION_ID, 42L, ExperimenterMessage.class);
        ExperimenterIdDeserializerKey<?> key2 =
                new ExperimenterIdDeserializerKey<>(EncodeConstants.OF10_VERSION_ID, 42L, ExperimenterMessage.class);
        assertTrue("Wrong equals", key1.equals(key2));
        assertTrue("Wrong hashcode", key1.hashCode() == key2.hashCode());
        key2 = new ExperimenterIdDeserializerKey<>(EncodeConstants.OF13_VERSION_ID, 42L, ExperimenterMessage.class);
        assertFalse("Wrong equals", key1.equals(key2));
        assertFalse("Wrong hashcode", key1.hashCode() == key2.hashCode());
        key2 = new ExperimenterIdDeserializerKey<>(EncodeConstants.OF10_VERSION_ID, 0L, ExperimenterMessage.class);
        assertFalse("Wrong equals", key1.equals(key2));
        assertFalse("Wrong hashcode", key1.hashCode() == key2.hashCode());
        key2 = new ExperimenterIdDeserializerKey<>(EncodeConstants.OF10_VERSION_ID, 55L, ExperimenterMessage.class);
        assertFalse("Wrong equals", key1.equals(key2));
        assertFalse("Wrong hashcode", key1.hashCode() == key2.hashCode());
        key2 = new ExperimenterIdDeserializerKey<>(EncodeConstants.OF10_VERSION_ID, 55L, null);
        assertFalse("Wrong equals", key1.equals(key2));
        assertFalse("Wrong hashcode", key1.hashCode() == key2.hashCode());
        key2 = new ExperimenterIdDeserializerKey<>(EncodeConstants.OF10_VERSION_ID, 55L, ErrorMessage.class);
        assertFalse("Wrong equals", key1.equals(key2));
        assertFalse("Wrong hashcode", key1.hashCode() == key2.hashCode());
    }

    /**
     * Test ExperimenterIdDeserializerKey equals - additional test.
     */
    @Test
    public void testEquals() {
        ExperimenterIdDeserializerKey<?> key1 =
                new ExperimenterIdDeserializerKey<>(EncodeConstants.OF10_VERSION_ID, 0L, ExperimenterMessage.class);
        assertTrue("Wrong equal to identical object.", key1.equals(key1));
        MessageCodeKey<?> mk = new MessageCodeKey<>(EncodeConstants.OF10_VERSION_ID,
                EncodeConstants.EXPERIMENTER_VALUE, ExperimenterMessage.class);
        assertFalse("Wrong equal to different class.", key1.equals(mk));
        ExperimenterIdDeserializerKey<?> key2 =
                new ExperimenterIdDeserializerKey<>(EncodeConstants.OF10_VERSION_ID, 42L, ExperimenterMessage.class);
        assertFalse("Wrong equal by experimenterId.", key1.equals(key2));
    }

}
