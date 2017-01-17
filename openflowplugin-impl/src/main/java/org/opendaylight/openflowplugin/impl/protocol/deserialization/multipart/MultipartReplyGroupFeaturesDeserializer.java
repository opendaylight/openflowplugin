/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.deserialization.multipart;

import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
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
    public MultipartReplyBody deserialize(ByteBuf message) {
        final MultipartReplyGroupFeaturesBuilder builder = new MultipartReplyGroupFeaturesBuilder();

        return builder
            .setGroupTypesSupported(readGroupTypes(message))
            .setGroupCapabilitiesSupported(readGroupCapabilities(message))
            .setMaxGroups(IntStream
                    .range(0, GROUP_TYPES)
                    .mapToObj(i -> message.readUnsignedInt())
                    .collect(Collectors.toList()))
            .setActions(IntStream
                    .range(0, GROUP_TYPES)
                    .mapToObj(i -> message.readUnsignedInt())
                    .collect(Collectors.toList()))
            .build();
    }

    private static List<Class<? extends GroupCapability>> readGroupCapabilities(ByteBuf message) {
        final List<Class<? extends GroupCapability>> groupCapabilities = new ArrayList<>();
        final long capabilitiesMask = message.readUnsignedInt();

        final boolean gcSelectWeight = ((capabilitiesMask) & (1 << 0)) != 0;
        final boolean gcSelectLiveness = ((capabilitiesMask) & (1 << 1)) != 0;
        final boolean gcChaining = ((capabilitiesMask) & (1 << 2)) != 0;
        final boolean gcChainingChecks = ((capabilitiesMask) & (1 << 3)) != 0;

        if (gcSelectWeight) groupCapabilities.add(SelectWeight.class);
        if (gcSelectLiveness) groupCapabilities.add(SelectLiveness.class);
        if (gcChaining) groupCapabilities.add(Chaining.class);
        if (gcChainingChecks) groupCapabilities.add(ChainingChecks.class);

        return groupCapabilities;
    }

    private static List<Class<? extends GroupType>> readGroupTypes(ByteBuf message) {
        final List<Class<? extends GroupType>> groupTypes = new ArrayList<>();
        final long typesMask = message.readUnsignedInt();

        final boolean gtAll = ((typesMask) & (1 << 0)) != 0;
        final boolean gtSelect = ((typesMask) & (1 << 1)) != 0;
        final boolean gtIndirect = ((typesMask) & (1 << 2)) != 0;
        final boolean gtFF = ((typesMask) & (1 << 3)) != 0;

        if (gtAll) groupTypes.add(GroupAll.class);
        if (gtSelect) groupTypes.add(GroupSelect.class);
        if (gtIndirect) groupTypes.add(GroupIndirect.class);
        if (gtFF) groupTypes.add(GroupFf.class);

        return groupTypes;
    }

}
