/*
 * (c) Copyright 2010-2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.net;

import java.util.ArrayList;
import java.util.List;

/**
 * Useful utility methods for manipulating {@link MacAddress MAC addresses}.
 *
 * @author Simon Hunt
 */
public final class MacUtils extends AbstractUtils {

    // no instantiation
    private MacUtils() { }

    /** Returns a MAC address where all the bytes are set to the
     * specified seed value.
     * For example:
     * <pre>
     * MacAddress mac = MacUtils.getRepeatedByteMacAddress(249);
     * </pre>
     * will return the MAC address {@code F9:F9:F9:F9:F9:F9}
     *
     * @param seed the seed value
     * @return the MAC address
     * @throws IllegalArgumentException if seed is not in the range 0..255
     */
    public static MacAddress getRepeatedByteMacAddress(int seed) {
        return MacAddress.valueOf(getRepeatedByteArray(seed, 6));
    }

    /** Returns a list of randomly generated MAC addresses.
     * See {@link MacRange} for a definition of the range specification.
     * Note that addresses may be repeated in the list.
     *
     * @param spec a string defining the set of MAC addresses from which to
     *             draw the random samples
     * @param count the number of MAC addresses to return in the list
     * @return a list of randomly generated MAC addresses
     * @throws IllegalArgumentException if count is less than 1 or if the
     *            spec is ill-formed
     * @throws NullPointerException if spec is null
     */
    public static List<MacAddress> getRandomMacs(String spec, int count) {
        return getRandomMacs(MacRange.valueOf(spec), count);
    }

    /** Returns a list of randomly generated MAC addresses.
     * Note that addresses may be repeated in the list.
     *
     * @param range a MAC address range from which to draw the random samples
     * @param count the number of MAC addresses to return in the list
     * @return a list of randomly generated MAC addresses
     * @throws IllegalArgumentException if count is less than 1
     */
    public static List<MacAddress> getRandomMacs(MacRange range, int count) {
        if (count < 1)
            throw new IllegalArgumentException(COUNT_GE_ONE);

        List<MacAddress> macs = new ArrayList<MacAddress>(count);
        for (int i=0; i<count; i++)
            macs.add(range.random());
        return macs;
    }
}
