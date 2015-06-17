/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.vendor.cisco.convertor.action;

import com.google.common.base.Preconditions;
import org.opendaylight.openflowjava.cof.api.CiscoConstants;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.ActionChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ExperimenterId;

/**
 * vendor action factory
 */
public class ActionUtil {


    /**
     * @param actionChoice
     * @return OFJava action with augmentation containing action subtype and experimenter type
     */
    public static Action createCiscoAction(ActionChoice actionChoice) {
        ActionBuilder actionBuilder = new ActionBuilder();
        actionBuilder.setActionChoice(actionChoice);
        actionBuilder.setExperimenterId(getExperimenterId());
        return actionBuilder.build();
    }

    protected final static ExperimenterId getExperimenterId() {
        return new ExperimenterId(CiscoConstants.COF_VENDOR_ID);
    }

    /**
     * TODO: expose at {@link ByteBufUtils}
     *
     * @param ipv4Raw
     * @return string suitable for {@link Ipv4Address}
     */
    public static String bytesToIpv4Address(byte[] ipv4Raw) {
        Preconditions.checkArgument(ipv4Raw.length == EncodeConstants.GROUPS_IN_IPV4_ADDRESS, "incorrect size of byte[] for IPV4: " + ipv4Raw.length);
        final StringBuilder sb = new StringBuilder(EncodeConstants.GROUPS_IN_IPV4_ADDRESS * 4 - 1);

        sb.append(ipv4Raw[0]);
        for (int i = 1; i < EncodeConstants.GROUPS_IN_IPV4_ADDRESS; i++) {
            sb.append('.').append(ipv4Raw[i]);
        }

        return sb.toString();
    }

    /**
     * TODO: expose at {@link ByteBufUtils}
     *
     * @param ipv6Raw
     * @return string suitable for {@link Ipv4Address}
     */
    public static String bytesToIpv6Address(byte[] ipv6Raw) {
        Preconditions.checkArgument(ipv6Raw.length == EncodeConstants.SIZE_OF_IPV6_ADDRESS_IN_BYTES, "incorrect size of byte[] for IPV6: " + ipv6Raw.length);
        final StringBuilder sb = new StringBuilder(EncodeConstants.GROUPS_IN_IPV6_ADDRESS * 5 - 1);

        appendHexUnsignedShort(sb, ipv6Raw[0] << 8 | ipv6Raw[1]);
        for (int i = 2; i < EncodeConstants.SIZE_OF_IPV6_ADDRESS_IN_BYTES; i += 2) {
            sb.append(':');
            appendHexUnsignedShort(sb, ipv6Raw[i] << 8 | ipv6Raw[i + 1]);
        }

        return sb.toString();
    }

    /**
     * TODO: expose at {@link ByteBufUtils}
     */
    private static final char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();

    /**
     * TODO: expose at {@link ByteBufUtils}
     */
    private static void appendHexUnsignedShort(final StringBuilder sb, final int val) {
        sb.append(ActionUtil.HEX_CHARS[(val >>> 12) & 15]);
        sb.append(ActionUtil.HEX_CHARS[(val >>> 8) & 15]);
        sb.append(ActionUtil.HEX_CHARS[(val >>> 4) & 15]);
        sb.append(ActionUtil.HEX_CHARS[val & 15]);
    }
}
