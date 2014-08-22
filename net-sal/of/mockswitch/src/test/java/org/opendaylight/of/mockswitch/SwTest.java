/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.mockswitch;

import org.opendaylight.util.junit.TestTools;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.opendaylight.util.junit.TestTools.AM_UXS;

/**
 * Abstract base class for unit tests.
 *
 * @author Simon Hunt
 */
public abstract class SwTest {

    protected final boolean showOutput = TestTools.showOutput();

    /** Verifies that the expected flags are present in the given set.
     *
     * @param set the actual set
     * @param flags the expected flags
     */
    protected void verifyFlags(Set<? extends Enum<?>> set, Enum<?>... flags) {
        assertEquals(AM_UXS, flags.length, set.size());
        for (Enum<?> e: flags)
            assertTrue("missing flag", set.contains(e));
    }
}
