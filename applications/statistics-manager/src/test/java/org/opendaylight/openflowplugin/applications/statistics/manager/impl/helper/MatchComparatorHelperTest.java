package org.opendaylight.openflowplugin.applications.statistics.manager.impl.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * test of {@link MatchComparatorHelper}
 */
public class MatchComparatorHelperTest {

    /**
     * mask for /32
     */
    private static final int DEFAULT_IPV4_MASK = 0xffffffff;

    /**
     * mask for /30
     */
    private static final int IPV4_30_MASK = 0xfffffffc;
    private static final int IP_ADDRESS = 0xC0A80101;

    /**
     * The test of conversion valid IP addres without mask to binary form.
     */
    @Test
    public void validIpWithoutMaskTest() {
        IntegerIpAddress intIp = MatchComparatorHelper.strIpToIntIp("192.168.1.1");
        assertEquals(IP_ADDRESS, intIp.getIp());
        assertEquals(DEFAULT_IPV4_MASK, intIp.getMask());
    }

    /**
     * The test of conversion of valid IP address with valid mask to binary form.
     */
    @Test
    public void validIpWithValidMaskTest() {
        IntegerIpAddress intIp = MatchComparatorHelper.strIpToIntIp("192.168.1.1/30");
        assertEquals(IP_ADDRESS, intIp.getIp());
        assertEquals(IPV4_30_MASK, intIp.getMask());
    }

    /**
     * The test of conversion of valid IP address invalid mask to binary form.
     */
    @Test
    public void validIpWithInvalidMaskTest() {
        try {
            MatchComparatorHelper.strIpToIntIp("192.168.1.1/40");
        } catch (IllegalStateException e) {
            assertEquals("Valid values for mask are from range 0 - 32. Value 40 is invalid.", e.getMessage());
            return;
        }
        fail("IllegalStateException was awaited (40 subnet is invalid)");
    }

    /**
     * The test of conversion invalid IP address with valid mask to binary form.
     */
    @Test
    public void invalidIpWithValidMaskTest() {
        try {
            MatchComparatorHelper.strIpToIntIp("257.168.1.1/25");
        } catch (IllegalArgumentException e) {
            assertEquals("'257.168.1.1' is not an IP string literal.", e.getMessage());
        }
    }
}
