/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.translator;

import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.MatchConvertorImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemoved;

/**
 * translate {@link FlowRemoved} message to FlowRemoved notification (omit instructions)
 */
public class FlowRemovedV10Translator extends FlowRemovedTranslator {

    @Override
    protected MatchBuilder translateMatch(FlowRemoved flowRemoved, DeviceInfo deviceInfo) {
        return MatchConvertorImpl.fromOFMatchV10ToSALMatch(flowRemoved.getMatchV10(),
                deviceInfo.getDatapathId(), OpenflowVersion.OF10);
    }

    /**
     * Always returns zero because OF10 FLOW_REMOVED doesn't contain table ID.
     *
     * @param flowRemoved  FLOW_REMOVED message.
     * @return  Zero.
     */
    @Override
    protected Short translateTableId(FlowRemoved flowRemoved) {
        return Short.valueOf((short)0);
    }
}
