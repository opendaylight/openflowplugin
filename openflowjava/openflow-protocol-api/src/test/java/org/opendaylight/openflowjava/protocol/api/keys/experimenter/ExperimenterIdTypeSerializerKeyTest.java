/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.api.keys.experimenter;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.keys.ExperimenterIdSerializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.ExperimenterIdTypeSerializerKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterMessage;

/**
 * @author michal.polkorab
 */
public class ExperimenterIdTypeSerializerKeyTest {

    /**
     * Test ExperimenterIdTypeSerializerKey equals and hashCode
     */
    @Test
    public void testHashCodeAndEquals() {
        ExperimenterIdTypeSerializerKey<?> key1 =
                new ExperimenterIdTypeSerializerKey<>(EncodeConstants.OF10_VERSION_ID, 42L, 1L, ExperimenterMessage.class);
        ExperimenterIdTypeSerializerKey<?> key2 =
                new ExperimenterIdTypeSerializerKey<>(EncodeConstants.OF10_VERSION_ID, 42L, 1L, ExperimenterMessage.class);
        Assert.assertTrue("Wrong equals", key1.equals(key2));
        Assert.assertTrue("Wrong hashcode", key1.hashCode() == key2.hashCode());
        key2 = new ExperimenterIdTypeSerializerKey<>(EncodeConstants.OF13_VERSION_ID, 42L, 1L, ExperimenterMessage.class);
        Assert.assertFalse("Wrong equals", key1.equals(key2));
        Assert.assertFalse("Wrong hashcode", key1.hashCode() == key2.hashCode());
        key2 = new ExperimenterIdTypeSerializerKey<>(EncodeConstants.OF10_VERSION_ID, 55L, 1L, ExperimenterMessage.class);
        Assert.assertFalse("Wrong equals", key1.equals(key2));
        Assert.assertFalse("Wrong hashcode", key1.hashCode() == key2.hashCode());
        key2 = new ExperimenterIdTypeSerializerKey<>(EncodeConstants.OF10_VERSION_ID, 55L, 1L, null);
        Assert.assertFalse("Wrong equals", key1.equals(key2));
        Assert.assertFalse("Wrong hashcode", key1.hashCode() == key2.hashCode());
        key2 = new ExperimenterIdTypeSerializerKey<>(EncodeConstants.OF10_VERSION_ID, 55L, 1L, ErrorMessage.class);
        Assert.assertFalse("Wrong equals", key1.equals(key2));
        Assert.assertFalse("Wrong hashcode", key1.hashCode() == key2.hashCode());

        key2 = new ExperimenterIdTypeSerializerKey<>(EncodeConstants.OF10_VERSION_ID, 42L, 2L, ExperimenterMessage.class);
        Assert.assertFalse("Wrong equals", key1.equals(key2));
        Assert.assertFalse("Wrong hashcode", key1.hashCode() == key2.hashCode());
    }

    /**
     * Test ExperimenterIdTypeSerializerKey equals - additional test
     */
    @Test
    public void testEquals() {
        ExperimenterIdTypeSerializerKey<?> key1 =
                new ExperimenterIdTypeSerializerKey<>(EncodeConstants.OF10_VERSION_ID, 41L, 1L, ExperimenterMessage.class);
        Assert.assertTrue("Wrong equal to identical object.", key1.equals(key1));
        ExperimenterIdSerializerKey<?> mk = new ExperimenterIdSerializerKey<>(EncodeConstants.OF10_VERSION_ID, 42L, ExperimenterMessage.class);
        Assert.assertFalse("Wrong equal to different class.", key1.equals(mk));
        ExperimenterIdTypeSerializerKey<?> key2 =
                new ExperimenterIdTypeSerializerKey<>(EncodeConstants.OF10_VERSION_ID, 42L, 1L, ExperimenterMessage.class);
        Assert.assertFalse("Wrong equal by experimenterId.", key1.equals(key2));

        ExperimenterIdTypeSerializerKey<?> key3 =
                new ExperimenterIdTypeSerializerKey<>(EncodeConstants.OF10_VERSION_ID, 41L, 2L, ExperimenterMessage.class);
        Assert.assertFalse("Wrong equal by type.", key1.equals(key3));
    }

}