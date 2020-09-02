/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization;

import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.deserialization.action.OF10EnqueueActionDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.action.OF10OutputActionDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.action.OF10SetDlDstActionDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.action.OF10SetDlSrcActionDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.action.OF10SetNwDstActionDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.action.OF10SetNwSrcActionDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.action.OF10SetNwTosActionDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.action.OF10SetTpDstActionDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.action.OF10SetTpSrcActionDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.action.OF10SetVlanPcpActionDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.action.OF10SetVlanVidActionDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.action.OF10StripVlanActionDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.action.OF13CopyTtlInActionDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.action.OF13CopyTtlOutActionDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.action.OF13DecMplsTtlActionDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.action.OF13DecNwTtlActionDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.action.OF13GroupActionDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.action.OF13OutputActionDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.action.OF13PopMplsActionDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.action.OF13PopPbbActionDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.action.OF13PopVlanActionDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.action.OF13PushMplsActionDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.action.OF13PushPbbActionDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.action.OF13PushVlanActionDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.action.OF13SetFieldActionDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.action.OF13SetMplsTtlActionDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.action.OF13SetNwTtlActionDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.action.OF13SetQueueActionDeserializer;
import org.opendaylight.openflowjava.protocol.impl.util.ActionConstants;
import org.opendaylight.openflowjava.protocol.impl.util.ActionDeserializerRegistryHelper;

/**
 * Helper for registering action deserializer initializers.
 *
 * @author michal.polkorab
 */
public final class ActionDeserializerInitializer {
    private ActionDeserializerInitializer() {
        throw new UnsupportedOperationException("Utility class shouldn't be instantiated");
    }

    /**
     * Registers action deserializers.
     *
     * @param registry registry to be filled with deserializers
     */
    public static void registerDeserializers(final DeserializerRegistry registry) {
        // register OF v1.0 action deserializers
        ActionDeserializerRegistryHelper helper =
                new ActionDeserializerRegistryHelper(EncodeConstants.OF10_VERSION_ID, registry);
        helper.registerDeserializer(ActionConstants.OUTPUT_CODE, new OF10OutputActionDeserializer());
        helper.registerDeserializer(ActionConstants.SET_VLAN_VID_CODE, new OF10SetVlanVidActionDeserializer());
        helper.registerDeserializer(ActionConstants.SET_VLAN_PCP_CODE, new OF10SetVlanPcpActionDeserializer());
        helper.registerDeserializer(ActionConstants.STRIP_VLAN_CODE, new OF10StripVlanActionDeserializer());
        helper.registerDeserializer(ActionConstants.SET_DL_SRC_CODE, new OF10SetDlSrcActionDeserializer());
        helper.registerDeserializer(ActionConstants.SET_DL_DST_CODE, new OF10SetDlDstActionDeserializer());
        helper.registerDeserializer(ActionConstants.SET_NW_SRC_CODE, new OF10SetNwSrcActionDeserializer());
        helper.registerDeserializer(ActionConstants.SET_NW_DST_CODE, new OF10SetNwDstActionDeserializer());
        helper.registerDeserializer(ActionConstants.SET_NW_TOS_CODE, new OF10SetNwTosActionDeserializer());
        helper.registerDeserializer(ActionConstants.SET_TP_SRC_CODE, new OF10SetTpSrcActionDeserializer());
        helper.registerDeserializer(ActionConstants.SET_TP_DST_CODE, new OF10SetTpDstActionDeserializer());
        helper.registerDeserializer(ActionConstants.ENQUEUE_CODE, new OF10EnqueueActionDeserializer());
        // register OF v1.3 action deserializers
        helper = new ActionDeserializerRegistryHelper(EncodeConstants.OF13_VERSION_ID, registry);
        helper.registerDeserializer(ActionConstants.OUTPUT_CODE, new OF13OutputActionDeserializer());
        helper.registerDeserializer(ActionConstants.COPY_TTL_OUT_CODE, new OF13CopyTtlOutActionDeserializer());
        helper.registerDeserializer(ActionConstants.COPY_TTL_IN_CODE, new OF13CopyTtlInActionDeserializer());
        helper.registerDeserializer(ActionConstants.SET_MPLS_TTL_CODE, new OF13SetMplsTtlActionDeserializer());
        helper.registerDeserializer(ActionConstants.DEC_MPLS_TTL_CODE, new OF13DecMplsTtlActionDeserializer());
        helper.registerDeserializer(ActionConstants.PUSH_VLAN_CODE, new OF13PushVlanActionDeserializer());
        helper.registerDeserializer(ActionConstants.POP_VLAN_CODE, new OF13PopVlanActionDeserializer());
        helper.registerDeserializer(ActionConstants.PUSH_MPLS_CODE, new OF13PushMplsActionDeserializer());
        helper.registerDeserializer(ActionConstants.POP_MPLS_CODE, new OF13PopMplsActionDeserializer());
        helper.registerDeserializer(ActionConstants.SET_QUEUE_CODE, new OF13SetQueueActionDeserializer());
        helper.registerDeserializer(ActionConstants.GROUP_CODE, new OF13GroupActionDeserializer());
        helper.registerDeserializer(ActionConstants.SET_NW_TTL_CODE, new OF13SetNwTtlActionDeserializer());
        helper.registerDeserializer(ActionConstants.DEC_NW_TTL_CODE, new OF13DecNwTtlActionDeserializer());
        helper.registerDeserializer(ActionConstants.SET_FIELD_CODE, new OF13SetFieldActionDeserializer());
        helper.registerDeserializer(ActionConstants.PUSH_PBB_CODE, new OF13PushPbbActionDeserializer());
        helper.registerDeserializer(ActionConstants.POP_PBB_CODE, new OF13PopPbbActionDeserializer());
    }
}
