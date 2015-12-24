/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.nx.codec.action;

/**
 * @author msunal
 *
 */
public class NiciraActionCodecs {

    public static final RegMoveCodec REG_MOVE_CODEC = new RegMoveCodec();
    public static final RegLoadCodec REG_LOAD_CODEC = new RegLoadCodec();
    public static final OutputRegCodec OUTPUT_REG_CODEC = new OutputRegCodec();
    public static final ResubmitCodec RESUBMIT_CODEC = new ResubmitCodec();
    public static final MultipathCodec MULTIPATH_CODEC = new MultipathCodec();
    public static final SetNspCodec SET_NSP_CODEC = new SetNspCodec();
    public static final SetNsiCodec SET_NSI_CODEC = new SetNsiCodec();
    public static final SetNshc1Codec SET_NSC1_CODEC = new SetNshc1Codec();
    public static final SetNshc2Codec SET_NSC2_CODEC = new SetNshc2Codec();
    public static final SetNshc3Codec SET_NSC3_CODEC = new SetNshc3Codec();
    public static final SetNshc4Codec SET_NSC4_CODEC = new SetNshc4Codec();
    public static final ConntrackCodec CONNTRACK_CODEC = new ConntrackCodec();
}
