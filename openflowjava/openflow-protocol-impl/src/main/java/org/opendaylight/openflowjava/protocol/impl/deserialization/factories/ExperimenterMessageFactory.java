/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.factories;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint32;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.util.ExperimenterDeserializerKeyFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ExperimenterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.experimenter.core.ExperimenterDataOfChoice;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Implementation of OFDeserializer for ExperimenterMessages.
 *
 * @author michal.polkorab
 */
public class ExperimenterMessageFactory implements OFDeserializer<ExperimenterMessage>,
        DeserializerRegistryInjector {
    private DeserializerRegistry deserializerRegistry = null;

    @Override
    public ExperimenterMessage deserialize(final ByteBuf message) {
        final Uint32 xid = readUint32(message);
        final Uint32 expId = readUint32(message);
        final Uint32 expType = readUint32(message);

        final OFDeserializer<ExperimenterDataOfChoice> deserializer = deserializerRegistry.getDeserializer(
                ExperimenterDeserializerKeyFactory.createExperimenterMessageDeserializerKey(
                        EncodeConstants.OF_VERSION_1_3, expId, expType.toJava()));

        return new ExperimenterMessageBuilder()
                .setVersion(EncodeConstants.OF_VERSION_1_3)
                .setXid(xid)
                .setExperimenter(new ExperimenterId(expId))
                .setExpType(expType)
                .setExperimenterDataOfChoice(deserializer.deserialize(message))
                .build();
    }

    @Override
    public void injectDeserializerRegistry(final DeserializerRegistry registry) {
        this.deserializerRegistry = requireNonNull(registry);
    }
}
