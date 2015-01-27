/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match;

import com.google.common.base.Preconditions;
import java.nio.ByteBuffer;
import java.util.Arrays;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.MaskMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.Ipv6ExthdrFlags;

/**
 * match related tools
 */
public abstract class MatchConvertorUtil {

    private static final String PREFIX_SEPARATOR = "/";
    private static final int MAC_ADDRESS_MASK_LENGTH = 6;

    /**
     * @param maskMatchEntry
     * @return subnetwork suffix in form of "/"+&lt;mask value {0..32}&gt;
     */
    public static String getIpv4Mask(MaskMatchEntry maskMatchEntry) {
        int receivedMask = ByteBuffer.wrap(maskMatchEntry.getMask()).getInt();
        int shiftCount = 0;
        
        if (receivedMask == 0) {
            shiftCount = 32;
        } else {
            while (receivedMask != 0xffffffff) {
                receivedMask = receivedMask >> 1;
                shiftCount++;
                if (shiftCount >= 32) {
                    throw new IllegalArgumentException("given mask is invalid: "+Arrays.toString(maskMatchEntry.getMask()));
                }
            }
        }
        return PREFIX_SEPARATOR + (32 - shiftCount);
    }

    /**
     * @param pField
     * @return integer containing lower 9 bits filled with corresponding flags
     */
    public static Integer ipv6ExthdrFlagsToInt(Ipv6ExthdrFlags pField) {
        Integer bitmap = 0;
        bitmap |= pField.isNonext() ? (1 << 0) : 0;
        bitmap |= pField.isEsp() ? (1 << 1) : 0;
        bitmap |= pField.isAuth() ? (1 << 2) : 0;
        bitmap |= pField.isDest() ? (1 << 3) : 0;
        bitmap |= pField.isFrag() ? (1 << 4) : 0;
        bitmap |= pField.isRouter() ? (1 << 5) : 0;
        bitmap |= pField.isHop() ? (1 << 6) : 0;
        bitmap |= pField.isUnrep() ? (1 << 7) : 0;
        bitmap |= pField.isUnseq() ? (1 << 8) : 0;
        return bitmap;
    }

    public static int ipv6NetmaskArrayToCIDRValue(byte[] rawMask) {

        /*
         * Openflow Spec : 1.3.2+
         * An all-one-bits oxm_mask is equivalent to specifying 0 for oxm_hasmask and omitting oxm_mask.
         * So when user specify 128 as a mask, switch omit that mask and we get null as a mask in flow
         * statistics response.
         */

        int maskValue = 128;

        if (rawMask != null) {
            maskValue = 0;
            for (int subArrayCounter = 0; subArrayCounter < 4; subArrayCounter++) {
                int copyFrom = subArrayCounter * 4;

                byte[] subArray = Arrays.copyOfRange(rawMask, copyFrom, copyFrom + 4);

                int receivedMask = ByteBuffer.wrap(subArray).getInt();

                int shiftCount = 0;

                if (receivedMask == 0) {
                    break;
                }

                while (receivedMask != 0xffffffff) {
                    receivedMask = receivedMask >> 1;
                    shiftCount++;
                }

                maskValue = maskValue + (32 - shiftCount);
                if (shiftCount != 0) {
                    break;
                }
            }
        }
        return maskValue;
    }

    public static MacAddress macAddressMaskToString(final byte[] macAddressMask) {
        Preconditions.checkArgument(macAddressMask.length == MAC_ADDRESS_MASK_LENGTH, "Illegal length of MAC avfdddgsyddress mask.");
        final StringBuilder resultMask = new StringBuilder();
        for (byte maskOctet : macAddressMask) {
            resultMask.append(String.format("%02X:", maskOctet));
        }
        resultMask.setLength(resultMask.length()-1);
        return new MacAddress(resultMask.toString().toLowerCase());
    }

}
