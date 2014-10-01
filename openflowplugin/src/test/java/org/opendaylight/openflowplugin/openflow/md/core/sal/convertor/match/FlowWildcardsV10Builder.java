/*
 * Copyright (c) 2014 NEC Corporation and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match;

import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowWildcardsV10;

/**
 * Builder class for {@link FlowWildcardsV10}.
 */
public class FlowWildcardsV10Builder {
    private boolean  dlDst;
    private boolean  dlSrc;
    private boolean  dlType;
    private boolean  dlVlan;
    private boolean  dlVlanPcp;
    private boolean  inPort;
    private boolean  nwProto;
    private boolean  nwTos;
    private boolean  tpDst;
    private boolean  tpSrc;

    public FlowWildcardsV10 build() {
        return new FlowWildcardsV10(dlDst, dlSrc, dlType, dlVlan, dlVlanPcp,
                                    inPort, nwProto, nwTos, tpDst, tpSrc);
    }

    public FlowWildcardsV10Builder setAll(boolean b) {
        dlDst = b;
        dlSrc = b;
        dlType = b;
        dlVlan = b;
        dlVlanPcp = b;
        inPort = b;
        nwProto = b;
        nwTos = b;
        tpDst = b;
        tpSrc = b;
        return this;
    }

    public FlowWildcardsV10Builder setDlDst(boolean b) {
        dlDst = b;
        return this;
    }

    public FlowWildcardsV10Builder setDlSrc(boolean b) {
        dlSrc = b;
        return this;
    }

    public FlowWildcardsV10Builder setDlType(boolean b) {
        dlType = b;
        return this;
    }

    public FlowWildcardsV10Builder setDlVlan(boolean b) {
        dlVlan = b;
        return this;
    }

    public FlowWildcardsV10Builder setDlVlanPcp(boolean b) {
        dlVlanPcp = b;
        return this;
    }

    public FlowWildcardsV10Builder setInPort(boolean b) {
        inPort = b;
        return this;
    }

    public FlowWildcardsV10Builder setNwProto(boolean b) {
        nwProto = b;
        return this;
    }

    public FlowWildcardsV10Builder setNwTos(boolean b) {
        nwTos = b;
        return this;
    }

    public FlowWildcardsV10Builder setTpDst(boolean b) {
        tpDst = b;
        return this;
    }

    public FlowWildcardsV10Builder setTpSrc(boolean b) {
        tpSrc = b;
        return this;
    }
}
