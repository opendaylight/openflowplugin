/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.json;

import org.junit.Test;

import java.util.MissingResourceException;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for {@link CommonValues}.
 *
 * @author Simon Hunt
 */
public class CommonValuesTest {

    private static final String NA_KEY = "na";
    private static final String NA_VAL = "n/a";

    private static final String NO_SUCH_KEY= "i_do_not_exist";

    @Test
    public void basic() {
        print(EOL + "basic()");
        String actual = CommonValues.lookup(NA_KEY);
        print("{} => {}", NA_KEY, actual);
        assertEquals(AM_NEQ, NA_VAL, actual);
    }

    @Test(expected = NullPointerException.class)
    public void nullKey() {
        CommonValues.lookup(null);
    }

    @Test(expected = MissingResourceException.class)
    public void noSuchKey() {
        CommonValues.lookup(NO_SUCH_KEY);
    }
}
