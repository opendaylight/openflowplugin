/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.serialization;

import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.serialization.action.EmptyActionSerializer;
import org.opendaylight.openflowjava.protocol.impl.serialization.action.OF10EnqueueActionSerializer;
import org.opendaylight.openflowjava.protocol.impl.serialization.action.OF10OutputActionSerializer;
import org.opendaylight.openflowjava.protocol.impl.serialization.action.OF10SetDlDstActionSerializer;
import org.opendaylight.openflowjava.protocol.impl.serialization.action.OF10SetDlSrcActionSerializer;
import org.opendaylight.openflowjava.protocol.impl.serialization.action.OF10SetNwDstActionSerializer;
import org.opendaylight.openflowjava.protocol.impl.serialization.action.OF10SetNwSrcActionSerializer;
import org.opendaylight.openflowjava.protocol.impl.serialization.action.OF10SetNwTosActionSerializer;
import org.opendaylight.openflowjava.protocol.impl.serialization.action.OF10SetTpDstActionSerializer;
import org.opendaylight.openflowjava.protocol.impl.serialization.action.OF10SetTpSrcActionSerializer;
import org.opendaylight.openflowjava.protocol.impl.serialization.action.OF10SetVlanPcpActionSerializer;
import org.opendaylight.openflowjava.protocol.impl.serialization.action.OF10SetVlanVidActionSerializer;
import org.opendaylight.openflowjava.protocol.impl.serialization.action.OF13GroupActionSerializer;
import org.opendaylight.openflowjava.protocol.impl.serialization.action.OF13OutputActionSerializer;
import org.opendaylight.openflowjava.protocol.impl.serialization.action.OF13PopMplsActionSerializer;
import org.opendaylight.openflowjava.protocol.impl.serialization.action.OF13PushMplsActionSerializer;
import org.opendaylight.openflowjava.protocol.impl.serialization.action.OF13PushPbbActionSerializer;
import org.opendaylight.openflowjava.protocol.impl.serialization.action.OF13PushVlanActionSerializer;
import org.opendaylight.openflowjava.protocol.impl.serialization.action.OF13SetFieldActionSerializer;
import org.opendaylight.openflowjava.protocol.impl.serialization.action.OF13SetMplsTtlActionSerializer;
import org.opendaylight.openflowjava.protocol.impl.serialization.action.OF13SetNwTtlActionSerializer;
import org.opendaylight.openflowjava.protocol.impl.serialization.action.OF13SetQueueActionSerializer;
import org.opendaylight.openflowjava.protocol.impl.util.ActionConstants;
import org.opendaylight.openflowjava.protocol.impl.util.ActionSerializerRegistryHelper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.CopyTtlInCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.CopyTtlOutCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.DecMplsTtlCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.DecNwTtlCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.EnqueueCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.GroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.OutputActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PopMplsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PopPbbCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PopVlanCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PushMplsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PushPbbCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PushVlanCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetDlDstCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetDlSrcCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetFieldCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetMplsTtlCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetNwDstCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetNwSrcCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetNwTosCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetNwTtlCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetQueueCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetTpDstCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetTpSrcCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetVlanPcpCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetVlanVidCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.StripVlanCase;
//import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.Group;

/**
 * Initializes serializer registry with action serializers.
 *
 * @author michal.polkorab
 */
public final class ActionsInitializer {

    private ActionsInitializer() {
        throw new UnsupportedOperationException("Utility class shouldn't be instantiated");
    }

    /**
     * Registers action serializers into provided registry.
     *
     * @param serializerRegistry registry to be initialized with action serializers
     */
    public static void registerActionSerializers(final SerializerRegistry serializerRegistry) {
        // register OF v1.0 action serializers
        ActionSerializerRegistryHelper helper = new ActionSerializerRegistryHelper(
                EncodeConstants.OF10_VERSION_ID, serializerRegistry);
        helper.registerSerializer(OutputActionCase.class, new OF10OutputActionSerializer());
        helper.registerSerializer(SetVlanVidCase.class, new OF10SetVlanVidActionSerializer());
        helper.registerSerializer(SetVlanPcpCase.class, new OF10SetVlanPcpActionSerializer());
        helper.registerSerializer(StripVlanCase.class, new EmptyActionSerializer(ActionConstants.STRIP_VLAN_CODE));
        helper.registerSerializer(SetDlSrcCase.class, new OF10SetDlSrcActionSerializer());
        helper.registerSerializer(SetDlDstCase.class, new OF10SetDlDstActionSerializer());
        helper.registerSerializer(SetNwSrcCase.class, new OF10SetNwSrcActionSerializer());
        helper.registerSerializer(SetNwDstCase.class, new OF10SetNwDstActionSerializer());
        helper.registerSerializer(SetNwTosCase.class, new OF10SetNwTosActionSerializer());
        helper.registerSerializer(SetTpSrcCase.class, new OF10SetTpSrcActionSerializer());
        helper.registerSerializer(SetTpDstCase.class, new OF10SetTpDstActionSerializer());
        helper.registerSerializer(EnqueueCase.class, new OF10EnqueueActionSerializer());
        // register OF v1.0 action serializers
        helper = new ActionSerializerRegistryHelper(
                EncodeConstants.OF13_VERSION_ID, serializerRegistry);
        helper.registerSerializer(OutputActionCase.class, new OF13OutputActionSerializer());
        helper.registerSerializer(CopyTtlOutCase.class, new EmptyActionSerializer(ActionConstants.COPY_TTL_OUT_CODE));
        helper.registerSerializer(CopyTtlInCase.class, new EmptyActionSerializer(ActionConstants.COPY_TTL_IN_CODE));
        helper.registerSerializer(SetMplsTtlCase.class, new OF13SetMplsTtlActionSerializer());
        helper.registerSerializer(DecMplsTtlCase.class, new EmptyActionSerializer(ActionConstants.DEC_MPLS_TTL_CODE));
        helper.registerSerializer(PushVlanCase.class, new OF13PushVlanActionSerializer());
        helper.registerSerializer(PopVlanCase.class, new EmptyActionSerializer(ActionConstants.POP_VLAN_CODE));
        helper.registerSerializer(PushMplsCase.class, new OF13PushMplsActionSerializer());
        helper.registerSerializer(PopMplsCase.class, new OF13PopMplsActionSerializer());
        helper.registerSerializer(SetQueueCase.class, new OF13SetQueueActionSerializer());
        helper.registerSerializer(GroupCase.class, new OF13GroupActionSerializer());
        helper.registerSerializer(SetNwTtlCase.class, new OF13SetNwTtlActionSerializer());
        helper.registerSerializer(DecNwTtlCase.class, new EmptyActionSerializer(ActionConstants.DEC_NW_TTL_CODE));
        helper.registerSerializer(SetFieldCase.class, new OF13SetFieldActionSerializer());
        helper.registerSerializer(PushPbbCase.class, new OF13PushPbbActionSerializer());
        helper.registerSerializer(PopPbbCase.class, new EmptyActionSerializer(ActionConstants.POP_PBB_CODE));
    }
}
