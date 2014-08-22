/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.mockswitch;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.util.junit.TestTools.*;


/**
 * Unit Test for AbstractDefn.
 *
 * @author Simon Hunt
 */
public class AbstractDefnTest {

    // READER fixture
    private static class Reader extends AbstractDefReader {
        protected Reader(String path) {
            super(path);
        }
        @Override
        protected String parseLogicalLine(LogicalLine line) {
            return null;  // accepts everything
        }
    }

    // DEFN fixture
    private static class Defn extends AbstractDefn {
        public Defn(String path) {
            super(new Reader(path));
        }
    }

    @Test
    public void basic() {
        print(EOL + "basic()");
        String path = "org/opendaylight/of/mockswitch/reader01.def";
        Defn defn = new Defn(path);
        print(defn.toDebugString());
        assertEquals(AM_NEQ, path, defn.getPath());
        assertEquals(AM_UXS, 4, defn.reader.getLines().size());
    }
}
