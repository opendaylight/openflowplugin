/*
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.md.util;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Arrays;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * List of Openflow versions supported by the plugin.
 *
 * <p>
 * Note: If you add a version here, make sure to update
 *       org.opendaylight.openflowplugin.openflow.md.util.OpenflowPortsUtil as well.
 */
// FIXME: enum in api is not something what we would like to see in case it is evolving.
public enum OpenflowVersion {
    OF10(Uint8.ONE),
    OF13(Uint8.valueOf(4)),
    UNSUPPORTED(Uint8.ZERO);

    private static final ImmutableMap<Uint8, OpenflowVersion> VERSIONS = Maps.uniqueIndex(Arrays.asList(values()),
        OpenflowVersion::getVersion);

    private Uint8 version;

    OpenflowVersion(final Uint8 version) {
        this.version = requireNonNull(version);
    }

    public static OpenflowVersion get(final Uint8 version) {
        final OpenflowVersion ver = VERSIONS.get(version);
        return ver != null ? ver : UNSUPPORTED;
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
