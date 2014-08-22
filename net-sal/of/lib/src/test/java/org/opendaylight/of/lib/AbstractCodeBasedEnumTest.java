/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * A base class for unit tests of enums that embody a code.
 *
 * @param <E> type of the code-based enumeration
 *
 * @author Simon Hunt
 */
public abstract class AbstractCodeBasedEnumTest<E extends OfpCodeBasedEnum>
        extends AbstractTest {

    /** Check that a given code (for a given version) decodes to the correct
     * constant. If exp is null, an exception is expected; usually
     * DecodeException, unless vme is true, in which case
     * VersionMismatchException is expected instead.
     *
     * @param pv the protocol version
     * @param code the code
     * @param exp the expected constant
     * @param vme expect VersionMismatchException
     */
    protected void check(ProtocolVersion pv, int code, E exp, boolean vme) {
        E type;
        try {
            type = decode(code, pv);
            if (exp != null) {
                int t = type == null ? code : type.getCode(pv);
                print("{} -> {}", t, type);
                assertEquals(AM_NEQ, exp, type);
                assertEquals(AM_NEQ, code, t);
            } else
                fail(AM_NOEX);
        } catch (VersionMismatchException v) {
            if (exp == null && vme)
                print(FMT_EX, v);
            else {
                print(v);
                fail(AM_WREX);
            }
        } catch (DecodeException e) {
            if (exp == null && !vme)
                print(FMT_EX, e);
            else {
                print(e);
                fail(AM_WREX);
            }
        }
    }


    /** Check that a given code (for a given version) decodes to the correct
     * constant. If exp is null, a DecodeException is expected.
     *
     * @param pv the protocol version
     * @param code the code
     * @param exp the expected constant
     */
    protected void check(ProtocolVersion pv, int code, E exp) {
        check(pv, code, exp, false);
    }

    /** Checks that the given code, for the given version, is either
     * unknown or unsupported and throws a DecodeException or a
     * VersionMismatchException.
     *
     * @param pv the protocol version
     * @param code the code (that should be unsupported)
     */
    protected void notSupCode(ProtocolVersion pv, int code) {
        try {
            decode(code, pv);
            fail(AM_NOEX);
        } catch (DecodeException de) {
            print(FMT_EX, de);
        } catch (VersionMismatchException vme) {
            print(FMT_EX, vme);
        } catch (Exception e) {
            print(e);
            fail(AM_WREX);
        }
    }

    /** Iterates from -1 to 10, and includes 0xffff, and checks that
     * a VersionMismatchException is thrown for all those values for the
     * given protocol version
     * (by calling {@link #notSupCode(ProtocolVersion, int)}.
     * This is not an exhaustive test, but it
     * gives some confidence that the given protocol version is not
     * supported.
     *
     * @param pv the protocol version
     */
    protected void notSup(ProtocolVersion pv) {
        for (int code=-1; code<10; code++)
            notSupCode(pv, code);
        notSupCode(pv, 0xffff);
    }


    /** Subclasses must call their static decode(...) method and return
     * the result here.
     *
     * @param code the code to interpret
     * @param pv the protocol version
     * @return the mapped constant to the code
     * @throws DecodeException if the code is not recognized
     */
    protected abstract E decode(int code, ProtocolVersion pv)
        throws DecodeException;
}
