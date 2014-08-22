/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.test;

import org.opendaylight.util.test.UnitTestSupportAdapter;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract superclass for the proxy unit tests.
 *
 * @author Simon Hunt
 */
abstract class AbstractUnitTestSupportProxyTest {

    /** A sample enumeration. */
    static enum ID { Foo, Bar, Baz }

    /** Implementation of the unit test support interface. */
    static class MyUTS extends UnitTestSupportAdapter {
        private boolean cleared = false;
        private boolean reset = false;
        private boolean set = false;
        private Map<Enum<?>, Boolean> clearMap = new HashMap<Enum<?>, Boolean>();
        private Map<Enum<?>, Boolean> resetMap = new HashMap<Enum<?>, Boolean>();
        private Map<Enum<?>, Boolean> setMap = new HashMap<Enum<?>, Boolean>();

        @Override public void clear() { cleared = true; }
        @Override public void reset() { reset = true; }
        @Override public void set() { set = true; }
        @Override public void clear(Enum<?> id) { clearMap.put(id, true); }
        @Override public void reset(Enum<?> id) { resetMap.put(id, true); }
        @Override public void set(Enum<?> id) { setMap.put(id, true); }

        public boolean wasCleared() {
            return cleared;
        }

        public boolean wasReset() {
            return reset;
        }

        public boolean wasSet() {
            return set;
        }

        public boolean wasCleared(Enum<?> e) {
            Boolean b = clearMap.get(e);
            return b != null && b;
        }

        public boolean wasReset(Enum<?> e) {
            Boolean b = resetMap.get(e);
            return b != null && b;
        }

        public boolean wasSet(Enum<?> e) {
            Boolean b = setMap.get(e);
            return b != null && b;
        }
    }

}
