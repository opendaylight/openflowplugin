/*
 * Copyright (c) 2019 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.serialization.match;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;

public abstract class AbstractMacAddressEntrySerializer extends AbstractPrimitiveEntrySerializer<MacAddress> {
    protected AbstractMacAddressEntrySerializer(final int oxmClassCode, final int oxmFieldCode) {
        super(oxmClassCode, oxmFieldCode, EncodeConstants.MAC_ADDRESS_LENGTH);
    }

    @Override
    protected final void serializeEntry(final MacAddress entry, final ByteBuf outBuffer) {
        writeMacAddress(entry, outBuffer);
    }
}
