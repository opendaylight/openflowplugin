/*
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.nx.codec.action;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.nx.NiciraExtensionCodecRegistratorImpl;
import org.opendaylight.openflowjava.nx.api.NiciraActionDeserializerKey;
import org.opendaylight.openflowjava.nx.api.NiciraConstants;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.keys.ExperimenterActionDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActionDeserializer implements OFDeserializer<Action>, DeserializerRegistryInjector {

    private static final Logger LOG = LoggerFactory.getLogger(ActionDeserializer.class);

    public static final ExperimenterActionDeserializerKey OF13_DESERIALIZER_KEY = new ExperimenterActionDeserializerKey(
            EncodeConstants.OF13_VERSION_ID, NiciraConstants.NX_VENDOR_ID.toJava());
    public static final ExperimenterActionDeserializerKey OF10_DESERIALIZER_KEY = new ExperimenterActionDeserializerKey(
            EncodeConstants.OF10_VERSION_ID, NiciraConstants.NX_VENDOR_ID.toJava());

    private final short version;
    private DeserializerRegistry deserializerRegistry;

    /**
     * Constructor.
     *
     * @param version protocol wire version
     */
    public ActionDeserializer(short version) {
        this.version = version;
    }

    @Override
    public Action deserialize(ByteBuf message) {
        final int startPosition = message.readerIndex();
        // size of experimenter type
        message.skipBytes(Short.BYTES);
        // size of length
        message.skipBytes(Short.BYTES);
        long experimenterId = message.readUnsignedInt();
        if (NiciraConstants.NX_VENDOR_ID.toJava() != experimenterId) {
            throw new IllegalStateException("Experimenter ID is not Nicira vendor id but is " + experimenterId);
        }
        int subtype = message.readUnsignedShort();
        NiciraActionDeserializerKey key = new NiciraActionDeserializerKey(version, subtype);
        OFDeserializer<Action> actionDeserializer = NiciraExtensionCodecRegistratorImpl.getActionDeserializer(key);
        if (actionDeserializer == null) {
            LOG.info("No deserializer was found for key {}", key);
            return null;
        }

        message.readerIndex(startPosition);
        return actionDeserializer.deserialize(message);
    }

    @Override
    public void injectDeserializerRegistry(DeserializerRegistry registry) {
        this.deserializerRegistry = registry;
    }

    public DeserializerRegistry getDeserializerRegistry() {
        return deserializerRegistry;
    }
}
