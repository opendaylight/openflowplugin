/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.util;

import static java.util.Objects.requireNonNull;

import org.opendaylight.openflowjava.protocol.api.extensibility.OFGeneralSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.keys.ActionSerializerKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.ActionChoice;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Helper for registering serializers.
 *
 * @author michal.polkorab
 */
public class ActionSerializerRegistryHelper {
    private final Uint8 version;
    private final SerializerRegistry serializerRegistry;

    /**
     * Constructor.
     *
     * @param version Openflow wire version
     * @param serializerRegistry registry to be filled with message serializers
     */
    public ActionSerializerRegistryHelper(final Uint8 version, final SerializerRegistry serializerRegistry) {
        this.version = requireNonNull(version);
        this.serializerRegistry = serializerRegistry;
    }

    /**
     * Registers given serializer.
     *
     * @param actionType action type
     * @param serializer serializer instance
     */
    public <T extends ActionChoice> void registerSerializer(final Class<T> actionType,
            final OFGeneralSerializer serializer) {
        serializerRegistry.registerSerializer(new ActionSerializerKey<>(version, actionType, (Uint32) null),
            serializer);
    }
}
