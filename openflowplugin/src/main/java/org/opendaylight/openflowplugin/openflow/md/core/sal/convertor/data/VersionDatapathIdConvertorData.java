/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data;

import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Convertor data implementation containing Openflow version and datapath ID.
 */
public class VersionDatapathIdConvertorData extends VersionConvertorData {
    private Uint64 datapathId;

    /**
     * Instantiates a new version datapath id convertor data.
     *
     * @param version the version
     */
    public VersionDatapathIdConvertorData(final Uint8 version) {
        super(version);
    }

    /**
     * Gets datapath id.
     *
     * @return the datapath id
     */
    public Uint64 getDatapathId() {
        return datapathId;
    }

    /**
     * Sets datapath id.
     *
     * @param datapathId the datapath id
     */
    public void setDatapathId(final Uint64 datapathId) {
        this.datapathId = datapathId;
    }
}
