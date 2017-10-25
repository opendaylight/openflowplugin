/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.util;

import org.junit.Assert;
import org.junit.Test;
/**
 * test of {@link ActionUtil}
 */
public class ActionUtilTest {

    /**
     * Test method for {@link org.opendaylight.openflowplugin.openflow.md.util.ActionUtil#tosToDscp(short)}.
     */
    @Test
    public void testTosToDscp() {
        Assert.assertEquals(0, ActionUtil.tosToDscp((short) 1).intValue());
        Assert.assertEquals(1, ActionUtil.tosToDscp((short) 4).intValue());
        Assert.assertEquals(63, ActionUtil.tosToDscp((short) 252).intValue());
    }

    /**
     * Test method for {@link org.opendaylight.openflowplugin.openflow.md.util.ActionUtil#dscpToTos(short)}.
     */
    @Test
    public void testDscpToTos() {
        Assert.assertEquals(0, ActionUtil.dscpToTos((short) 0).intValue());
        Assert.assertEquals(4, ActionUtil.dscpToTos((short) 1).intValue());
        Assert.assertEquals(16, ActionUtil.dscpToTos((short) 4).intValue());
        Assert.assertEquals(252, ActionUtil.dscpToTos((short) 63).intValue());
    }
}
