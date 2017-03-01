/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.deserialization;

import java.util.function.Consumer;
import java.util.function.Function;

import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerExtensionProvider;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.util.ActionConstants;
import org.opendaylight.openflowplugin.api.openflow.protocol.deserialization.MessageCodeExperimenterKey;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.action.CopyTtlInActionDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.action.CopyTtlOutActionDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.action.DecMplsTtlActionDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.action.DecNwTtlActionDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.action.GroupActionDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.action.OutputActionDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.action.PopMplsActionDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.action.PopPbbActionDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.action.PopVlanActionDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.action.PushMplsActionDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.action.PushPbbActionDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.action.PushVlanActionDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.action.SetFieldActionDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.action.SetMplsTtlActionDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.action.SetNwTtlActionDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.action.SetQueueActionDeserializer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action;

import com.google.common.annotations.VisibleForTesting;

class ActionDeserializerInjector {

    /**
     * Injects action deserializers into provided {@link org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerExtensionProvider}
     * @param provider OpenflowJava deserializer extension provider
     */
    static void injectDeserializers(final DeserializerExtensionProvider provider) {
        // Inject new message deserializers here using injector created by createInjector method
        final Function<Byte, Consumer<OFDeserializer<Action>>> injector =
                createInjector(provider, EncodeConstants.OF13_VERSION_ID);

        injector.apply(ActionConstants.COPY_TTL_IN_CODE).accept(new CopyTtlInActionDeserializer());
        injector.apply(ActionConstants.COPY_TTL_OUT_CODE).accept(new CopyTtlOutActionDeserializer());
        injector.apply(ActionConstants.OUTPUT_CODE).accept(new OutputActionDeserializer());
        injector.apply(ActionConstants.DEC_MPLS_TTL_CODE).accept(new DecMplsTtlActionDeserializer());
        injector.apply(ActionConstants.DEC_NW_TTL_CODE).accept(new DecNwTtlActionDeserializer());
        injector.apply(ActionConstants.GROUP_CODE).accept(new GroupActionDeserializer());
        injector.apply(ActionConstants.POP_MPLS_CODE).accept(new PopMplsActionDeserializer());
        injector.apply(ActionConstants.POP_PBB_CODE).accept(new PopPbbActionDeserializer());
        injector.apply(ActionConstants.POP_VLAN_CODE).accept(new PopVlanActionDeserializer());
        injector.apply(ActionConstants.PUSH_MPLS_CODE).accept(new PushMplsActionDeserializer());
        injector.apply(ActionConstants.PUSH_PBB_CODE).accept(new PushPbbActionDeserializer());
        injector.apply(ActionConstants.PUSH_VLAN_CODE).accept(new PushVlanActionDeserializer());
        injector.apply(ActionConstants.SET_MPLS_TTL_CODE).accept(new SetMplsTtlActionDeserializer());
        injector.apply(ActionConstants.SET_NW_TTL_CODE).accept(new SetNwTtlActionDeserializer());
        injector.apply(ActionConstants.SET_QUEUE_CODE).accept(new SetQueueActionDeserializer());
        injector.apply((byte) ActionConstants.SET_FIELD_CODE).accept(new SetFieldActionDeserializer());
    }

    /**
     * Create injector that will inject new deserializers into #{@link org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerExtensionProvider}
     * @param provider OpenflowJava deserializer extension provider
     * @param version Openflow version
     * @return injector
     */
    @VisibleForTesting
    static Function<Byte, Consumer<OFDeserializer<Action>>> createInjector(
            final DeserializerExtensionProvider provider,
            final short version) {
        return code -> deserializer -> {
            provider.registerDeserializer(
                    new MessageCodeExperimenterKey(version, code, Action.class, null),
                    deserializer);
        };
    }

}
