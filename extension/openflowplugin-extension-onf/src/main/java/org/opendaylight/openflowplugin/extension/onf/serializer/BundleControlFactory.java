/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.extension.onf.serializer;

import io.netty.buffer.ByteBuf;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.experimenter.input.experimenter.data.of.choice.BundleControl;

/**
 * Translates BundleControl messages (OpenFlow v1.3 extension #230).
 */
public class BundleControlFactory extends AbstractBundleMessageFactory<BundleControl> {

    @Override
    public void serialize(BundleControl input, ByteBuf outBuffer) {
        outBuffer.writeInt(input.getBundleId().getValue().intValue());
        outBuffer.writeShort(input.getType().getIntValue());
        writeBundleFlags(input.getFlags(), outBuffer);
        if (input.getBundleProperty() != null) {
            writeBundleProperties(input.getBundleProperty(), outBuffer);
        }
    }

}
