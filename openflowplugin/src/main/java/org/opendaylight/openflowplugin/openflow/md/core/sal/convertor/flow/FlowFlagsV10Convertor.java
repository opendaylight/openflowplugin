/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.flow;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.Convertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionConvertorData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowModFlagsV10;

public class FlowFlagsV10Convertor extends Convertor<FlowModFlags, FlowModFlagsV10, VersionConvertorData> {
    private static final List<Class<?>> TYPES = Collections.singletonList(
            org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags.class);

    /**
     * Create default empty flow flags
     * Use this method, if result from converter is empty.
     */
    public static FlowModFlagsV10 defaultResult() {
        return new FlowModFlagsV10(
                FlowConvertor.DEFAULT_OFPFF_CHECK_OVERLAP,
                FlowConvertor.DEFAULT_OFPFF_EMERGENCY,
                FlowConvertor.DEFAULT_OFPFF_FLOW_REM);
    }

    @Override
    public Collection<Class<?>> getTypes() {
        return TYPES;
    }

    @Override
    public FlowModFlagsV10 convert(final FlowModFlags source, final VersionConvertorData data) {
        return new FlowModFlagsV10(
                source.getCHECKOVERLAP(), FlowConvertor.DEFAULT_OFPFF_EMERGENCY, source.getSENDFLOWREM());
    }
}
