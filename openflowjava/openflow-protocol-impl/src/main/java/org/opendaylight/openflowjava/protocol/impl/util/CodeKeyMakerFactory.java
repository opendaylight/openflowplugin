/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.util;

import io.netty.buffer.ByteBuf;

import org.opendaylight.openflowjava.protocol.api.keys.ActionDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.ExperimenterActionDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.ExperimenterInstructionDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.InstructionDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntryDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.MessageCodeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;

/**
 * @author michal.polkorab
 *
 */
public abstract class CodeKeyMakerFactory {

    private CodeKeyMakerFactory() {
        //not called
    }
    /**
     * @param version
     * @return
     */
    public static CodeKeyMaker createMatchEntriesKeyMaker(short version) {
        return new AbstractCodeKeyMaker(version) {
            @Override
            public MessageCodeKey make(ByteBuf input) {
                int oxmClass = input.getUnsignedShort(input.readerIndex());
                int oxmField = input.getUnsignedByte(input.readerIndex()
                        + EncodeConstants.SIZE_OF_SHORT_IN_BYTES) >>> 1;
                MatchEntryDeserializerKey key = new MatchEntryDeserializerKey(getVersion(),
                        oxmClass, oxmField);
                if (oxmClass == EncodeConstants.EXPERIMENTER_VALUE) {
                    long expId = input.getUnsignedInt(input.readerIndex() + EncodeConstants.SIZE_OF_SHORT_IN_BYTES
                            + 2 * EncodeConstants.SIZE_OF_BYTE_IN_BYTES);
                    key.setExperimenterId(expId);
                    return key;
                }
                key.setExperimenterId(null);
                return key;
            }
        };
    }

    /**
     * @param version
     * @return
     */
    public static CodeKeyMaker createActionsKeyMaker(short version) {
        return new AbstractCodeKeyMaker(version) {
            @Override
            public MessageCodeKey make(ByteBuf input) {
                int type = input.getUnsignedShort(input.readerIndex());
                if (type == EncodeConstants.EXPERIMENTER_VALUE) {
                    Long expId = input.getUnsignedInt(input.readerIndex()
                            + 2 * EncodeConstants.SIZE_OF_SHORT_IN_BYTES);
                    return new ExperimenterActionDeserializerKey(getVersion(), expId);
                }
                ActionDeserializerKey actionDeserializerKey = new ActionDeserializerKey(getVersion(), type, null);
                return actionDeserializerKey;
            }
        };
    }

    /**
     * @param version
     * @return
     */
    public static CodeKeyMaker createInstructionsKeyMaker(short version) {
        return new AbstractCodeKeyMaker(version) {
            @Override
            public MessageCodeKey make(ByteBuf input) {
                int type = input.getUnsignedShort(input.readerIndex());
                if (type == EncodeConstants.EXPERIMENTER_VALUE) {
                    Long expId = input.getUnsignedInt(input.readerIndex()
                            + 2 * EncodeConstants.SIZE_OF_SHORT_IN_BYTES);
                    return new ExperimenterInstructionDeserializerKey(getVersion(), expId);
                }
                return new InstructionDeserializerKey(getVersion(), type, null);
            }
        };
    }
}
