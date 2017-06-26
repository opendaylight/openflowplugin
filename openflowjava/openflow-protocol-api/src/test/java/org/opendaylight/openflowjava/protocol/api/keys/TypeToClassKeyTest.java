/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.api.keys;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
/**
 *
 * @author madamjak
 *
 */
public class TypeToClassKeyTest {

    /**
     * Test equals
     */
    @Test
    public void test(){
        final short ver10 = EncodeConstants.OF10_VERSION_ID;
        final short ver13 = EncodeConstants.OF13_VERSION_ID;
        final int type1 = 1;
        final int type2 = 2;
        TypeToClassKey typeToClsKey10 = new TypeToClassKey(ver10,type1);
        Assert.assertTrue("Wrong - equals to same object", typeToClsKey10.equals(typeToClsKey10));
        Assert.assertFalse("Wrong - equals to null", typeToClsKey10.equals(null));
        Assert.assertFalse("Wrong - equals to different class", typeToClsKey10.equals(new Object()));
        TypeToClassKey typeToClsKey13 = new TypeToClassKey(ver13,type2);
        Assert.assertFalse("Wrong - equals by different version", typeToClsKey13.equals(new Object()));
        Assert.assertFalse("Wrong - equals by different type", typeToClsKey13.equals(typeToClsKey10));
    }
}
