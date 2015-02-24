/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.MaskMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.Ipv6ExthdrFlags;

/**
 * match related tools
 */
public abstract class MatchConvertorUtil {

    private static final String PREFIX_SEPARATOR = "/";
    private static final int MAX_SHIFT_COUNT = 32;
    private static final int FULL_IPV4_MASK = 32;

    /**
     * Postion of bits counted from right (from the less meaning bit)
     */
    static final int NONEXT_BIT_POSITION = 0;
    static final int ESP_BIT_POSITION = 1;
    static final int AUTH_BIT_POSITION = 2;
    static final int DEST_BIT_POSITION = 3;
    static final int FRAG_BIT_POSITION = 4;
    static final int ROUTER_BIT_POSITION = 5;
    static final int HOP_BIT_POSITION = 6;
    static final int UNREP_BIT_POSITION = 7;
    static final int UNSEQ_BIT_POSITION = 8;

    private static final int MAX_IP_V6_MASK = 128;
    private static final int NUMBER_OF_SUBARRAYS = 4;
    private static final int SUBARRAY_LENGTH = 4;
    private static final int BITS_IN_SUBARRAY = 32;

    private MatchConvertorUtil() {
        // hiding implicit constructor
    }

    /**
     * @param maskMatchEntry
     * @return subnetwork suffix in form of "/"+&lt;mask value {0..32}&gt;
     */
    public static String getIpv4Mask(MaskMatchEntry maskMatchEntry) {
        int receivedMask = ByteBuffer.wrap(maskMatchEntry.getMask()).getInt();
        int shiftCount = 0;
        
        if (receivedMask == 0) {
            shiftCount = MAX_SHIFT_COUNT;
        } else {
            while (receivedMask != 0xffffffff) {
                receivedMask = receivedMask >> 1;
                shiftCount++;
                if (shiftCount >= MAX_SHIFT_COUNT) {
                    throw new IllegalArgumentException("given mask is invalid: "+Arrays.toString(maskMatchEntry.getMask()));
                }
            }
        }
        return PREFIX_SEPARATOR + (FULL_IPV4_MASK - shiftCount);
    }

    /**
     * @param pField
     * @return integer containing lower 9 bits filled with corresponding flags
     */
    public static Integer ipv6ExthdrFlagsToInt(Ipv6ExthdrFlags pField) {
        Integer bitmap = 0;
        bitmap |= pField.isNonext() ? (1 << NONEXT_BIT_POSITION) : 0;
        bitmap |= pField.isEsp() ? (1 << ESP_BIT_POSITION) : 0;
        bitmap |= pField.isAuth() ? (1 << AUTH_BIT_POSITION) : 0;
        bitmap |= pField.isDest() ? (1 << DEST_BIT_POSITION) : 0;
        bitmap |= pField.isFrag() ? (1 << FRAG_BIT_POSITION ) : 0;
        bitmap |= pField.isRouter() ? (1 << ROUTER_BIT_POSITION) : 0;
        bitmap |= pField.isHop() ? (1 << HOP_BIT_POSITION) : 0;
        bitmap |= pField.isUnrep() ? (1 << UNREP_BIT_POSITION) : 0;
        bitmap |= pField.isUnseq() ? (1 << UNSEQ_BIT_POSITION) : 0;
        return bitmap;
    }

    public static int ipv6NetmaskArrayToCIDRValue(byte[] rawMask) {

        /*
         * Openflow Spec : 1.3.2+
         * An all-one-bits oxm_mask is equivalent to specifying 0 for oxm_hasmask and omitting oxm_mask.
         * So when user specify 128 as a mask, switch omit that mask and we get null as a mask in flow
         * statistics response.
         */

        int maskValue = MAX_IP_V6_MASK;

        if (rawMask != null) {
            maskValue = 0;
            for (int subArrayCounter = 0; subArrayCounter < NUMBER_OF_SUBARRAYS; subArrayCounter++) {
                int subarrayStartIndex = subArrayCounter * SUBARRAY_LENGTH;

                byte[] subArray = Arrays.copyOfRange(rawMask, subarrayStartIndex, subarrayStartIndex + SUBARRAY_LENGTH);

                int receivedMask = ByteBuffer.wrap(subArray).getInt();

                int shiftCount = 0;

                if (receivedMask == 0) {
                    break;
                }

                while (receivedMask != 0xffffffff) {
                    receivedMask = receivedMask >> 1;
                    shiftCount++;
                }

                maskValue = maskValue + (BITS_IN_SUBARRAY - shiftCount);
                if (shiftCount != 0) {
                    break;
                }
            }
        }
        return maskValue;
    }

}
