/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.serialization.actions;

import static java.util.Objects.requireNonNullElse;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.impl.util.ActionConstants;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputAction;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;

public class OutputActionSerializer extends AbstractActionSerializer<OutputActionCase> {

    @Override
    public void serialize(final OutputActionCase action, final ByteBuf outBuffer) {
        super.serialize(action, outBuffer);
        final OutputAction outputAction = action.getOutputAction();
        Uint32 value = InventoryDataServiceUtil.portNumberfromNodeConnectorId(
                OpenflowVersion.OF13,
                outputAction.getOutputNodeConnector().getValue());
        if (value == null) {
            throw new IllegalArgumentException("Not a valid port number: "
                    + outputAction.getOutputNodeConnector().getValue());
        }
        outBuffer.writeInt(value.intValue());
        outBuffer.writeShort(requireNonNullElse(outputAction.getMaxLength(), Uint16.ZERO).toJava());
        outBuffer.writeZero(ActionConstants.OUTPUT_PADDING);
    }

    @Override
    protected int getLength() {
        return ActionConstants.LARGER_ACTION_LENGTH;
    }

    @Override
    protected int getType() {
        return ActionConstants.OUTPUT_CODE;
    }
}
