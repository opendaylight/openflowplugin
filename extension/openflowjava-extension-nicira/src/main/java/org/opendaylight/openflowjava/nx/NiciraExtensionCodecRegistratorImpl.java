/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.nx;

import com.google.common.annotations.VisibleForTesting;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.opendaylight.openflowjava.nx.api.NiciraActionDeserializerKey;
import org.opendaylight.openflowjava.nx.api.NiciraActionSerializerKey;
import org.opendaylight.openflowjava.nx.api.NiciraExtensionCodecRegistrator;
import org.opendaylight.openflowjava.nx.api.NiciraUtil;
import org.opendaylight.openflowjava.nx.codec.action.ActionDeserializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistryInjector;
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
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * Implementation of NiciraExtensionCodecRegistrator.
 *
 * @author msunal
 */
@Component(immediate = true)
public final class NiciraExtensionCodecRegistratorImpl implements NiciraExtensionCodecRegistrator {
    // FIXME: this should not be static!
    private static final Map<NiciraActionDeserializerKey, OFDeserializer<Action>> ACTION_DESERIALIZERS =
            new ConcurrentHashMap<>();

    private final ActionDeserializer of10ActionDeserializer = new ActionDeserializer(EncodeConstants.OF_VERSION_1_0);
    private final ActionDeserializer of13ActionDeserializer = new ActionDeserializer(EncodeConstants.OF_VERSION_1_3);
    private final List<SwitchConnectionProvider> providers;

    @Activate
    public NiciraExtensionCodecRegistratorImpl(
            @Reference(target = "(type=openflow-switch-connection-provider-default-impl)")
            final SwitchConnectionProvider defaultSwitchConnProvider,
            @Reference(target = "(type=openflow-switch-connection-provider-legacy-impl)")
            final SwitchConnectionProvider legacySwitchConnProvider) {
        this(List.of(defaultSwitchConnProvider, legacySwitchConnProvider));
    }

    public NiciraExtensionCodecRegistratorImpl(final List<SwitchConnectionProvider> providers) {
        this.providers = List.copyOf(providers);
        registerActionDeserializer(ActionDeserializer.OF10_DESERIALIZER_KEY, of10ActionDeserializer);
        registerActionDeserializer(ActionDeserializer.OF13_DESERIALIZER_KEY, of13ActionDeserializer);
    }

    private void registerActionDeserializer(final ExperimenterActionDeserializerKey key,
            final OFGeneralDeserializer deserializer) {
        for (var provider : providers) {
            provider.registerActionDeserializer(key, deserializer);
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
    public void registerActionDeserializer(final NiciraActionDeserializerKey key,
            final OFDeserializer<Action> deserializer) {
        if (deserializer instanceof DeserializerRegistryInjector registryInjectable) {
            if (EncodeConstants.OF_VERSION_1_0.equals(key.getVersion())) {
                registryInjectable.injectDeserializerRegistry(of10ActionDeserializer.getDeserializerRegistry());
            }
            if (EncodeConstants.OF_VERSION_1_3.equals(key.getVersion())) {
                registryInjectable.injectDeserializerRegistry(of13ActionDeserializer.getDeserializerRegistry());
            }
        }
        ACTION_DESERIALIZERS.put(key, deserializer);
    }

    private void registerActionSerializer(final ActionSerializerKey<?> key, final OFGeneralSerializer serializer) {
        for (var provider : providers) {
            provider.registerActionSerializer(key, serializer);
        }
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
    public void registerActionSerializer(final NiciraActionSerializerKey key, final OFSerializer<Action> serializer) {
        registerActionSerializer(NiciraUtil.createOfJavaKeyFrom(key), serializer);
    }

    private void unregisterDeserializer(final ExperimenterDeserializerKey key) {
        for (var provider : providers) {
            provider.unregisterDeserializer(key);
        }
    }

    private void unregisterSerializer(final ExperimenterSerializerKey key) {
        for (var provider : providers) {
            provider.unregisterSerializer(key);
        }
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
    public void unregisterActionDeserializer(final NiciraActionDeserializerKey key) {
        ACTION_DESERIALIZERS.remove(key);
    }

    public static OFDeserializer<Action> getActionDeserializer(final NiciraActionDeserializerKey key) {
        return ACTION_DESERIALIZERS.get(key);
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
    public void unregisterActionSerializer(final NiciraActionSerializerKey key) {
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
    public void registerMatchEntryDeserializer(final MatchEntryDeserializerKey key,
            final OFDeserializer<MatchEntry> deserializer) {
        for (var provider : providers) {
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
    public void unregisterMatchEntryDeserializer(final MatchEntryDeserializerKey key) {
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
    public void registerMatchEntrySerializer(
            final MatchEntrySerializerKey<? extends OxmClassBase, ? extends MatchField> key,
            final OFSerializer<MatchEntry> serializer) {
        for (var provider : providers) {
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
    public void unregisterMatchEntrySerializer(
            final MatchEntrySerializerKey<? extends OxmClassBase, ? extends MatchField> key) {
        unregisterSerializer(key);
    }

    @Deactivate
    @Override
    public void close() {
        // TODO Auto-generated method stub
    }

    @VisibleForTesting
    boolean isEmptyActionDeserializers() {
        return ACTION_DESERIALIZERS.isEmpty();
    }
}
