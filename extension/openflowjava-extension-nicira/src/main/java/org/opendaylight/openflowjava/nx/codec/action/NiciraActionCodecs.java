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

    private NiciraActionCodecs() {
    }

    public static final RegMoveCodec REG_MOVE_CODEC = new RegMoveCodec();
    public static final RegLoadCodec REG_LOAD_CODEC = new RegLoadCodec();
    public static final OutputRegCodec OUTPUT_REG_CODEC = new OutputRegCodec();
    public static final ResubmitCodec RESUBMIT_CODEC = new ResubmitCodec();
    public static final MultipathCodec MULTIPATH_CODEC = new MultipathCodec();
    public static final PushNshCodec PUSH_NSH_CODEC = new PushNshCodec();
    public static final PopNshCodec POP_NSH_CODEC = new PopNshCodec();
    public static final ConntrackCodec CONNTRACK_CODEC = new ConntrackCodec();
    public static final LearnCodec LEARN_CODEC = new LearnCodec();
    public static final FinTimeoutCodec FIN_TIMEOUT_CODEC = new FinTimeoutCodec();
}
