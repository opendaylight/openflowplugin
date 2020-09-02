/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.api.extensibility;

import org.opendaylight.openflowjava.protocol.api.keys.ExperimenterActionDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.ExperimenterDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.ExperimenterIdDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.ExperimenterInstructionDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntryDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.MessageCodeKey;
import org.opendaylight.openflowjava.protocol.api.keys.TypeToClassKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.experimenter.core.ExperimenterDataOfChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.MeterBandExperimenterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.queue.property.header.QueueProperty;

/**
 * Provides methods for deserialization part of extensibility.
 * In case of handling multiple multiple structures of same type (actions,
 * instructions, match entries, ... ) which are differentiated by
 * vendor / experimenter subtype, vendor has to switch / choose between
 * these subtypes.<br>
 *
 * <p>
 * This has to be done in this way because of experimenter headers, which
 * provide only vendor / experimenter ID. Subtype position may be different
 * for different vendors (or not present at all) - that's why vendor has to
 * handle it in his own implementations.
 * @author michal.polkorab
 */
public interface DeserializerExtensionProvider {
    /**
     * Registers deserializer.
     * Throws IllegalStateException when there is
     * a deserializer already registered under given key.
     *
     * @param key          used for deserializer lookup
     * @param deserializer deserializer instance
     */
    void registerDeserializer(MessageCodeKey key, OFGeneralDeserializer deserializer);

    /**
     * Unregisters custom deserializer.
     *
     * @param key used for deserializer lookup
     * @return true if deserializer was removed,
     *     false if no deserializer was found under specified key
     */
    boolean unregisterDeserializer(ExperimenterDeserializerKey key);

    /**
     * Registers action deserializer.
     *
     * @param key used for deserializer lookup
     * @param deserializer deserializer instance
     */
    void registerActionDeserializer(ExperimenterActionDeserializerKey key, OFGeneralDeserializer deserializer);

    /**
     * Registers instruction deserializer.
     *
     * @param key used for deserializer lookup
     * @param deserializer deserializer instance
     */
    void registerInstructionDeserializer(ExperimenterInstructionDeserializerKey key,
            OFGeneralDeserializer deserializer);

    /**
     * Registers match entry deserializer.
     *
     * @param key used for deserializer lookup
     * @param deserializer deserializer instance
     */
    void registerMatchEntryDeserializer(MatchEntryDeserializerKey key, OFGeneralDeserializer deserializer);

    /**
     * Registers error message deserializer.
     *
     * @param key used for deserializer lookup
     * @param deserializer deserializer instance
     */
    void registerErrorDeserializer(ExperimenterIdDeserializerKey key, OFDeserializer<ErrorMessage> deserializer);

    /**
     * Registers experimenter (vendor) message deserializer.
     *
     * @param key used for deserializer lookup
     * @param deserializer deserializer instance
     */
    void registerExperimenterMessageDeserializer(ExperimenterIdDeserializerKey key,
                                                 OFDeserializer<? extends ExperimenterDataOfChoice> deserializer);

    /**
     * Registers multipart-reply (stats) message deserializer.
     *
     * @param key used for deserializer lookup
     * @param deserializer deserializer instance
     */
    void registerMultipartReplyMessageDeserializer(ExperimenterIdDeserializerKey key,
                                                   OFDeserializer<? extends ExperimenterDataOfChoice> deserializer);

    /**
     * Registers multipart-reply table-features message deserializer.
     *
     * @param key used for deserializer lookup
     * @param deserializer deserializer instance
     */
    void registerMultipartReplyTFDeserializer(ExperimenterIdDeserializerKey key, OFGeneralDeserializer deserializer);

    /**
     * Registers meter band deserializer (used in multipart-reply meter-config).
     *
     * @param key used for deserializer lookup
     * @param deserializer deserializer instance
     */
    void registerMeterBandDeserializer(ExperimenterIdDeserializerKey key,
            OFDeserializer<MeterBandExperimenterCase> deserializer);

    /**
     * Registers queue property (QUEUE_GET_CONFIG_REPLY message) deserializer.
     *
     * @param key used for deserializer lookup
     * @param deserializer deserializer instance
     */
    void registerQueuePropertyDeserializer(ExperimenterIdDeserializerKey key,
            OFDeserializer<QueueProperty> deserializer);

    /**
     * Registers type to class mapping used to assign return type when deserializing message.
     *
     * @param key type to class key
     * @param clazz return class
     */
    void registerDeserializerMapping(TypeToClassKey key, Class<?> clazz);

    /**
     * Unregisters type to class mapping used to assign return type when deserializing message.
     *
     * @param key type to class key
     * @return true if mapping was successfully removed
     */
    boolean unregisterDeserializerMapping(TypeToClassKey key);
}
