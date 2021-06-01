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
import org.opendaylight.openflowjava.protocol.api.keys.ExperimenterInstructionSerializerKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;

/**
 * Unit tests for ExperimenterInstructionSerializerKey.
 *
 * @author michal.polkorab
 */
public class ExperimenterInstructionSerializerKeyTest {
    /**
     * Test ExperimenterInstructionSerializerKey equals and hashCode.
     */
    @Test
    public void test() {
        ExperimenterInstructionSerializerKey key1 =
                new ExperimenterInstructionSerializerKey(EncodeConstants.OF_VERSION_1_0, 42L);
        ExperimenterInstructionSerializerKey key2 =
                new ExperimenterInstructionSerializerKey(EncodeConstants.OF_VERSION_1_0, 42L);
        Assert.assertTrue("Wrong equals", key1.equals(key2));
        Assert.assertTrue("Wrong hashcode", key1.hashCode() == key2.hashCode());
        key2 = new ExperimenterInstructionSerializerKey(EncodeConstants.OF_VERSION_1_3, 42L);
        Assert.assertFalse("Wrong equals", key1.equals(key2));
        Assert.assertFalse("Wrong hashcode", key1.hashCode() == key2.hashCode());
        key2 = new ExperimenterInstructionSerializerKey(EncodeConstants.OF_VERSION_1_0, null);
        Assert.assertFalse("Wrong equals", key1.equals(key2));
        Assert.assertFalse("Wrong hashcode", key1.hashCode() == key2.hashCode());
        key2 = new ExperimenterInstructionSerializerKey(EncodeConstants.OF_VERSION_1_0, 55L);
        Assert.assertFalse("Wrong equals", key1.equals(key2));
        Assert.assertFalse("Wrong hashcode", key1.hashCode() == key2.hashCode());
    }
}
