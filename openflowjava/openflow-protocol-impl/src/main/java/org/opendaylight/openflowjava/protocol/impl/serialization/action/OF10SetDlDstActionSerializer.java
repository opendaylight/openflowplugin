/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.serialization.action;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.impl.util.ActionConstants;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.IetfYangUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetDlDstCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;

/**
 * Serializes OF 1.0 SetDlDst actions.
 *
 * @author michal.polkorab
 */
public class OF10SetDlDstActionSerializer extends AbstractActionSerializer {
    public OF10SetDlDstActionSerializer() {
        super(ActionConstants.SET_DL_DST_CODE, ActionConstants.LARGER_ACTION_LENGTH);
    }

    @Override
    public void serialize(final Action action, final ByteBuf outBuffer) {
        super.serialize(action, outBuffer);
        outBuffer.writeBytes(IetfYangUtil.INSTANCE.macAddressBytes(((SetDlDstCase) action.getActionChoice())
                .getSetDlDstAction().getDlDstAddress()));
        outBuffer.writeZero(ActionConstants.PADDING_IN_DL_ADDRESS_ACTION);
    }
}
