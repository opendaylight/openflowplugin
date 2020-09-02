/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * OFP related constants.
 */
public final class OFConstants {

    private OFConstants() {
        throw new UnsupportedOperationException("OF plugin Constants holder class");
    }

    /** enum ofp_port_no, reserved port: process with normal L2/L3 switching.  */
    public static final short OFPP_NORMAL = (short)0xfffa;
    /** enum ofp_port_no, reserved port: all physical ports except input port.  */
    public static final short OFPP_ALL  = (short)0xfffc;
    /** enum ofp_port_no, reserved port: local openflow port.  */
    public static final short OFPP_LOCAL = (short)0xfffe;

    /** openflow protocol 1.0 - version identifier. */
    public static final short OFP_VERSION_1_0 = 0x01;
    /** openflow protocol 1.3 - version identifier. */
    public static final short OFP_VERSION_1_3 = 0x04;

    public static final String OF_URI_PREFIX = "openflow:";

    /** enum ofp_table: Table numbering, wildcard table used for table config, flow stats and flow deletes. */
    public static final Uint8 OFPTT_ALL = Uint8.MAX_VALUE;
    public static final Uint32 ANY = Uint32.MAX_VALUE;
    /** Wildcard port used only for flow mod (delete) and flow stats requests. Selects
     *  all flows regardless of output port (including flows with no output port).*/
    public static final Uint32 OFPP_ANY = ANY;
    /** enum ofp_group: For OFPFC_DELETE* commands, require matching entries to include this as an
     *  output group. A value of OFPG_ANY indicates no restriction. */
    public static final Uint32 OFPG_ANY = ANY;
    /** enum ofp_group: Represents all groups for group delete commands. */
    public static final Uint32 OFPG_ALL = Uint32.valueOf(0xfffffffcL).intern();
    /** Refers to all queues conﬁgured at the speciﬁed port. */
    public static final Uint32 OFPQ_ALL = ANY;
    /** Represents all meters for stat requests commands. */
    public static final Uint32 OFPM_ALL = ANY;
    /** Default cookie. */
    public static final Uint64 DEFAULT_COOKIE = Uint64.ZERO;
    public static final Uint64 DEFAULT_COOKIE_MASK = Uint64.ZERO;
    public static final FlowCookie DEFAULT_FLOW_COOKIE = new FlowCookie(DEFAULT_COOKIE);
    public static final Uint16 DEFAULT_FLOW_PRIORITY = Uint16.valueOf(0x8000).intern();
    /** Empty flow match. */
    public static final Match EMPTY_MATCH = new MatchBuilder().build();

    /** indicates that no buffering should be applied and the whole packet is to be
     *  sent to the controller. */
    public static final Uint32 OFP_NO_BUFFER = Uint32.valueOf(0xffffffffL).intern();
    /** enum ofp_controller_max_len: indicates that no buffering should be applied and the whole packet is to be
     *  sent to the controller. */
    public static final Uint16 OFPCML_NO_BUFFER = Uint16.MAX_VALUE;

    public static final int MAC_ADDRESS_LENGTH = 6;
    public static final int SIZE_OF_LONG_IN_BYTES = 8;
    public static final int SIGNUM_UNSIGNED = 1;

    /** RpcError application tag. */
    public static final String APPLICATION_TAG = "OPENFLOW_PLUGIN";
    /** RpcError tag - timeout. */
    public static final String ERROR_TAG_TIMEOUT = "TIMOUT";

    /** Persistent ID of OpenFlowPlugin configuration file. */
    public static final String CONFIG_FILE_ID = "org.opendaylight.openflowplugin";

    /** supported version ordered by height (highest version is at the beginning). */
    public static final List<Short> VERSION_ORDER = ImmutableList
            .<Short>builder()
            .add((short) 0x04, (short) 0x01)
            .build();
}
