/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.cache;

import static org.opendaylight.util.junit.TestTools.EOL;
import static org.opendaylight.util.junit.TestTools.print;

/**
 * This class forms the basis for unit testing the Age-Out Map classes.
 *
 * @author Simon Hunt
 */
public abstract class AbstractAgeOutTest {

    static enum Hobbit { FRODO, SAM, MERRY, PIPPIN }

    static final String SWORD = "sword";
    static final String RING = "ring";
    static final String STAFF = "staff";
    static final String BREAD = "bread";

    static final long TEST_AGE_OUT = 100;
    static final String FMT_EX = "EX> {}";

    Watch watch;

    void startTest(String s) {
        watch = new Watch(s);
        print(EOL + watch);
    }

    void endTest() {
        print(watch.stop());
    }
}
