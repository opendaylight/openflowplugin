/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api;

import java.math.BigInteger;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;

/**
 * OFP related constants.
 */
public final class OFConstants {

    private OFConstants() {
        throw new UnsupportedOperationException("OF plugin Constants holder class");
    }

    /** enum ofp_port_no, reserved port: process with normal L2/L3 switching.  */
    public static final short OFPP_NORMAL = ((short)0xfffa);
    /** enum ofp_port_no, reserved port: all physical ports except input port.  */
    public static final short OFPP_ALL  = ((short)0xfffc);
    /** enum ofp_port_no, reserved port: local openflow port.  */
    public static final short OFPP_LOCAL = ((short)0xfffe);

    /** openflow protocol 1.0 - version identifier. */
    public static final short OFP_VERSION_1_0 = 0x01;
    /** openflow protocol 1.3 - version identifier. */
    public static final short OFP_VERSION_1_3 = 0x04;

    public static final String OF_URI_PREFIX = "openflow:";

    /** enum ofp_table: Table numbering, wildcard table used for table config, flow stats and flow deletes. */
    public static final Short OFPTT_ALL = 0xff;
    public static final Long ANY = 0xffffffffL;
    /** Wildcard port used only for flow mod (delete) and flow stats requests. Selects
     *  all flows regardless of output port (including flows with no output port).*/
    public static final Long OFPP_ANY = ANY;
    /** enum ofp_group: For OFPFC_DELETE* commands, require matching entries to include this as an
     *  output group. A value of OFPG_ANY indicates no restriction. */
    public static final Long OFPG_ANY = ANY;
    /** enum ofp_group: Represents all groups for group delete commands. */
    public static final Long OFPG_ALL = 0xfffffffcL;
    /** Refers to all queues conﬁgured at the speciﬁed port. */
    public static final Long OFPQ_ALL = ANY;
    /** Represents all meters for stat requests commands. */
    public static final Long OFPM_ALL = ANY;
    /** Default cookie. */
    public static final BigInteger DEFAULT_COOKIE = BigInteger.ZERO;
    public static final BigInteger DEFAULT_COOKIE_MASK = BigInteger.ZERO;
    public static final FlowCookie DEFAULT_FLOW_COOKIE = new FlowCookie(DEFAULT_COOKIE);
    public static final Integer DEFAULT_FLOW_PRIORITY = 0x8000;
    /** Empty flow match. */
    public static final Match EMPTY_MATCH = new MatchBuilder().build();

    /** indicates that no buffering should be applied and the whole packet is to be
     *  sent to the controller. */
    public static final Long OFP_NO_BUFFER = 0xffffffffL;
    /** enum ofp_controller_max_len: indicates that no buffering should be applied and the whole packet is to be
     *  sent to the controller. */
    public static final Integer OFPCML_NO_BUFFER = 0xffff;

    public static final int MAC_ADDRESS_LENGTH = 6;
    public static final int SIZE_OF_LONG_IN_BYTES = 8;
    public static final int SIGNUM_UNSIGNED = 1;

    /** RpcError application tag. */
    public static final String APPLICATION_TAG = "OPENFLOW_PLUGIN";
    /** RpcError tag - timeout. */
    public static final String ERROR_TAG_TIMEOUT = "TIMOUT";

    /** Persistent ID of OpenFlowPlugin configuration file */
    public static final String CONFIG_FILE_ID = "org.opendaylight.openflowplugin";
}
