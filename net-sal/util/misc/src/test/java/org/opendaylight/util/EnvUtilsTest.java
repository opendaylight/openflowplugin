/*
 * (c) Copyright 2011 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opendaylight.util.EnvUtils;

/**
 * Simple test suite for the environment utilities.
 *
 * @author Thomas Vachuska
 */
public class EnvUtilsTest {
    
    private static final String SOP = "show.output";
    private static final String AUX = "some.really.wierd.test.property";

    @Test
    public void showOutput() {
        // Remember the original setting, since we don't want to mess up the
        // desired setting for tests that follow us.
        boolean original = EnvUtils.showOutput();
        
        // Set the value to the negative and expect it to come out like that...
        System.setProperty(SOP, Boolean.toString(!original));
        assertEquals("incorrect property value", !original, EnvUtils.showOutput());
        
        // Now reset the original and expect it to come out like that...
        System.setProperty(SOP, Boolean.toString(original));
        assertEquals("incorrect property value", original, EnvUtils.showOutput());
    }

    @Test
    public void booleanProperty() {
        // Start false as a default
        assertFalse("incorrect property value", EnvUtils.isPropertyTrue(AUX));
        
        // Set it to true and expect it to come out like that...
        System.setProperty(AUX, "true");
        assertTrue("incorrect property value", EnvUtils.isPropertyTrue(AUX));

        // Set it to false and expect it to come out like that...
        System.setProperty(AUX, "false");
        assertFalse("incorrect property value", EnvUtils.isPropertyTrue(AUX));
    }

}
