/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.nx.api.impl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.annotations.VisibleForTesting;
import org.opendaylight.openflowjava.nx.api.NiciraActionDeserializerKey;
import org.opendaylight.openflowjava.nx.api.NiciraActionSerializerKey;
import org.opendaylight.openflowjava.nx.api.NiciraExtensionCodecRegistrator;
import org.opendaylight.openflowjava.nx.api.NiciraUtil;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFGeneralDeserializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFGeneralSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.keys.ActionSerializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.ExperimenterActionDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.ExperimenterDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.ExperimenterSerializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntryDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntrySerializerKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.spi.connection.SwitchConnectionProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OxmClassBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;

/**
 * @author msunal
 *
 */
public class NiciraExtensionCodecRegistratorImpl implements NiciraExtensionCodecRegistrator, AutoCloseable {

    private static final Map<NiciraActionDeserializerKey, OFDeserializer<Action>> actionDeserializers = new ConcurrentHashMap<>();

    private final List<SwitchConnectionProvider> providers;

    /**
     * @param providers
     */
    public NiciraExtensionCodecRegistratorImpl(List<SwitchConnectionProvider> providers) {
        this.providers = providers;
        ActionDeserializer of10ActionDeserializer = new ActionDeserializer(EncodeConstants.OF10_VERSION_ID);
        ActionDeserializer of13ActionDeserializer = new ActionDeserializer(EncodeConstants.OF13_VERSION_ID);
        registerActionDeserializer(ActionDeserializer.OF10_DESERIALIZER_KEY, of10ActionDeserializer);
        registerActionDeserializer(ActionDeserializer.OF13_DESERIALIZER_KEY, of13ActionDeserializer);
    }

    private void registerActionDeserializer(ExperimenterActionDeserializerKey key, OFGeneralDeserializer deserializer) {
        for (SwitchConnectionProvider provider : providers) {
            provider.registerActionDeserializer(key, deserializer);
        }
    }

    private void registerActionSerializer(ActionSerializerKey<?> key, OFGeneralSerializer serializer) {
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

    /*
     * (non-Javadoc)
     * 
     * @see org.opendaylight.openflow.extension.nicira.api.
     * NiciraExtensionCodecRegistrator
     * #registerActionDeserializer(org.opendaylight
     * .openflow.extension.nicira.api.NiciraActionDeserializerKey,
     * org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer)
     */
    @Override
    public void registerActionDeserializer(NiciraActionDeserializerKey key, OFDeserializer<Action> deserializer) {
        actionDeserializers.put(key, deserializer);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opendaylight.openflow.extension.nicira.api.
     * NiciraExtensionCodecRegistrator
     * #unregisterActionDeserializer(org.opendaylight
     * .openflow.extension.nicira.api.NiciraActionDeserializerKey)
     */
    @Override
    public void unregisterActionDeserializer(NiciraActionDeserializerKey key) {
        actionDeserializers.remove(key);
    }

    static OFDeserializer<Action> getActionDeserializer(NiciraActionDeserializerKey key) {
        return actionDeserializers.get(key);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opendaylight.openflow.extension.nicira.api.
     * NiciraExtensionCodecRegistrator
     * #registerActionSerializer(org.opendaylight.
     * openflow.extension.nicira.api.NiciraActionSerializerKey,
     * org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer)
     */
    @Override
    public void registerActionSerializer(NiciraActionSerializerKey key, OFSerializer<Action> serializer) {
        registerActionSerializer(NiciraUtil.createOfJavaKeyFrom(key), serializer);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opendaylight.openflow.extension.nicira.api.
     * NiciraExtensionCodecRegistrator
     * #unregisterActionSerializer(org.opendaylight
     * .openflow.extension.nicira.api.NiciraActionSerializerKey)
     */
    @Override
    public void unregisterActionSerializer(NiciraActionSerializerKey key) {
        unregisterSerializer(NiciraUtil.createOfJavaKeyFrom(key));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opendaylight.openflow.extension.nicira.api.
     * NiciraExtensionCodecRegistrator
     * #registerMatchEntryDeserializer(org.opendaylight
     * .openflowjava.protocol.api.keys.MatchEntryDeserializerKey,
     * org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer)
     */
    @Override
    public void registerMatchEntryDeserializer(MatchEntryDeserializerKey key, OFDeserializer<MatchEntry> deserializer) {
        for (SwitchConnectionProvider provider : providers) {
            provider.registerMatchEntryDeserializer(key, deserializer);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opendaylight.openflow.extension.nicira.api.
     * NiciraExtensionCodecRegistrator
     * #unregisterMatchEntryDeserializer(org.opendaylight
     * .openflowjava.protocol.api.keys.MatchEntryDeserializerKey)
     */
    @Override
    public void unregisterMatchEntryDeserializer(MatchEntryDeserializerKey key) {
        unregisterDeserializer(key);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opendaylight.openflow.extension.nicira.api.
     * NiciraExtensionCodecRegistrator
     * #registerMatchEntrySerializer(org.opendaylight
     * .openflowjava.protocol.api.keys.MatchEntrySerializerKey,
     * org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer)
     */
    @Override
    public void registerMatchEntrySerializer(MatchEntrySerializerKey<? extends OxmClassBase, ? extends MatchField> key,
            OFSerializer<MatchEntry> serializer) {
        for (SwitchConnectionProvider provider : providers) {
            provider.registerMatchEntrySerializer(key, serializer);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opendaylight.openflow.extension.nicira.api.
     * NiciraExtensionCodecRegistrator
     * #unregisterMatchEntrySerializer(org.opendaylight
     * .openflowjava.protocol.api.keys.MatchEntrySerializerKey)
     */
    @Override
    public void unregisterMatchEntrySerializer(MatchEntrySerializerKey<? extends OxmClassBase, ? extends MatchField> key) {
        unregisterSerializer(key);
    }

    @Override
    public void close() throws Exception {
        // TODO Auto-generated method stub
    }

    @VisibleForTesting
    boolean isEmptyActionDeserializers() {
        return actionDeserializers.isEmpty();
    }

}
