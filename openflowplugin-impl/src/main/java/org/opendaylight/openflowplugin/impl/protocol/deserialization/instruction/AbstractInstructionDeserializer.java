/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.deserialization.instruction;

import org.opendaylight.openflowjava.protocol.api.extensibility.HeaderDeserializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.Instruction;

import io.netty.buffer.ByteBuf;

public abstract class AbstractInstructionDeserializer implements OFDeserializer<Instruction>, HeaderDeserializer<Instruction> {

    /**
     * Skip first few bytes of instruction message because they are irrelevant
     * @param message Openflow buffered message
     **/
    protected static void processHeader(ByteBuf message) {
        message.skipBytes(2 * EncodeConstants.SIZE_OF_SHORT_IN_BYTES);
    }

}
