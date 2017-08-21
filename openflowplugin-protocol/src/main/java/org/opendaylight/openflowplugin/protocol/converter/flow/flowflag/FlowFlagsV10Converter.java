/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.protocol.converter.flow.flowflag;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.protocol.converter.Converter;
import org.opendaylight.openflowplugin.protocol.converter.data.VersionConverterData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowModFlagsV10;

/**
 *
 */
public class FlowFlagsV10Converter extends Converter<FlowModFlags, FlowModFlagsV10, VersionConverterData> {
    private static final List<Class<?>> TYPES = Collections.singletonList(
            org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags.class);
    /**
     * Create default empty flow flags
     * Use this method, if result from converter is empty.
     */
    public static FlowModFlagsV10 defaultResult() {
        return new FlowModFlagsV10(
                OFConstants.DEFAULT_OFPFF_CHECK_OVERLAP,
                OFConstants.DEFAULT_OFPFF_EMERGENCY,
                OFConstants.DEFAULT_OFPFF_FLOW_REM);
    }

    @Override
    public Collection<Class<?>> getTypes() {
        return TYPES;
    }

    @Override
    public FlowModFlagsV10 convert(final FlowModFlags source, final VersionConverterData data) {
        return new FlowModFlagsV10(
                source.isCHECKOVERLAP(), OFConstants.DEFAULT_OFPFF_EMERGENCY, source.isSENDFLOWREM());
    }
}
