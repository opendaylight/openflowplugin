/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.data;

import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionDatapathIdConvertorData;

/**
 * Convertor data used in {@link org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.ActionConvertor}
 * containing Openflow version, datapath ID and IP protocol from flow
 */
public class ActionConvertorData extends VersionDatapathIdConvertorData {
    private Short ipProtocol;

    /**
     * Instantiates a new Action convertor data.
     *
     * @param version the version
     */
    public ActionConvertorData(short version) {
        super(version);
    }

    /**
     * Gets ip protocol.
     *
     * @return the ip protocol
     */
    public Short getIpProtocol() {
        return ipProtocol;
    }

    /**
     * Sets ip protocol.
     *
     * @param ipProtocol the ip protocol
     */
    public void setIpProtocol(Short ipProtocol) {
        this.ipProtocol = ipProtocol;
    }
}
