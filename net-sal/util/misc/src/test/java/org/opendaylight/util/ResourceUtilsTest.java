/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.util;

import java.util.ResourceBundle;

import static org.opendaylight.util.junit.TestTools.AM_HUH;
import static org.opendaylight.util.junit.TestTools.print;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * This JUnit test class tests the ResourceUtils class.
 *
 * @author Simon Hunt
 */
public class ResourceUtilsTest {

    @Test
    public void getBundleByClassName() {
        ResourceBundle res = ResourceUtils.getBundledResource(FixtureResource.class);
        assertNotNull(AM_HUH, res);
        String value = res.getString("key1");
        assertTrue(AM_HUH, value.contains("one"));
        print(value);
    }

    @Test
    public void getBundleByOtherName() {
        ResourceBundle res =
                ResourceUtils.getBundledResource(FixtureResource.class, "FixtureOtherResource");
        assertNotNull(AM_HUH, res);
        String value = res.getString("keyA");
        assertTrue(AM_HUH, value.contains("Aye"));
        print(value);
    }
}
