/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.cof.api.impl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.opendaylight.openflowjava.cof.api.CiscoActionDeserializerKey;
import org.opendaylight.openflowjava.cof.api.CiscoActionSerializerKey;
import org.opendaylight.openflowjava.cof.api.CiscoConstants;
import org.opendaylight.openflowjava.cof.api.CiscoExtensionCodecRegistrator;
import org.opendaylight.openflowjava.cof.api.CiscoUtil;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFGeneralDeserializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFGeneralSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntryDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntrySerializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.experimenter.ExperimenterActionDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.experimenter.ExperimenterActionSerializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.experimenter.ExperimenterDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.experimenter.ExperimenterSerializerKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.spi.connection.SwitchConnectionProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.OxmClassBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.oxm.fields.grouping.MatchEntries;

/**
 * @author msunal
 *
 */
public class CiscoExtensionCodecRegistratorImpl implements CiscoExtensionCodecRegistrator, AutoCloseable {
    
    private final Map<CiscoActionDeserializerKey, OFDeserializer<Action>> actionDeserializers = new ConcurrentHashMap<>();

    private final List<SwitchConnectionProvider> providers;

    /**
     * @param providers
     */
    public CiscoExtensionCodecRegistratorImpl(List<SwitchConnectionProvider> providers) {
        this.providers = providers;
        ActionDeserializer of10ActionDeserializer = new ActionDeserializer(EncodeConstants.OF10_VERSION_ID, actionDeserializers);
        ActionDeserializer of13ActionDeserializer = new ActionDeserializer(EncodeConstants.OF13_VERSION_ID, actionDeserializers);
        registerActionDeserializer(CiscoConstants.OF10_DESERIALIZER_KEY, of10ActionDeserializer);
        registerActionDeserializer(CiscoConstants.OF13_DESERIALIZER_KEY, of13ActionDeserializer);
    }

    private void registerActionDeserializer(ExperimenterActionDeserializerKey key, OFGeneralDeserializer deserializer) {
        for (SwitchConnectionProvider provider : providers) {
            provider.registerActionDeserializer(key, deserializer);
        }
    }

    private void registerActionSerializer(ExperimenterActionSerializerKey key, OFGeneralSerializer serializer) {
        for (SwitchConnectionProvider provider : providers) {
            provider.registerActionSerializer(key, serializer);
        }
    }

    private void unregisterDeserializer(ExperimenterDeserializerKey key) {
        for (SwitchConnectionProvider provider : providers) {
            provider.unregisterDeserializer(key);
        }
    }

    private void unregisterSerializer(ExperimenterSerializerKey key) {
        for (SwitchConnectionProvider provider : providers) {
            provider.unregisterSerializer(key);
        }
    }

    @Override
    public void registerActionDeserializer(CiscoActionDeserializerKey key, OFDeserializer<Action> deserializer) {
        actionDeserializers.put(key, deserializer);
    }

    @Override
    public void unregisterActionDeserializer(CiscoActionDeserializerKey key) {
        actionDeserializers.remove(key);
    }

    @Override
    public void registerActionSerializer(CiscoActionSerializerKey key, OFSerializer<Action> serializer) {
        registerActionSerializer(CiscoUtil.createOfJavaKeyFrom(key), serializer);
    }

    @Override
    public void unregisterActionSerializer(CiscoActionSerializerKey key) {
        unregisterSerializer(CiscoUtil.createOfJavaKeyFrom(key));
    }

    @Override
    public void registerMatchEntryDeserializer(MatchEntryDeserializerKey key, OFDeserializer<MatchEntries> deserializer) {
        for (SwitchConnectionProvider provider : providers) {
            provider.registerMatchEntryDeserializer(key, deserializer);
        }
    }

    @Override
    public void unregisterMatchEntryDeserializer(MatchEntryDeserializerKey key) {
        unregisterDeserializer(key);
    }

    @Override
    public void registerMatchEntrySerializer(MatchEntrySerializerKey<? extends OxmClassBase, ? extends MatchField> key,
            OFSerializer<MatchEntries> serializer) {
        for (SwitchConnectionProvider provider : providers) {
            provider.registerMatchEntrySerializer(key, serializer);
        }
    }

    @Override
    public void unregisterMatchEntrySerializer(MatchEntrySerializerKey<? extends OxmClassBase, ? extends MatchField> key) {
        unregisterSerializer(key);
    }

    @Override
    public void close() throws Exception {
        // ofJava extension API does not provide 
    }

}
