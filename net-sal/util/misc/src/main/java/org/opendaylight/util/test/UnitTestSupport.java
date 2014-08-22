/*
 * (c) Copyright 2010-2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.test;

/**
 * An interface for unit test support. It is up to each implementation
 * to decide what each of the method calls mean. To allow for distinction
 * between multiple types of the same operation, method signatures with an
 * enumeration constant identifier are also provided. It is up to the
 * implementation to provide the enum and interpret the values.
 *
 * @author Simon Hunt
 */
public interface UnitTestSupport {

    /** Clears something; see the production code to determine what
     * is cleared.
     */
    void clear();

    /** Clears something, using the specified id; see the production code
     * to determine what is cleared.
     *
     * @param id an identifier
     */
    void clear(Enum<?> id);

    /** Resets something; see the production code to determine what is reset. */
    void reset();

    /** Resets something, using the specified id; see the production code
     * to determine what is reset.
     *
     * @param id an identifier
     */
    void reset(Enum<?> id);

    /** Sets something; see the production code to determine what is set. */
    void set();

    /** Sets something, using the specified id; see the production code
     * to determine what is set.
     *
     * @param id an identifier
     */
    void set(Enum<?> id);

    /* IMPLEMENTATION NOTE:
     *  If further methods are added to this interface, the following classes
     *  will need to be updated:
     *   - UnitTestSupportAdapter - implement the methods
     *   - UnitTestSupportProxy - add method names to the UTS_METHODS set
     */
}
