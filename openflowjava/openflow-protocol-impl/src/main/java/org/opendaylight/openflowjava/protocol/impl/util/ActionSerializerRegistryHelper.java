/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.util;

import org.opendaylight.openflowjava.protocol.api.extensibility.OFGeneralSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.keys.ActionSerializerKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.ActionChoice;

/**
 * Helper for registering serializers.
 *
 * @author michal.polkorab
 */
public class ActionSerializerRegistryHelper {

    private final short version;
    private final SerializerRegistry serializerRegistry;

    /**
     * Constructor.
     *
     * @param version Openflow wire version
     * @param serializerRegistry registry to be filled with message serializers
     */
    public ActionSerializerRegistryHelper(short version, SerializerRegistry serializerRegistry) {
        this.version = version;
        this.serializerRegistry = serializerRegistry;
    }

    /**
     * Registers given serializer.
     *
     * @param actionType action type
     * @param serializer serializer instance
     */
    public <T extends ActionChoice> void registerSerializer(Class<T> actionType,
            OFGeneralSerializer serializer) {
        serializerRegistry.registerSerializer(new ActionSerializerKey<>(version,
                actionType, null), serializer);
    }
}
