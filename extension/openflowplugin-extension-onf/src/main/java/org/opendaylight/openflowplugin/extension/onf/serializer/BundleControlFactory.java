/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.extension.onf.serializer;

import io.netty.buffer.ByteBuf;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.experimenter.input.experimenter.data.of.choice.BundleControlOnf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Translates BundleControl messages (OpenFlow v1.3 extension #230).
 */
public class BundleControlFactory extends AbstractBundleMessageFactory<BundleControlOnf> {

    private static final Logger LOG = LoggerFactory.getLogger(BundleControlFactory.class);

    @Override
    public void serialize(BundleControlOnf input, ByteBuf outBuffer) {
        outBuffer.writeInt(input.getOnfControlGroupingData().getBundleId().getValue().intValue());
        outBuffer.writeShort(input.getOnfControlGroupingData().getType().getIntValue());
        LOG.trace("The control bundle message {} with bundleId {}", input,
                input.getOnfControlGroupingData().getBundleId().getValue().intValue());
        writeBundleFlags(input.getOnfControlGroupingData().getFlags(), outBuffer);
        if (input.getOnfControlGroupingData().getBundleProperty() != null) {
            writeBundleProperties(input.getOnfControlGroupingData().getBundleProperty(), outBuffer);
        }
    }

}
