/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import static org.junit.Assert.*;

import org.junit.Test;
import org.opendaylight.util.UIDGenerator;

/**
 * Simple test for the UID generator facility.
 *
 * @author Thomas Vachuska
 */
public class UIDGeneratorTest {
    
    @Test
    public void basics() {
        String a = UIDGenerator.newUID();
        String b = UIDGenerator.newUID();
        assertFalse("two random UIDs should not equal", a.equals(b));
    }

}
