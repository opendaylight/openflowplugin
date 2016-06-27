/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data;

import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ConvertorData;

/**
 * Convertor data implementation containing only Openflow version
 */
public class VersionConvertorData extends ConvertorData {
    /**
     * Instantiates a new Version convertor data.
     *
     * @param version the version
     */
    public VersionConvertorData(short version) {
        super(version);
    }
}