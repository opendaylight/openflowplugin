/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.nx.codec.action;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.nx.api.NiciraConstants;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ExperimenterId;

/**
 * @author msunal
 */
public abstract class AbstractActionCodec implements OFSerializer<Action>, OFDeserializer<Action> {

    protected static final void serializeHeader(final int msgLength, final int subtype, final ByteBuf outBuffer) {
        outBuffer.writeShort(EncodeConstants.EXPERIMENTER_VALUE);
        writeMsgLengthVendorIdSubtypeToBuffer(msgLength, subtype, outBuffer);
    }

    private static final void writeMsgLengthVendorIdSubtypeToBuffer(final int msgLength, final int subtype, final ByteBuf outBuffer) {
        outBuffer.writeShort(msgLength);
        outBuffer.writeInt(NiciraConstants.NX_VENDOR_ID.intValue());
        outBuffer.writeShort(subtype);
    }

    protected static final ActionBuilder deserializeHeader(final ByteBuf message) {
        // size of experimenter type
        message.skipBytes(EncodeConstants.SIZE_OF_SHORT_IN_BYTES);
        // size of length
        message.skipBytes(EncodeConstants.SIZE_OF_SHORT_IN_BYTES);
        // vendor id
        message.skipBytes(EncodeConstants.SIZE_OF_INT_IN_BYTES);
        // subtype
        message.skipBytes(EncodeConstants.SIZE_OF_SHORT_IN_BYTES);
        ActionBuilder actionBuilder = new ActionBuilder();
        actionBuilder.setExperimenterId(getExperimenterId());
        return actionBuilder;
    }

    protected static final ExperimenterId getExperimenterId(){
        return new ExperimenterId(NiciraConstants.NX_VENDOR_ID);
    }


}
