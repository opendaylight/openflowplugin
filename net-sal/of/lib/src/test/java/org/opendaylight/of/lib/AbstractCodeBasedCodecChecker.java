/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib;


import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * A base class for testing OfpCodeBasedEnum enumerations that utilize the
 * OfpCodeBasedCodec for encoding/decoding the enumeration constants to/from a
 * bitmap.
 *
 * @param <E> type of the code-based enumeration
 *
 * @author Scott Simes
 * @author Simon Hunt
 */
public abstract class AbstractCodeBasedCodecChecker<E extends OfpCodeBasedEnum>
        extends AbstractTest {

    /**
     * Checks the encoding of a set of flags into the correct bitmap.
     * An expected bitmap of {@link #EXPECT_EXCEPTION} indicates that a
     * VersionMismatchException is expected to be thrown.
     *
     * @param pv the protocol version
     * @param expBitmap the expected bitmap
     * @param flags the flags to be encoded
     */
    public void checkEncode(ProtocolVersion pv, int expBitmap, E... flags) {
        checkEncode(pv, expBitmap, false, flags);
    }

    /** Bitmap value denoting that we expect an exception from encodeFlags(). */
    public static final int EXPECT_EXCEPTION = -1;

    /**
     * Checks the encoding of a set of flags into the correct bitmap.  If the
     * expected bitmap parameter is -1, an exception is expected; if expIae
     * is false, a VersionMismatchException; if expIae is true, then an
     * IllegalArgumentException is expected instead.
     *
     * @param pv the protocol version
     * @param expBitmap the expected bitmap
     * @param expIae expected IllegalArgumentException
     * @param flags the flags to be encoded
     */
     public void checkEncode(ProtocolVersion pv, int expBitmap, boolean expIae,
                             E... flags) {
         Set<E> set = new TreeSet<E>(Arrays.asList(flags));
         try {
             int bitmap = encodeFlags(set, pv);
             if (expBitmap == EXPECT_EXCEPTION)
                 fail(AM_NOEX);

             print(" {} -> {}", set, hex(bitmap));
             assertEquals(AM_NEQ, expBitmap, bitmap);

         } catch (VersionMismatchException e) {
             if (expBitmap == EXPECT_EXCEPTION && !expIae)
                 print(FMT_EX, e);
             else {
                 print(e);
                 fail(AM_WREX);
             }
         } catch (IllegalArgumentException e) {
             if (expBitmap == EXPECT_EXCEPTION && expIae)
                 print(FMT_EX, e);
             else {
                 print(e);
                 fail(AM_WREX);
             }
         }
     }

    /**
     * Checks the decoding of a bitmap into the set of flags.
     * An array of zero length denotes the expectation that a
     * VersionMismatchException will be thrown.
     *
     * @param pv the protocol version
     * @param bitmap the bitmap to decode
     * @param expFlags the expected set of flags
     */
    public void checkDecode(ProtocolVersion pv, int bitmap, E... expFlags) {
        try {
            Set<E> flags = decodeFlags(bitmap, pv);
            if (expFlags.length > 0) {
                Set<E> expSet = new TreeSet<E>(Arrays.asList(expFlags));
                print(" {} -> {}", hex(bitmap), flags);
                assertEquals(AM_NEQ, expSet, flags);
            } else
                fail(AM_NOEX);
        } catch (VersionMismatchException e) {
            if (expFlags.length == 0)
                print(FMT_EX, e);
            else {
                print(e);
                fail(AM_WREX);
            }
        } catch (Exception e) {
            print(e);
            fail(AM_WREX);
        }
    }

    /**
     * Subclasses must call their static decodeFlags(...) method and return
     * the result here.
     *
     * @param bitmap the bitmap to decode
     * @param pv the protocol version
     * @return the corresponding set of flags
     */
    protected abstract Set<E> decodeFlags(int bitmap, ProtocolVersion pv);

    /**
     * Subclasses must call their static encodeFlags(...) method and return
     * the result here.
     *
     * @param flags the set of flags to encode
     * @param pv the protocol version
     * @return the encoded bitmap of flags
     */
    protected abstract int encodeFlags(Set<E> flags, ProtocolVersion pv);
}
