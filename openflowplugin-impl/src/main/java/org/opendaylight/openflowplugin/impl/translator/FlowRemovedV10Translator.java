/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.translator;

import java.util.Optional;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionDatapathIdConvertorData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemoved;

/**
 * translate {@link FlowRemoved} message to FlowRemoved notification (omit instructions)
 */
public class FlowRemovedV10Translator extends FlowRemovedTranslator {

    public FlowRemovedV10Translator(ConvertorExecutor convertorExecutor) {
        super(convertorExecutor);
    }

    @Override
    protected MatchBuilder translateMatch(FlowRemoved flowRemoved, DeviceInfo deviceInfo) {
        final VersionDatapathIdConvertorData datapathIdConvertorData = new VersionDatapathIdConvertorData(deviceInfo.getVersion());
        datapathIdConvertorData.setDatapathId(deviceInfo.getDatapathId());

        final Optional<MatchBuilder> matchBuilderOptional = getConvertorExecutor().convert(
                flowRemoved.getMatchV10(),
                datapathIdConvertorData);

        return matchBuilderOptional.orElse(new MatchBuilder());
    }

    /**
     * Always returns zero because OF10 FLOW_REMOVED doesn't contain table ID.
     *
     * @param flowRemoved  FLOW_REMOVED message.
     * @return  Zero.
     */
    @Override
    protected Short translateTableId(FlowRemoved flowRemoved) {
        return (short) 0;
    }
}
