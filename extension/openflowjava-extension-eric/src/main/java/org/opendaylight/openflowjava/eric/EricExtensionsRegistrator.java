/*
 * Copyright (c) 2019 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.eric;

import com.google.common.base.Preconditions;
import org.opendaylight.openflowjava.eric.api.EricExtensionCodecRegistrator;
import org.opendaylight.openflowjava.eric.codec.match.Icmpv6NDOptionsTypeCodec;
import org.opendaylight.openflowjava.eric.codec.match.Icmpv6NDReservedCodec;

public class EricExtensionsRegistrator implements AutoCloseable {
    private final EricExtensionCodecRegistrator registrator;

    public EricExtensionsRegistrator(EricExtensionCodecRegistrator registrator) {
        this.registrator = Preconditions.checkNotNull(registrator);
        registrator.registerMatchEntrySerializer(Icmpv6NDReservedCodec.SERIALIZER_KEY,
                Icmpv6NDReservedCodec.INSTANCE);
        registrator.registerMatchEntrySerializer(Icmpv6NDOptionsTypeCodec.SERIALIZER_KEY,
                Icmpv6NDOptionsTypeCodec.INSTANCE);
        registrator.registerMatchEntryDeserializer(Icmpv6NDReservedCodec.DESERIALIZER_KEY,
                Icmpv6NDReservedCodec.INSTANCE);
        registrator.registerMatchEntryDeserializer(Icmpv6NDOptionsTypeCodec.DESERIALIZER_KEY,
                Icmpv6NDOptionsTypeCodec.INSTANCE);
    }

    @Override
    public void close()  {
        registrator.unregisterMatchEntrySerializer(Icmpv6NDReservedCodec.SERIALIZER_KEY);
        registrator.unregisterMatchEntrySerializer(Icmpv6NDOptionsTypeCodec.SERIALIZER_KEY);
        registrator.unregisterMatchEntryDeserializer(Icmpv6NDReservedCodec.DESERIALIZER_KEY);
        registrator.unregisterMatchEntryDeserializer(Icmpv6NDOptionsTypeCodec.DESERIALIZER_KEY);
    }
}