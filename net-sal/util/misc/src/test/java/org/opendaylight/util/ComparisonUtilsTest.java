/*
 * (c) Copyright 2011 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import static org.opendaylight.util.junit.TestTools.*;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * This JUnit test class tests the ComparisonUtils class.
 *
 * @author Simon Hunt
 */
public class ComparisonUtilsTest {

    @Test
    public void compareBooleans() {
        print(EOL + "compareBooleans()");
        assertEquals("should be 0", 0, ComparisonUtils.compareBooleans(false, false));
        assertEquals("should be 0", -1, ComparisonUtils.compareBooleans(false, true));
        assertEquals("should be 0", 1, ComparisonUtils.compareBooleans(true, false));
        assertEquals("should be 0", 0, ComparisonUtils.compareBooleans(true, true));
    }

}
