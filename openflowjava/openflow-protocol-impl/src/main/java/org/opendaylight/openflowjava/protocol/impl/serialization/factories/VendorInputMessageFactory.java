/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.serialization.factories;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.util.ExperimenterSerializerKeyFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterOfMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.experimenter.core.ExperimenterDataOfChoice;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Translates Vendor messages (OF-1.0 limited version - skipping experimenter type).
 *
 * @author michal.polkorab
 */
public class VendorInputMessageFactory implements OFSerializer<ExperimenterOfMessage>,
        SerializerRegistryInjector {

    private SerializerRegistry registry;

    @Override
    @SuppressFBWarnings("UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR") // FB doesn't recognize Objects.requireNonNull
    public void serialize(final ExperimenterOfMessage message, final ByteBuf outBuffer) {
        Objects.requireNonNull(registry);

        Uint32 expId = message.getExperimenter().getValue();
        OFSerializer<ExperimenterDataOfChoice> serializer = registry.getSerializer(
                ExperimenterSerializerKeyFactory.createExperimenterMessageSerializerKey(
                        EncodeConstants.OF_VERSION_1_0, expId, message.getExpType().longValue()));

        // write experimenterId
        outBuffer.writeInt(message.getExperimenter().getValue().intValue());

        serializer.serialize(message.getExperimenterDataOfChoice(), outBuffer);
    }

    @Override
    public void injectSerializerRegistry(final SerializerRegistry serializerRegistry) {
        this.registry = serializerRegistry;
    }
}
