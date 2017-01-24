/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.deserialization.multipart;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.multipart.reply.multipart.reply.body.MultipartReplyGroupFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.Chaining;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.ChainingChecks;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupAll;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupCapabilities;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupIndirect;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupSelect;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupTypes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;

public class MultipartReplyGroupFeaturesDeserializerTest extends AbstractMultipartDeserializerTest {
    private static final List<Long> MAX_GROUPS_LIST = Arrays.asList(1l, 2l, 3l, 4l);
    private static final List<Long> ACTIONS_LIST = Arrays.asList(5l, 6l, 7l, 8l);

    private static final List<GroupTypes> GROUP_TYPES_SUPPORTED = Arrays.asList(
            GroupTypes.GroupAll,
            GroupTypes.GroupSelect,
            GroupTypes.GroupIndirect);

    private static final List<GroupCapabilities> GROUP_CAPABILITIES_SUPPORTED = Arrays.asList(
            GroupCapabilities.Chaining,
            GroupCapabilities.ChainingChecks);

    @Test
    public void testDeserialize() throws Exception {

        int bitMaskGroups = ByteBufUtils.fillBitMask(0,
                GROUP_TYPES_SUPPORTED.contains(GroupTypes.GroupAll),
                GROUP_TYPES_SUPPORTED.contains(GroupTypes.GroupSelect),
                GROUP_TYPES_SUPPORTED.contains(GroupTypes.GroupIndirect),
                GROUP_TYPES_SUPPORTED.contains(GroupTypes.GroupFf));

        int bitMaskCapabilities = ByteBufUtils.fillBitMask(0,
                GROUP_CAPABILITIES_SUPPORTED.contains(GroupCapabilities.SelectWeight),
                GROUP_CAPABILITIES_SUPPORTED.contains(GroupCapabilities.SelectLiveness),
                GROUP_CAPABILITIES_SUPPORTED.contains(GroupCapabilities.Chaining),
                GROUP_CAPABILITIES_SUPPORTED.contains(GroupCapabilities.ChainingChecks));

        ByteBuf buffer = UnpooledByteBufAllocator.DEFAULT.buffer();

        buffer.writeInt(bitMaskGroups);
        buffer.writeInt(bitMaskCapabilities);

        for (Long group: MAX_GROUPS_LIST) {
            buffer.writeInt(group.intValue());
        }

        for (Long action: ACTIONS_LIST) {
            buffer.writeInt(action.intValue());
        }

        final MultipartReplyGroupFeatures reply = (MultipartReplyGroupFeatures) deserializeMultipart(buffer);
        assertTrue(reply.getActions().containsAll(ACTIONS_LIST));
        assertTrue(reply.getMaxGroups().containsAll(MAX_GROUPS_LIST));

        assertEquals(GROUP_TYPES_SUPPORTED.size(), reply.getGroupTypesSupported().size());
        assertEquals(GroupAll.class, reply.getGroupTypesSupported().get(0));
        assertEquals(GroupSelect.class, reply.getGroupTypesSupported().get(1));
        assertEquals(GroupIndirect.class, reply.getGroupTypesSupported().get(2));

        assertEquals(GROUP_CAPABILITIES_SUPPORTED.size(), reply.getGroupCapabilitiesSupported().size());
        assertEquals(Chaining.class, reply.getGroupCapabilitiesSupported().get(0));
        assertEquals(ChainingChecks.class, reply.getGroupCapabilitiesSupported().get(1));

        assertEquals(0, buffer.readableBytes());
    }

    @Override
    protected int getType() {
        return MultipartType.OFPMPGROUPFEATURES.getIntValue();
    }
}