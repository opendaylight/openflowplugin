/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.deserialization.util;

import java.util.Objects;

import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.HeaderDeserializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.keys.ActionDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.ExperimenterActionDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.MessageCodeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.api.openflow.protocol.deserialization.MessageCodeExperimenterKey;
import org.opendaylight.openflowplugin.extension.api.path.ActionPath;
import org.opendaylight.openflowplugin.openflow.md.core.extension.ActionExtensionHelper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action;

import io.netty.buffer.ByteBuf;

/**
 * Utility class for action deserialization
 */
public class ActionUtil {

    /**
     * Deserialize OpenFlow action, using extension converter if available
     * TODO: Remove also extension converters
     *
     * @param version OpenFlow version
     * @param message OpenFlow buffered message
     * @param registry deserializer registry
     * @param path Action path
     */
    public static Action readAction(short version, ByteBuf message, DeserializerRegistry registry,
            ActionPath path) {
        int type = message.getUnsignedShort(message.readerIndex());
        Long expId = null;

        if (type == EncodeConstants.EXPERIMENTER_VALUE) {
            expId = message.getUnsignedInt(message.readerIndex()
                    + 2 * EncodeConstants.SIZE_OF_SHORT_IN_BYTES);
        }

        try {
            final MessageCodeExperimenterKey key = new MessageCodeExperimenterKey(
                version, type, Action.class, expId);

            final OFDeserializer<Action> deserializer = registry.getDeserializer(key);

            return deserializer.deserialize(message);
        } catch (ClassCastException | IllegalStateException e) {
            final MessageCodeKey key = Objects.nonNull(expId)
                ? new ExperimenterActionDeserializerKey(version, expId)
                : new ActionDeserializerKey(version, type, expId);

            final OFDeserializer<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203
                .actions.grouping.Action> deserializer = registry.getDeserializer(key);

            return ActionExtensionHelper.processAlienAction(deserializer.deserialize(message),
                    OpenflowVersion.get(version), path);
        }
    }

    /**
     * Deserialize OpenFlow action header, using extension converter if available
     * TODO: Remove also extension converters
     *
     * @param version OpenFlow version
     * @param message OpenFlow buffered message
     * @param registry deserializer registry
     * @param path Action path
     */
    public static Action readActionHeader(short version, ByteBuf message, DeserializerRegistry registry,
            ActionPath path) {
        int type = message.getUnsignedShort(message.readerIndex());
        Long expId = null;

        if (type == EncodeConstants.EXPERIMENTER_VALUE) {
            expId = message.getUnsignedInt(message.readerIndex()
                    + 2 * EncodeConstants.SIZE_OF_SHORT_IN_BYTES);
        }

        try {
            final MessageCodeExperimenterKey key = new MessageCodeExperimenterKey(
                version, type, Action.class, expId);

            final HeaderDeserializer<Action> deserializer = registry.getDeserializer(key);

            return deserializer.deserializeHeader(message);
        } catch (ClassCastException | IllegalStateException e) {
            final MessageCodeKey key = Objects.nonNull(expId)
                ? new ExperimenterActionDeserializerKey(version, expId)
                : new ActionDeserializerKey(version, type, expId);

            final HeaderDeserializer<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203
                .actions.grouping.Action> deserializer = registry.getDeserializer(key);

            return ActionExtensionHelper.processAlienAction(deserializer.deserializeHeader(message),
                    OpenflowVersion.get(version), path);
        }
    }

}
