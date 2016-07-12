/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data;

import java.math.BigInteger;

/**
 * Convertor data implementation containing Openflow version and datapath ID
 */
public class VersionDatapathIdConvertorData extends VersionConvertorData {
    private BigInteger datapathId;

    /**
     * Instantiates a new version datapath id convertor data.
     *
     * @param version the version
     */
    public VersionDatapathIdConvertorData(short version) {
        super(version);
    }

    /**
     * Gets datapath id.
     *
     * @return the datapath id
     */
    public BigInteger getDatapathId() {
        return datapathId;
    }

    /**
     * Sets datapath id.
     *
     * @param datapathId the datapath id
     */
    public void setDatapathId(BigInteger datapathId) {
        this.datapathId = datapathId;
    }
}
