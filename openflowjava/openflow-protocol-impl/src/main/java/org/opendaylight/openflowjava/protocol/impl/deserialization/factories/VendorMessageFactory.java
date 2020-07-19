/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.factories;

import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint32;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
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
 * Translates ExperimenterMessages.
 *
 * @author michal.polkorab
 */
public class VendorMessageFactory implements OFDeserializer<ExperimenterMessage>,
        DeserializerRegistryInjector {

    private DeserializerRegistry deserializerRegistry;

    @Override
    @SuppressFBWarnings("UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR") // FB doesn't recognize Objects.requireNonNull
    public ExperimenterMessage deserialize(ByteBuf message) {
        Objects.requireNonNull(deserializerRegistry);

        final Uint32 xid = readUint32(message);
        final long expId = message.readUnsignedInt();
        OFDeserializer<ExperimenterDataOfChoice> deserializer = deserializerRegistry.getDeserializer(
                ExperimenterDeserializerKeyFactory.createVendorMessageDeserializerKey(
                        EncodeConstants.OF10_VERSION_ID, expId));
        final ExperimenterDataOfChoice vendorData = deserializer.deserialize(message);

        final ExperimenterMessageBuilder messageBld = new ExperimenterMessageBuilder()
                .setVersion(EncodeConstants.OF_VERSION_1_0)
                .setXid(xid)
                .setExperimenter(new ExperimenterId(expId))
                .setExperimenterDataOfChoice(vendorData);
        return messageBld.build();
    }

    @Override
    public void injectDeserializerRegistry(DeserializerRegistry registry) {
        this.deserializerRegistry = registry;
    }
}
