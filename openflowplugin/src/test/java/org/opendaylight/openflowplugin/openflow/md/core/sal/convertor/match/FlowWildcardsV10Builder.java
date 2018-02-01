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

    public FlowWildcardsV10Builder setAll(boolean value) {
        dlDst = value;
        dlSrc = value;
        dlType = value;
        dlVlan = value;
        dlVlanPcp = value;
        inPort = value;
        nwProto = value;
        nwTos = value;
        tpDst = value;
        tpSrc = value;
        return this;
    }

    public FlowWildcardsV10Builder setDlDst(boolean value) {
        dlDst = value;
        return this;
    }

    public FlowWildcardsV10Builder setDlSrc(boolean value) {
        dlSrc = value;
        return this;
    }

    public FlowWildcardsV10Builder setDlType(boolean value) {
        dlType = value;
        return this;
    }

    public FlowWildcardsV10Builder setDlVlan(boolean value) {
        dlVlan = value;
        return this;
    }

    public FlowWildcardsV10Builder setDlVlanPcp(boolean value) {
        dlVlanPcp = value;
        return this;
    }

    public FlowWildcardsV10Builder setInPort(boolean value) {
        inPort = value;
        return this;
    }

    public FlowWildcardsV10Builder setNwProto(boolean value) {
        nwProto = value;
        return this;
    }

    public FlowWildcardsV10Builder setNwTos(boolean value) {
        nwTos = value;
        return this;
    }

    public FlowWildcardsV10Builder setTpDst(boolean value) {
        tpDst = value;
        return this;
    }

    public FlowWildcardsV10Builder setTpSrc(boolean value) {
        tpSrc = value;
        return this;
    }
}
