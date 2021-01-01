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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowModFlags;

public class FlowFlagsConvertor extends Convertor<
        org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags,
        FlowModFlags,
        VersionConvertorData> {

    private static final List<Class<?>> TYPES = Collections.singletonList(
            org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags.class);

    /**
     * Create default empty flow flags
     * Use this method, if result from converter is empty.
     */
    public static FlowModFlags defaultResult() {
        return new FlowModFlags(
                FlowConvertor.DEFAULT_OFPFF_CHECK_OVERLAP,
                FlowConvertor.DEFAULT_OFPFF_NO_BYT_COUNTS,
                FlowConvertor.DEFAULT_OFPFF_NO_PKT_COUNTS,
                FlowConvertor.DEFAULT_OFPFF_RESET_COUNTS,
                FlowConvertor.DEFAULT_OFPFF_FLOW_REM);
    }

    @Override
    public Collection<Class<?>> getTypes() {
        return TYPES;
    }

    @Override
    public FlowModFlags convert(
            final org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags source,
            final VersionConvertorData data) {
        return new FlowModFlags(
                source.getCHECKOVERLAP(), source.getNOBYTCOUNTS(), source.getNOPKTCOUNTS(),
                source.getRESETCOUNTS(), source.getSENDFLOWREM());
    }
}
