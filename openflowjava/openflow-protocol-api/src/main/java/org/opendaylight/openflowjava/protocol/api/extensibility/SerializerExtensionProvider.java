/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.api.extensibility;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.keys.ActionSerializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.ExperimenterIdMeterSubTypeSerializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.ExperimenterIdSerializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.ExperimenterSerializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.InstructionSerializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntrySerializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OxmClassBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.experimenter.core.ExperimenterDataOfChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.MeterBandExperimenterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.table.features.properties.grouping.TableFeatureProperties;
import org.opendaylight.yangtools.yang.binding.DataObject;


/**
 * Provides methods for serialization part of extensibility.
 * In case of handling multiple structures of same type (actions,
 * instructions, match entries, ... ) which are differentiated by
 * vendor / experimenter subtype, vendor has to switch / choose between
 * these subtypes.<br>
 *
 * This has to be done in this way because of unknown augmentations
 * - that's why vendor has to handle it in his own implementations.
 * @author michal.polkorab
 */
public interface SerializerExtensionProvider {

    /**
     * Transforms POJO message into ByteBuf
     * @param version version used for encoding received message
     * @param out ByteBuf for storing and sending transformed message
     * @param message POJO message
     */
    void messageToBuffer(short version, ByteBuf out, DataObject message);

    /**
     * Registers serializer
     * Throws IllegalStateException when there is
     * a serializer already registered under given key.
     *
     * If the serializer implements {@link SerializerRegistryInjector} interface,
     * the serializer is injected with SerializerRegistry instance.
     *
     * @param <K> serializer key type
     * @param key used for serializer lookup
     * @param serializer serializer implementation
     */
    <K> void registerSerializer(MessageTypeKey<K> key,
            OFGeneralSerializer serializer);

    /**
     * Unregisters custom serializer
     * @param key used for serializer lookup
     * @return true if serializer was removed,
     *  false if no serializer was found under specified key
     */
    boolean unregisterSerializer(ExperimenterSerializerKey key);

    /**
     * Registers action serializer
     * @param key used for serializer lookup
     * @param serializer serializer implementation
     */
    void registerActionSerializer(ActionSerializerKey<?> key,
            OFGeneralSerializer serializer);

    /**
     * Registers instruction serializer
     * @param key used for serializer lookup
     * @param serializer serializer implementation
     */
    void registerInstructionSerializer(InstructionSerializerKey<?> key,
            OFGeneralSerializer serializer);

    /**
     * Registers match entry serializer
     * @param <C> oxm type
     * @param <F> match field type
     * @param key used for serializer lookup
     * @param serializer serializer implementation
     */
    <C extends OxmClassBase, F extends MatchField> void registerMatchEntrySerializer(
            MatchEntrySerializerKey<C, F> key,OFGeneralSerializer serializer);

    /**
     * Registers experimenter (vendor) message serializer
     * @param key used for serializer lookup
     * @param serializer serializer implementation
     */
    void registerExperimenterMessageSerializer(ExperimenterIdSerializerKey<? extends ExperimenterDataOfChoice> key,
                                               OFSerializer<? extends ExperimenterDataOfChoice> serializer);

    /**
     * Registers multipart-request (stats-request) serializer
     * @param key used for serializer lookup
     * @param serializer serializer implementation
     */
    void registerMultipartRequestSerializer(ExperimenterIdSerializerKey<? extends ExperimenterDataOfChoice> key,
                                            OFSerializer<? extends ExperimenterDataOfChoice> serializer);

    /**
     * Registers multipart-request table-features serializer
     * @param key used for serializer lookup
     * @param serializer serializer implementation
     */
    void registerMultipartRequestTFSerializer(ExperimenterIdSerializerKey<TableFeatureProperties> key,
            OFGeneralSerializer serializer);

    /**
     * @deprecated Since we use ExperimenterIdMeterSubTypeSerializerKey as MeterBandSerializer's key, in order to avoid
     * the occurrence of an error,we should discard this function
     * Registers meter band serializer (used in meter-mod messages)
     * @param key used for serializer lookup
     * @param serializer serializer implementation
     */
    @Deprecated
    void registerMeterBandSerializer(ExperimenterIdSerializerKey<MeterBandExperimenterCase> key,
            OFSerializer<MeterBandExperimenterCase> serializer);

    /**
     * Registers meter band serializer (used in meter-mod messages)
     * @param key used for serializer lookup
     * @param serializer serializer implementation
     */
    void registerMeterBandSerializer(ExperimenterIdMeterSubTypeSerializerKey<MeterBandExperimenterCase> key,
                                     OFSerializer<MeterBandExperimenterCase> serializer);
}
