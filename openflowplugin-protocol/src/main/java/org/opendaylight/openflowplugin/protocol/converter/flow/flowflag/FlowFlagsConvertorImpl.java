/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.protocol.converter.flow.flowflag;

import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.protocol.converter.ConverterExecutor;
import org.opendaylight.openflowplugin.extension.api.core.extension.ExtensionConverterProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowModFlags;

/**
 *
 */
public class FlowFlagsConvertorImpl implements FlowFlagConvertor<FlowModFlags> {

    @Override
    public FlowModFlags convert(
            org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags source,
            ConverterExecutor converterExecutor, ExtensionConverterProvider extensionConverterProvider) {

        FlowModFlags ofFlowModFlags;
        if (source != null) {
            ofFlowModFlags = new FlowModFlags(
                    source.isCHECKOVERLAP(), source.isNOBYTCOUNTS(), source.isNOPKTCOUNTS(),
                    source.isRESETCOUNTS(), source.isSENDFLOWREM());
        } else {
            ofFlowModFlags = new FlowModFlags(
                    OFConstants.DEFAULT_OFPFF_CHECK_OVERLAP,
                    OFConstants.DEFAULT_OFPFF_NO_BYT_COUNTS,
                    OFConstants.DEFAULT_OFPFF_NO_PKT_COUNTS,
                    OFConstants.DEFAULT_OFPFF_RESET_COUNTS,
                    OFConstants.DEFAULT_OFPFF_FLOW_REM);
        }

        return ofFlowModFlags;
    }
}
