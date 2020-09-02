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
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Factory for creating CodeKeyMaker instances.
 *
 * @author michal.polkorab
 */
public final class CodeKeyMakerFactory {

    private CodeKeyMakerFactory() {
        //not called
    }

    public static CodeKeyMaker createMatchEntriesKeyMaker(final short version) {
        return new AbstractCodeKeyMaker(version) {
            @Override
            public MessageCodeKey make(final ByteBuf input) {
                int oxmClass = input.getUnsignedShort(input.readerIndex());
                int oxmField = input.getUnsignedByte(input.readerIndex()
                        + Short.BYTES) >>> 1;
                MatchEntryDeserializerKey key = new MatchEntryDeserializerKey(getVersion(),
                        oxmClass, oxmField);
                if (oxmClass == EncodeConstants.EXPERIMENTER_VALUE) {
                    long expId = input.getUnsignedInt(input.readerIndex() + Short.BYTES
                            + 2 * Byte.BYTES);
                    key.setExperimenterId(Uint32.valueOf(expId));
                    return key;
                }
                key.setExperimenterId(null);
                return key;
            }
        };
    }

    public static CodeKeyMaker createActionsKeyMaker(final short version) {
        return new AbstractCodeKeyMaker(version) {
            @Override
            public MessageCodeKey make(final ByteBuf input) {
                int type = input.getUnsignedShort(input.readerIndex());
                if (type == EncodeConstants.EXPERIMENTER_VALUE) {
                    Long expId = input.getUnsignedInt(input.readerIndex()
                            + 2 * Short.BYTES);
                    return new ExperimenterActionDeserializerKey(getVersion(), expId);
                }
                ActionDeserializerKey actionDeserializerKey = new ActionDeserializerKey(getVersion(), type, null);
                return actionDeserializerKey;
            }
        };
    }

    public static CodeKeyMaker createInstructionsKeyMaker(final short version) {
        return new AbstractCodeKeyMaker(version) {
            @Override
            public MessageCodeKey make(final ByteBuf input) {
                int type = input.getUnsignedShort(input.readerIndex());
                if (type == EncodeConstants.EXPERIMENTER_VALUE) {
                    Long expId = input.getUnsignedInt(input.readerIndex()
                            + 2 * Short.BYTES);
                    return new ExperimenterInstructionDeserializerKey(getVersion(), expId);
                }
                return new InstructionDeserializerKey(getVersion(), type, null);
            }
        };
    }
}
