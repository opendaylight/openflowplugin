/*
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.md.util;

/**
 * List of Openflow versions supported by the plugin.
 * Note: If you add a version here,
 * make sure to update org.opendaylight.openflowplugin.openflow.md.util.OpenflowPortsUtil as well.
 * @deprecated enum in api is not something what we would like to see in case it is evolving.
 */
public enum OpenflowVersion {

    OF10((short)0x01),
    OF13((short)0x04),
    UNSUPPORTED((short)0x00);


    private short version;

    OpenflowVersion(final short version) {
        this.version = version;
    }

    public static OpenflowVersion get(final Short version) {
        for (final OpenflowVersion ofv : OpenflowVersion.values()) {
            if (ofv.version == version) {
                return ofv;
            }
        }
        return UNSUPPORTED;
    }

    /**
     * Getter.
     * @return the version
     */
    public short getVersion() {
        return version;
    }

}
