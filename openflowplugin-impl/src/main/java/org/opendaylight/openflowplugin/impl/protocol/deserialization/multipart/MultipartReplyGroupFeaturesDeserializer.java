/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.deserialization.multipart;

import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint32;

import com.google.common.collect.ImmutableSet;
import io.netty.buffer.ByteBuf;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.multipart.reply.multipart.reply.body.MultipartReplyGroupFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.Chaining;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.ChainingChecks;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupAll;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupCapability;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupFf;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupIndirect;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupSelect;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.SelectLiveness;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.SelectWeight;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.multipart.reply.MultipartReplyBody;

public class MultipartReplyGroupFeaturesDeserializer implements OFDeserializer<MultipartReplyBody> {
    private static final int GROUP_TYPES = 4;

    @Override
    public MultipartReplyBody deserialize(final ByteBuf message) {
        return new MultipartReplyGroupFeaturesBuilder()
            .setGroupTypesSupported(readGroupTypes(message))
            .setGroupCapabilitiesSupported(readGroupCapabilities(message))
            .setMaxGroups(IntStream.range(0, GROUP_TYPES)
                .mapToObj(i -> readUint32(message))
                .collect(Collectors.toUnmodifiableList()))
            .setActions(IntStream.range(0, GROUP_TYPES)
                .mapToObj(i -> readUint32(message))
                .collect(Collectors.toUnmodifiableList()))
            .build();
    }

    private static Set<Class<? extends GroupCapability>> readGroupCapabilities(final ByteBuf message) {
        final long capabilitiesMask = message.readUnsignedInt();

        final var builder = ImmutableSet.<Class<? extends GroupCapability>>builder();
        if ((capabilitiesMask & 1 << 0) != 0) {
            builder.add(SelectWeight.class);
        }
        if ((capabilitiesMask & 1 << 1) != 0) {
            builder.add(SelectLiveness.class);
        }
        if ((capabilitiesMask & 1 << 2) != 0) {
            builder.add(Chaining.class);
        }
        if ((capabilitiesMask & 1 << 3) != 0) {
            builder.add(ChainingChecks.class);
        }
        return builder.build();
    }

    private static Set<Class<? extends GroupType>> readGroupTypes(final ByteBuf message) {
        final long typesMask = message.readUnsignedInt();

        final var builder = ImmutableSet.<Class<? extends GroupType>>builder();
        if ((typesMask & 1 << 0) != 0) {
            builder.add(GroupAll.class);
        }
        if ((typesMask & 1 << 1) != 0) {
            builder.add(GroupSelect.class);
        }
        if ((typesMask & 1 << 2) != 0) {
            builder.add(GroupIndirect.class);
        }
        if ((typesMask & 1 << 3) != 0) {
            builder.add(GroupFf.class);
        }
        return builder.build();
    }
}
