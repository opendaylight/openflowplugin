/*
 * (c) Copyright 2010-2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.test;

/**
 * An adapter for the {@link UnitTestSupport} interface. Implementing classes
 * should extend this class rather than implementing the interface directly;
 * that way, if new methods are added to the interface, the adapter will
 * absorb the change and the implementing classes won't break at compile time.
 *
 * @author Simon Hunt
 */
public class UnitTestSupportAdapter implements UnitTestSupport {
    @Override public void clear() { }
    @Override public void clear(Enum<?> id) { }
    @Override public void reset() { }
    @Override public void reset(Enum<?> id) { }
    @Override public void set() { }
    @Override public void set(Enum<?> id) { }
}
