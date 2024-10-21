/*
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.md.util;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * List of Openflow versions supported by the plugin.
 *
 * <p>Note: If you add a version here, make sure to update
 *          {@code org.opendaylight.openflowplugin.openflow.md.util.OpenflowPortsUtil} as well.
 */
// FIXME: enum in api is not something what we would like to see in case it is evolving. On the other hand we have
//        static constants for well-known versions, so this is not *that* bad.
@NonNullByDefault
public enum OpenflowVersion {
    OF10(OFConstants.OFP_VERSION_1_0),
    OF13(OFConstants.OFP_VERSION_1_3),
    UNSUPPORTED(Uint8.ZERO);

    private final Uint8 version;

    OpenflowVersion(final Uint8 version) {
        this.version = requireNonNull(version);
    }

    @Deprecated
    public static OpenflowVersion get(final Uint8 version) {
        return ofVersion(version);
    }

    public static OpenflowVersion ofVersion(final Uint8 version) {
        if (OFConstants.OFP_VERSION_1_3.equals(version)) {
            return OF13;
        } else if (OFConstants.OFP_VERSION_1_0.equals(version)) {
            return OF10;
        } else {
            return UNSUPPORTED;
        }
    }

    /**
     * Getter.
     *
     * @return the version
     */
    public Uint8 getVersion() {
        return version;
    }
}
