/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.flow.flowflag;

import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.flow.FlowConvertor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowModFlagsV10;

/**
 *
 */
public class FlowFlagsConvertorV10Impl implements FlowFlagConvertor<FlowModFlagsV10> {

    @Override
    public FlowModFlagsV10 convert(
            FlowModFlags source, ConvertorExecutor convertorExecutor) {

        FlowModFlagsV10 ofFlowModFlags;
        if (source != null) {
            ofFlowModFlags = new FlowModFlagsV10(
                    source.isCHECKOVERLAP(), FlowConvertor.DEFAULT_OFPFF_EMERGENCY, source.isSENDFLOWREM());
        } else {
            ofFlowModFlags = new FlowModFlagsV10(
                    FlowConvertor.DEFAULT_OFPFF_CHECK_OVERLAP,
                    FlowConvertor.DEFAULT_OFPFF_EMERGENCY,
                    FlowConvertor.DEFAULT_OFPFF_FLOW_REM);
        }

        return ofFlowModFlags;
    }
}
