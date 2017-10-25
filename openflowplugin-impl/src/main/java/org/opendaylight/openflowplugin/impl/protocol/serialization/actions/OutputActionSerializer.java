/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.actions;

import com.google.common.base.MoreObjects;
import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.impl.util.ActionConstants;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputAction;

public class OutputActionSerializer extends AbstractActionSerializer {

    @Override
    public void serialize(Action action, ByteBuf outBuffer) {
        super.serialize(action, outBuffer);
        final OutputAction outputAction = OutputActionCase.class.cast(action).getOutputAction();
        Long value = InventoryDataServiceUtil.portNumberfromNodeConnectorId(
                OpenflowVersion.OF13,
                outputAction.getOutputNodeConnector().getValue());
        if (value == null) {
            throw new IllegalArgumentException("Not a valid port number: "
                    + outputAction.getOutputNodeConnector().getValue());
        }
        outBuffer.writeInt(value.intValue());
        outBuffer.writeShort(MoreObjects.firstNonNull(outputAction.getMaxLength(), 0));
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
