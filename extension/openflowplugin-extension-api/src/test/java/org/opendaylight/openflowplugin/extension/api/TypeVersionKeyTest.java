/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.api;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloMessage;
import org.opendaylight.yangtools.binding.DataObject;

/**
 * Unit tests for TypeVersionKey.
 *
 * @author michal.polkorab
 */
public class TypeVersionKeyTest {

    /**
     * Tests {@link TypeVersionKey#equals(Object)} and {@link TypeVersionKey#hashCode()}.
     */
    @Test
    public void test() {
        TypeVersionKey<? extends DataObject> key1 =
                new TypeVersionKey<>(HelloMessage.class, EncodeConstants.OF_VERSION_1_3);
        TypeVersionKey<? extends DataObject> key2 =
                new TypeVersionKey<>(HelloMessage.class, EncodeConstants.OF_VERSION_1_3);
        Assert.assertTrue("Wrong equals()", key1.equals(key2));
        Assert.assertEquals("Wrong hashCode()", key1.hashCode(), key2.hashCode());

        key2 = new TypeVersionKey<>(HelloMessage.class, EncodeConstants.OF_VERSION_1_0);
        Assert.assertFalse("Wrong equals()", key1.equals(key2));
        Assert.assertFalse("Wrong hashCode()", key1.hashCode() == key2.hashCode());

        key2 = new TypeVersionKey<>(BarrierReply.class, EncodeConstants.OF_VERSION_1_3);
        Assert.assertFalse("Wrong equals()", key1.equals(key2));
        Assert.assertFalse("Wrong hashCode()", key1.hashCode() == key2.hashCode());
    }
}
