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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowModFlags;

/**
 *
 */
public class FlowFlagsConvertorImpl implements FlowFlagConvertor<FlowModFlags> {

    @Override
    public FlowModFlags convert(
            org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags source, ConvertorExecutor convertorExecutor) {

        FlowModFlags ofFlowModFlags;
        if (source != null) {
            ofFlowModFlags = new FlowModFlags(
                    source.isCHECKOVERLAP(), source.isNOBYTCOUNTS(), source.isNOPKTCOUNTS(),
                    source.isRESETCOUNTS(), source.isSENDFLOWREM());
        } else {
            ofFlowModFlags = new FlowModFlags(
                    FlowConvertor.DEFAULT_OFPFF_CHECK_OVERLAP,
                    FlowConvertor.DEFAULT_OFPFF_NO_BYT_COUNTS,
                    FlowConvertor.DEFAULT_OFPFF_NO_PKT_COUNTS,
                    FlowConvertor.DEFAULT_OFPFF_RESET_COUNTS,
                    FlowConvertor.DEFAULT_OFPFF_FLOW_REM);
        }

        return ofFlowModFlags;
    }
}
