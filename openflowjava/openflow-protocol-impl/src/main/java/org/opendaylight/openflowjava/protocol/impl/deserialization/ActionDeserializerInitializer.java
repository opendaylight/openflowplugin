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
import org.opendaylight.openflowjava.protocol.impl.util.ActionDeserializerRegistryHelper;

/**
 * @author michal.polkorab
 *
 */
public final class ActionDeserializerInitializer {

    private ActionDeserializerInitializer() {
        throw new UnsupportedOperationException("Utility class shouldn't be instantiated");
    }

    /**
     * Registers action deserializers
     * @param registry registry to be filled with deserializers
     */
    public static void registerDeserializers(DeserializerRegistry registry) {
        // register OF v1.0 action deserializers
        ActionDeserializerRegistryHelper helper =
                new ActionDeserializerRegistryHelper(EncodeConstants.OF10_VERSION_ID, registry);
        helper.registerDeserializer(0, new OF10OutputActionDeserializer());
        helper.registerDeserializer(1, new OF10SetVlanVidActionDeserializer());
        helper.registerDeserializer(2, new OF10SetVlanPcpActionDeserializer());
        helper.registerDeserializer(3, new OF10StripVlanActionDeserializer());
        helper.registerDeserializer(4, new OF10SetDlSrcActionDeserializer());
        helper.registerDeserializer(5, new OF10SetDlDstActionDeserializer());
        helper.registerDeserializer(6, new OF10SetNwSrcActionDeserializer());
        helper.registerDeserializer(7, new OF10SetNwDstActionDeserializer());
        helper.registerDeserializer(8, new OF10SetNwTosActionDeserializer());
        helper.registerDeserializer(9, new OF10SetTpSrcActionDeserializer());
        helper.registerDeserializer(10, new OF10SetTpDstActionDeserializer());
        helper.registerDeserializer(11, new OF10EnqueueActionDeserializer());
        // register OF v1.3 action deserializers
        helper = new ActionDeserializerRegistryHelper(EncodeConstants.OF13_VERSION_ID, registry);
        helper.registerDeserializer(0, new OF13OutputActionDeserializer());
        helper.registerDeserializer(11, new OF13CopyTtlOutActionDeserializer());
        helper.registerDeserializer(12, new OF13CopyTtlInActionDeserializer());
        helper.registerDeserializer(15, new OF13SetMplsTtlActionDeserializer());
        helper.registerDeserializer(16, new OF13DecMplsTtlActionDeserializer());
        helper.registerDeserializer(17, new OF13PushVlanActionDeserializer());
        helper.registerDeserializer(18, new OF13PopVlanActionDeserializer());
        helper.registerDeserializer(19, new OF13PushMplsActionDeserializer());
        helper.registerDeserializer(20, new OF13PopMplsActionDeserializer());
        helper.registerDeserializer(21, new OF13SetQueueActionDeserializer());
        helper.registerDeserializer(22, new OF13GroupActionDeserializer());
        helper.registerDeserializer(23, new OF13SetNwTtlActionDeserializer());
        helper.registerDeserializer(24, new OF13DecNwTtlActionDeserializer());
        helper.registerDeserializer(25, new OF13SetFieldActionDeserializer());
        helper.registerDeserializer(26, new OF13PushPbbActionDeserializer());
        helper.registerDeserializer(27, new OF13PopPbbActionDeserializer());
    }
}