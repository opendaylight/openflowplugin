/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.nx.codec.action;

/**
 * Defines the Nicira action codecs.
 *
 * @author msunal
 */
public interface NiciraActionCodecs {
    RegMoveCodec REG_MOVE_CODEC = new RegMoveCodec();
    RegLoadCodec REG_LOAD_CODEC = new RegLoadCodec();
    RegLoad2Codec REG_LOAD2_CODEC = new RegLoad2Codec();
    OutputRegCodec OUTPUT_REG_CODEC = new OutputRegCodec();
    OutputReg2Codec OUTPUT_REG2_CODEC = new OutputReg2Codec();
    ResubmitCodec RESUBMIT_CODEC = new ResubmitCodec();
    MultipathCodec MULTIPATH_CODEC = new MultipathCodec();
    ConntrackCodec CONNTRACK_CODEC = new ConntrackCodec();
    CtClearCodec CT_CLEAR_CODEC = new CtClearCodec();
    LearnCodec LEARN_CODEC = new LearnCodec();
    FinTimeoutCodec FIN_TIMEOUT_CODEC = new FinTimeoutCodec();
    EncapCodec ENCAP_CODEC = new EncapCodec();
    DecapCodec DECAP_CODEC = new DecapCodec();
    DecNshTtlCodec DEC_NSH_TTL_CODEC = new DecNshTtlCodec();
}
