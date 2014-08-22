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
 * Useful utility methods for manipulating {@link IpAddress IP addresses}.
 *
 * @author Simon Hunt
 */
public final class IpUtils extends AbstractUtils {

    // no instantiation
    private IpUtils() { }

    /** Returns an IPv4 address where all the bytes are set to the specified
     * seed value.
     * For example:
     * <pre>
     * IpAddress ip = IpUtils.getRepeatedByteIpAddressV4(5);
     * </pre>
     * will return the IP address {@code 5.5.5.5}
     *
     * @param seed the seed value
     * @return the IPv4 address
     * @throws IllegalArgumentException if seed is not in the range 0..255
     */
    public static IpAddress getRepeatedByteIpAddressV4(int seed) {
        return IpAddress.valueOf(getRepeatedByteArray(seed, 4));
    }

    /** Returns an IPv6 address where all the bytes are set to the specified
     * seed value.
     * For example:
     * <pre>
     * IpAddress ip = IpUtils.getRepeatedByteIpAddressV6(15);
     * </pre>
     * will return the IP address {@code F0F:F0F:F0F:F0F:F0F:F0F:F0F:F0F}
     *
     * @param seed the seed value
     * @return the IPv6 address
     * @throws IllegalArgumentException if seed is not in the range 0..255
     */
    public static IpAddress getRepeatedByteIpAddressV6(int seed) {
        return IpAddress.valueOf(getRepeatedByteArray(seed, 16));
    }

    /** Returns a list of randomly generated IP addresses.
     * See {@link IpRange} for a definition of the range specification.
     * Note that addresses may be repeated in the list.
     *
     * @param spec a string defining the set of IP addresses from which to
     *             draw the random samples
     * @param count the number of IP addresses to return in the list
     * @return a list of randomly generated IP addresses
     * @throws IllegalArgumentException if count is less than 1 or if the
     *              spec is ill-formed
     * @throws NullPointerException if spec is null
     */
    public static List<IpAddress> getRandomIps(String spec, int count) {
        return getRandomIps(IpRange.valueOf(spec), count);
    }

    /** Returns a list of randomly generated IP addresses.
     * Note that addresses may be repeated in the list.
     *
     * @param range an IP address range from which to draw the random samples
     * @param count the number of IP addresses to return in the list
     * @return a list of randomly generated IP addresses
     * @throws IllegalArgumentException if count is less than 1
     */
    public static List<IpAddress> getRandomIps(IpRange range, int count) {
        if (count < 1)
            throw new IllegalArgumentException(COUNT_GE_ONE);

        List<IpAddress> ips = new ArrayList<IpAddress>(count);
        for (int i=0; i<count; i++)
            ips.add(range.random());
        return ips;
    }

}
