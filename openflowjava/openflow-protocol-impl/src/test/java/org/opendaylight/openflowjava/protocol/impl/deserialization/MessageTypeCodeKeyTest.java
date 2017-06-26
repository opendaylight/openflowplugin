/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.deserialization;

import org.junit.Assert;
import org.junit.Test;


/**
 *
 * @author madamjak
 *
 */
public class MessageTypeCodeKeyTest {

    /**
     * Tests equals and hashCode
     */
    @Test
    public void testEqualsAndHashcode(){
        short msgType1 = 12;
        short msgVersion1 = 34;
        short msgType2 = 21;
        short msgVersion2 = 43;
        MessageTypeCodeKey key1 = new MessageTypeCodeKey(msgVersion1, msgType1);

        Assert.assertTrue("Wrong - equals to same object", key1.equals(key1));
        Assert.assertFalse("Wrong - equals to null", key1.equals(null));
        Assert.assertFalse("Wrong - equals to different class", key1.equals(new Object()));

        MessageTypeCodeKey key2 = new MessageTypeCodeKey(msgVersion1, msgType1);
        Assert.assertTrue("Wrong hashcode", key1.hashCode() == key2.hashCode());
        Assert.assertTrue("Wrong - equals to mirror object", key1.equals(key2));

        key2 = new MessageTypeCodeKey(msgVersion2, msgType2);
        Assert.assertFalse("Wrong hashcode", key1.hashCode() == key2.hashCode());
        Assert.assertFalse("Wrong - equals by msgType", key1.equals(key2));

        key2 = new MessageTypeCodeKey(msgVersion2, msgType1);
        Assert.assertFalse("Wrong - equals by msgVersion", key1.equals(key2));
        Assert.assertFalse("Wrong hashcode", key1.hashCode() == key2.hashCode());
    }

    /**
     * Tests getters
     */
    @Test
    public void testGetter(){
        short msgType = 12;
        short msgVersion = 34;
        MessageTypeCodeKey key1 = new MessageTypeCodeKey(msgVersion, msgType);

        Assert.assertEquals(msgType, key1.getMsgType());
        Assert.assertEquals(msgVersion, key1.getMsgVersion());
    }
}