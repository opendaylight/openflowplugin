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
import org.opendaylight.openflowplugin.api.openflow.protocol.converter.ConverterExecutor;
import org.opendaylight.openflowplugin.protocol.converter.data.VersionDatapathIdConverterData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemoved;

/**
 * translate {@link FlowRemoved} message to FlowRemoved notification (omit instructions)
 */
public class FlowRemovedV10Translator extends FlowRemovedTranslator {

    public FlowRemovedV10Translator(ConverterExecutor converterExecutor) {
        super(converterExecutor);
    }

    @Override
    protected MatchBuilder translateMatch(FlowRemoved flowRemoved, DeviceInfo deviceInfo) {
        final VersionDatapathIdConverterData datapathIdConverterData = new VersionDatapathIdConverterData(deviceInfo.getVersion());
        datapathIdConverterData.setDatapathId(deviceInfo.getDatapathId());

        final Optional<MatchBuilder> matchBuilderOptional = getConverterExecutor().convert(
                flowRemoved.getMatchV10(),
                datapathIdConverterData);

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
