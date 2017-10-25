/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.util;

/**
 * @author michal.polkorab
 *
 */
public final class ActionConstants {

    /** Openflow v1.0 and v1.3 OFPAT_OUTPUT code */
    public static final byte OUTPUT_CODE = 0;
    /** Openflow v1.0 OFPAT_SET_VLAN_VID code */
    public static final byte SET_VLAN_VID_CODE = 1;
    /** Openflow v1.0 OFPAT_SET_VLAN_PCP code */
    public static final byte SET_VLAN_PCP_CODE = 2;
    /** Openflow v1.0 OFPAT_STRIP_VLAN code */
    public static final byte STRIP_VLAN_CODE = 3;
    /** Openflow v1.0 OFPAT_SET_DL_SRC code */
    public static final byte SET_DL_SRC_CODE = 4;
    /** Openflow v1.0 OFPAT_SET_DL_DST code */
    public static final byte SET_DL_DST_CODE = 5;
    /** Openflow v1.0 OFPAT_SET_NW_SRC code */
    public static final byte SET_NW_SRC_CODE = 6;
    /** Openflow v1.0 OFPAT_SET_NW_DST code */
    public static final byte SET_NW_DST_CODE = 7;
    /** Openflow v1.0 OFPAT_SET_NW_TOS code */
    public static final byte SET_NW_TOS_CODE = 8;
    /** Openflow v1.0 OFPAT_SET_TP_SRC code */
    public static final byte SET_TP_SRC_CODE = 9;
    /** Openflow v1.0 OFPAT_SET_TP_DST code */
    public static final byte SET_TP_DST_CODE = 10;
    /** Openflow v1.0 OFPAT_ENQUEUE code */
    public static final byte ENQUEUE_CODE = 11;
    /** Openflow v1.3 OFPAT_COPY_TTL_OUT code */
    public static final byte COPY_TTL_OUT_CODE = 11;
    /** Openflow v1.3 OFPAT_COPY_TTL_IN code */
    public static final byte COPY_TTL_IN_CODE = 12;
    /** Openflow v1.3 OFPAT_SET_MPLS_TTL code */
    public static final byte SET_MPLS_TTL_CODE = 15;
    /** Openflow v1.3 OFPAT_DEC_MPLS_TTL code */
    public static final byte DEC_MPLS_TTL_CODE = 16;
    /** Openflow v1.3 OFPAT_PUSH_VLAN code */
    public static final byte PUSH_VLAN_CODE = 17;
    /** Openflow v1.3 OFPAT_POP_VLAN code */
    public static final byte POP_VLAN_CODE = 18;
    /** Openflow v1.3 OFPAT_PUSH_MPLS code */
    public static final byte PUSH_MPLS_CODE = 19;
    /** Openflow v1.3 OFPAT_POP_MPLS code */
    public static final byte POP_MPLS_CODE = 20;
    /** Openflow v1.3 OFPAT_SET_QUEUE code */
    public static final byte SET_QUEUE_CODE = 21;
    /** Openflow v1.3 OFPAT_GROUP code */
    public static final byte GROUP_CODE = 22;
    /** Openflow v1.3 OFPAT_SET_NW_TTL code */
    public static final byte SET_NW_TTL_CODE = 23;
    /** Openflow v1.3 OFPAT_DEC_NW_TTL code */
    public static final byte DEC_NW_TTL_CODE = 24;
    /** Openflow v1.3 OFPAT_SET_FIELD code */
    public static final int SET_FIELD_CODE = 25;
    /** Openflow v1.3 OFPAT_PUSH_PBB code */
    public static final byte PUSH_PBB_CODE = 26;
    /** Openflow v1.3 OFPAT_POP_PBB code */
    public static final byte POP_PBB_CODE = 27;

    /** Padding in OFPAT_OUTPUT (OF v1.3) */
    public static final byte OUTPUT_PADDING = 6;
    /** Padding in OFPAT_SET_VLAN_VID (OF v1.3) */
    public static final byte PADDING_IN_SET_VLAN_VID_ACTION = 2;
    /** Padding in OFPAT_SET_VLAN_PCP (OF v1.3) */
    public static final byte PADDING_IN_SET_VLAN_PCP_ACTION = 3;
    /** Padding in OFPAT_SET_NW_TOS (OF v1.3) */
    public static final byte PADDING_IN_SET_NW_TOS_ACTION = 3;
    /** Padding in OFPAT_ENQUEUE (OF v1.3) */
    public static final int PADDING_IN_ENQUEUE_ACTION = 6;
    /** Padding in OFPAT_SET_MPLS_TTL (OF v1.3) */
    public static final byte SET_MPLS_TTL_PADDING = 3;
    /** Padding in OFPAT_SET_NW_TTL (OF v1.3) */
    public static final byte SET_NW_TTL_PADDING = 3;
    /** Padding in OFPAT_SET_DL_SRC and OFPAT_SET_DL_DST (OF v1.3) */
    public static final byte PADDING_IN_DL_ADDRESS_ACTION = 6;
    /** Padding in OFPAT_SET_TP_SRC and OFPAT_SET_TP_DST (OF v1.3) */
    public static final byte PADDING_IN_TP_PORT_ACTION = 2;
    /** Padding in action header (OF v1.3) */
    public static final byte PADDING_IN_ACTION_HEADER = 4;
    /** Padding in OFPAT_PUSH_VLAN, OFPAT_PUSH_MPLS, OFPAT_POP_MPLS
     *  and OFPAT_PUSH_PBB (OF v1.3) */
    public static final byte ETHERTYPE_ACTION_PADDING = 2;

    /** Most common action length */
    public static final byte GENERAL_ACTION_LENGTH = 8;
    /** Action larger than GENERAL_ACTION_LENGTH - currently
     *  only 16 bytes long actions for both OF v1.0 and v1.3*/
    public static final byte LARGER_ACTION_LENGTH = 16;
    /** Action header size */
    public static final byte ACTION_IDS_LENGTH = 4;

    private ActionConstants() {
        throw new UnsupportedOperationException("Utility class shouldn't be instantiated");
    }
}
